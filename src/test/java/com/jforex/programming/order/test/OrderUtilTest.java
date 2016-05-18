package com.jforex.programming.order.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.JFException;
import com.google.common.collect.Sets;
import com.jforex.programming.order.OrderParams;
import com.jforex.programming.order.OrderUtil;
import com.jforex.programming.order.call.OrderCallExecutor;
import com.jforex.programming.order.call.OrderCallExecutorResult;
import com.jforex.programming.order.call.OrderCallRejectException;
import com.jforex.programming.order.call.OrderCallRequest;
import com.jforex.programming.order.call.OrderSupplierCall;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventGateway;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.position.Position;
import com.jforex.programming.position.PositionFactory;
import com.jforex.programming.test.common.InstrumentUtilForTest;
import com.jforex.programming.test.common.OrderParamsForTest;
import com.jforex.programming.test.fakes.IOrderForTest;

import rx.Completable;
import rx.observers.TestSubscriber;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

public class OrderUtilTest extends InstrumentUtilForTest {

    private OrderUtil orderUtil;

    @Mock
    private OrderCallExecutor orderCallExecutorMock;
    @Mock
    private OrderEventGateway orderEventGatewayMock;
    @Mock
    private PositionFactory positionFactoryMock;
    @Mock
    private Position positionMock;
    @Captor
    private ArgumentCaptor<OrderSupplierCall> orderCallCaptor;
    private Subject<OrderEvent, OrderEvent> orderEventSubject = PublishSubject.create();
    private final TestSubscriber<OrderEvent> subscriber = new TestSubscriber<>();
    private final OrderParams orderParams = OrderParamsForTest.paramsBuyEURUSD();
    private final IOrderForTest orderUnderTest = IOrderForTest.buyOrderEURUSD();
    private OrderCallExecutorResult orderExecutorResult;
    private OrderCallExecutorResult orderExecutorResultWithJFException;
    private final String mergeLabel = "MergeLabel";
    private final Set<IOrder> toMergeOrders =
            Sets.newHashSet(IOrderForTest.buyOrderEURUSD(), IOrderForTest.sellOrderEURUSD());
    private final String newLabel = "NewLabel";
    private final long newGTT = 123456L;
    private final double newAmount = 0.12;
    private final double newOpenPrice = 1.12122;
    private final double newSL = 1.10987;
    private final double newTP = 1.11001;

    @Before
    public void setUp() {
        initCommonTestFramework();

        orderEventSubject = PublishSubject.create();
        orderExecutorResult = new OrderCallExecutorResult(Optional.of(orderUnderTest),
                                                          Optional.empty());
        orderExecutorResultWithJFException = new OrderCallExecutorResult(Optional.empty(),
                                                                         Optional.of(jfException));
        setUpMocks();
        orderUtil = new OrderUtil(engineMock,
                                  orderCallExecutorMock,
                                  orderEventGatewayMock,
                                  positionFactoryMock);
    }

    private void setUpMocks() {
        when(orderEventGatewayMock.observable()).thenReturn(orderEventSubject);
        when(orderCallExecutorMock.run(any(OrderSupplierCall.class))).thenReturn(orderExecutorResult);
        when(positionFactoryMock.forInstrument(instrumentEURUSD)).thenReturn(positionMock);
    }

    private void prepareJFException() {
        when(orderCallExecutorMock.run(any(OrderSupplierCall.class)))
                .thenReturn(orderExecutorResultWithJFException);
    }

    private void assertJFException() {
        subscriber.assertValueCount(0);
        subscriber.assertError(JFException.class);
    }

    private void assertRejectException() {
        subscriber.assertValueCount(0);
        subscriber.assertError(OrderCallRejectException.class);
    }

    private void captureAndRunOrderCall() throws JFException {
        verify(orderCallExecutorMock).run(orderCallCaptor.capture());
        orderCallCaptor.getValue().get();
    }

    private void assertOrderEvent(final OrderEventType orderEventType) {
        subscriber.assertNoErrors();
        subscriber.assertValueCount(1);

        final OrderEvent orderEvent = subscriber.getOnNextEvents().get(0);

        assertThat(orderEvent.order(), equalTo(orderUnderTest));
        assertThat(orderEvent.type(), equalTo(orderEventType));
    }

    private void assertCompletedOrderEvent(final OrderEventType orderEventType) {
        assertOrderEvent(orderEventType);
        subscriber.assertCompleted();
    }

    private void assertOtherOrderIsIgnored() {
        orderEventSubject.onNext(new OrderEvent(IOrderForTest.orderAUDUSD(), OrderEventType.MERGE_OK));
        subscriber.assertNoErrors();
        subscriber.assertNotCompleted();
    }

    private void sendOrderEvent(final OrderEventType orderEventType) {
        orderEventSubject.onNext(new OrderEvent(orderUnderTest, orderEventType));
    }

    @Test
    public void testSubmitCallsOnIEngine() throws JFException {
        orderUtil.submitOrder(orderParams);

        captureAndRunOrderCall();

        engineForTest.verifySubmit(orderParams, 1);
    }

    @Test
    public void testSubmitRegistersOnEventGateway() {
        orderUtil.submitOrder(orderParams);

        verify(orderEventGatewayMock).registerOrderRequest(orderUnderTest, OrderCallRequest.SUBMIT);
    }

    @Test
    public void testSubmitObservableIgnoresOtherOrders() {
        orderUtil.submitOrder(orderParams).subscribe(subscriber);

        assertOtherOrderIsIgnored();
    }

    @Test
    public void testSubmitObservableGetsNotifiedOnJFException() {
        prepareJFException();

        orderUtil.submitOrder(orderParams).subscribe(subscriber);

        assertJFException();
    }

    @Test
    public void testSubmitObservableCompletesOnFullFill() {
        orderUtil.submitOrder(orderParams).subscribe(subscriber);

        sendOrderEvent(OrderEventType.FULL_FILL_OK);

        assertCompletedOrderEvent(OrderEventType.FULL_FILL_OK);
    }

    @Test
    public void testSubmitObservableHasRejectExceptionOnSubmitReject() {
        orderUtil.submitOrder(orderParams).subscribe(subscriber);

        sendOrderEvent(OrderEventType.SUBMIT_REJECTED);

        assertRejectException();
    }

    @Test
    public void testSubmitObservableHasRejectExceptionOnFillReject() {
        orderUtil.submitOrder(orderParams).subscribe(subscriber);

        sendOrderEvent(OrderEventType.FILL_REJECTED);

        assertRejectException();
    }

    @Test
    public void testSubmitObservableIsNotCompleteOnSubmitOK() {
        orderUtil.submitOrder(orderParams).subscribe(subscriber);

        sendOrderEvent(OrderEventType.SUBMIT_OK);

        subscriber.assertValueCount(1);
        subscriber.assertNotCompleted();
    }

    @Test
    public void testMergeCallsOnIEngine() throws JFException {
        orderUtil.mergeOrders(mergeLabel, toMergeOrders);

        captureAndRunOrderCall();

        engineForTest.verifyMerge(mergeLabel, toMergeOrders, 1);
    }

    @Test
    public void testMergePositionCallsOnPosition() {
        final Completable expectedCompletable = Completable.complete();
        when(positionMock.merge(mergeLabel)).thenReturn(expectedCompletable);

        final Completable mergeCompletable = orderUtil.mergePositionOrders(mergeLabel, instrumentEURUSD);

        assertThat(mergeCompletable, equalTo(expectedCompletable));
    }

    @Test
    public void testMergeRegistersOnEventGateway() {
        orderUtil.mergeOrders(mergeLabel, toMergeOrders);

        verify(orderEventGatewayMock).registerOrderRequest(orderUnderTest, OrderCallRequest.MERGE);
    }

    @Test
    public void testMergeObservableIgnoresOtherOrders() {
        orderUtil.mergeOrders(mergeLabel, toMergeOrders).subscribe(subscriber);

        assertOtherOrderIsIgnored();
    }

    @Test
    public void testMergeObservableGetsNotifiedOnJFException() {
        prepareJFException();

        orderUtil.mergeOrders(mergeLabel, toMergeOrders).subscribe(subscriber);

        assertJFException();
    }

    @Test
    public void testMergeObservableCompletesOnMergeOK() {
        orderUtil.mergeOrders(mergeLabel, toMergeOrders).subscribe(subscriber);

        sendOrderEvent(OrderEventType.MERGE_OK);

        assertCompletedOrderEvent(OrderEventType.MERGE_OK);
    }

    @Test
    public void testMergeObservableCompletesOnMergeCloseOK() {
        orderUtil.mergeOrders(mergeLabel, toMergeOrders).subscribe(subscriber);

        sendOrderEvent(OrderEventType.MERGE_CLOSE_OK);

        assertCompletedOrderEvent(OrderEventType.MERGE_CLOSE_OK);
    }

    @Test
    public void testMergeObservableHasRejectExceptionOnMergeReject() {
        orderUtil.mergeOrders(mergeLabel, toMergeOrders).subscribe(subscriber);

        sendOrderEvent(OrderEventType.MERGE_REJECTED);

        assertRejectException();
    }

    @Test
    public void testCloseCallsOnOrder() throws JFException {
        orderUtil.close(orderUnderTest);

        captureAndRunOrderCall();

        verify(orderUnderTest).close();
    }

    @Test
    public void testCloseRegistersOnEventGateway() {
        orderUtil.close(orderUnderTest);

        verify(orderEventGatewayMock).registerOrderRequest(orderUnderTest, OrderCallRequest.CLOSE);
    }

    @Test
    public void testCloseObservableIgnoresOtherOrders() {
        orderUtil.close(orderUnderTest).subscribe(subscriber);

        assertOtherOrderIsIgnored();
    }

    @Test
    public void testCloseObservableGetsNotifiedOnJFException() {
        prepareJFException();

        orderUtil.close(orderUnderTest).subscribe(subscriber);

        assertJFException();
    }

    @Test
    public void testCloseObservableCompletesOnCloseOK() {
        orderUtil.close(orderUnderTest).subscribe(subscriber);

        sendOrderEvent(OrderEventType.CLOSE_OK);

        assertCompletedOrderEvent(OrderEventType.CLOSE_OK);
    }

    @Test
    public void testCloseObservableHasRejectExceptionOnCloseReject() {
        orderUtil.close(orderUnderTest).subscribe(subscriber);

        sendOrderEvent(OrderEventType.CLOSE_REJECTED);

        assertRejectException();
    }

    @Test
    public void testCloseOnEmptyPositionDoesNotCallOnIEngine() {
        final TestSubscriber<OrderEvent> closeSubscriber = new TestSubscriber<>();

        orderUtil.closePosition(instrumentEURUSD).subscribe(closeSubscriber);

        verifyZeroInteractions(engineMock);
    }

    @Test
    public void testSetLabelCallsOnOrder() throws JFException {
        orderUtil.setLabel(orderUnderTest, newLabel);

        captureAndRunOrderCall();

        verify(orderUnderTest).setLabel(newLabel);
    }

    @Test
    public void testSetLabelRegistersOnEventGateway() {
        orderUtil.setLabel(orderUnderTest, newLabel);

        verify(orderEventGatewayMock).registerOrderRequest(orderUnderTest, OrderCallRequest.CHANGE_LABEL);
    }

    @Test
    public void testSetLabelObservableIgnoresOtherOrders() {
        orderUtil.setLabel(orderUnderTest, newLabel).subscribe(subscriber);

        assertOtherOrderIsIgnored();
    }

    @Test
    public void testSetLabelObservableGetsNotifiedOnJFException() {
        prepareJFException();

        orderUtil.setLabel(orderUnderTest, newLabel).subscribe(subscriber);

        assertJFException();
    }

    @Test
    public void testSetLabelObservableCompletesOnChangeLabelOK() {
        orderUtil.setLabel(orderUnderTest, newLabel).subscribe(subscriber);

        sendOrderEvent(OrderEventType.LABEL_CHANGE_OK);

        assertCompletedOrderEvent(OrderEventType.LABEL_CHANGE_OK);
    }

    @Test
    public void testSetLabelObservableHasRejectExceptionOnSetLabelReject() {
        orderUtil.setLabel(orderUnderTest, newLabel).subscribe(subscriber);

        sendOrderEvent(OrderEventType.CHANGE_LABEL_REJECTED);

        assertRejectException();
    }

    @Test
    public void testSetGTTCallsOnOrder() throws JFException {
        orderUtil.setGoodTillTime(orderUnderTest, newGTT);

        captureAndRunOrderCall();

        verify(orderUnderTest).setGoodTillTime(newGTT);
    }

    @Test
    public void testSetGTTRegistersOnEventGateway() {
        orderUtil.setGoodTillTime(orderUnderTest, newGTT);

        verify(orderEventGatewayMock).registerOrderRequest(orderUnderTest, OrderCallRequest.CHANGE_GTT);
    }

    @Test
    public void testSetGTTObservableIgnoresOtherOrders() {
        orderUtil.setGoodTillTime(orderUnderTest, newGTT).subscribe(subscriber);

        assertOtherOrderIsIgnored();
    }

    @Test
    public void testSetGTTObservableGetsNotifiedOnJFException() {
        prepareJFException();

        orderUtil.setGoodTillTime(orderUnderTest, newGTT).subscribe(subscriber);

        assertJFException();
    }

    @Test
    public void testSetGTTObservableCompletesOnSetGTTOK() {
        orderUtil.setGoodTillTime(orderUnderTest, newGTT).subscribe(subscriber);

        sendOrderEvent(OrderEventType.GTT_CHANGE_OK);

        assertCompletedOrderEvent(OrderEventType.GTT_CHANGE_OK);
    }

    @Test
    public void testSetGTTObservableHasRejectExceptionOnSetGTTReject() {
        orderUtil.setGoodTillTime(orderUnderTest, newGTT).subscribe(subscriber);

        sendOrderEvent(OrderEventType.CHANGE_GTT_REJECTED);

        assertRejectException();
    }

    @Test
    public void testSetOpenPriceCallsOnOrder() throws JFException {
        orderUtil.setOpenPrice(orderUnderTest, newOpenPrice);

        captureAndRunOrderCall();

        verify(orderUnderTest).setOpenPrice(newOpenPrice);
    }

    @Test
    public void testSetOpenPriceRegistersOnEventGateway() {
        orderUtil.setOpenPrice(orderUnderTest, newOpenPrice);

        verify(orderEventGatewayMock).registerOrderRequest(orderUnderTest, OrderCallRequest.CHANGE_OPENPRICE);
    }

    @Test
    public void testSetOpenPriceObservableIgnoresOtherOrders() {
        orderUtil.setOpenPrice(orderUnderTest, newOpenPrice).subscribe(subscriber);

        assertOtherOrderIsIgnored();
    }

    @Test
    public void testSetOpenPriceObservableGetsNotifiedOnJFException() {
        prepareJFException();

        orderUtil.setOpenPrice(orderUnderTest, newOpenPrice).subscribe(subscriber);

        assertJFException();
    }

    @Test
    public void testSetOpenPriceObservableCompletesOnSetOpenPriceOK() {
        orderUtil.setOpenPrice(orderUnderTest, newOpenPrice).subscribe(subscriber);

        sendOrderEvent(OrderEventType.OPENPRICE_CHANGE_OK);

        assertCompletedOrderEvent(OrderEventType.OPENPRICE_CHANGE_OK);
    }

    @Test
    public void testSetOpenPriceObservableHasRejectExceptionOnSetOpenPriceReject() {
        orderUtil.setOpenPrice(orderUnderTest, newOpenPrice).subscribe(subscriber);

        sendOrderEvent(OrderEventType.CHANGE_OPENPRICE_REJECTED);

        assertRejectException();
    }

    @Test
    public void testSetRequestedAmountCallsOnOrder() throws JFException {
        orderUtil.setRequestedAmount(orderUnderTest, newAmount);

        captureAndRunOrderCall();

        verify(orderUnderTest).setRequestedAmount(newAmount);
    }

    @Test
    public void testSetRequestedAmountRegistersOnEventGateway() {
        orderUtil.setRequestedAmount(orderUnderTest, newAmount);

        verify(orderEventGatewayMock).registerOrderRequest(orderUnderTest, OrderCallRequest.CHANGE_REQUESTED_AMOUNT);
    }

    @Test
    public void testSetRequestedAmountObservableIgnoresOtherOrders() {
        orderUtil.setRequestedAmount(orderUnderTest, newAmount).subscribe(subscriber);

        assertOtherOrderIsIgnored();
    }

    @Test
    public void testSetRequestedAmountObservableGetsNotifiedOnJFException() {
        prepareJFException();

        orderUtil.setRequestedAmount(orderUnderTest, newAmount).subscribe(subscriber);

        assertJFException();
    }

    @Test
    public void testSetRequestedAmountObservableCompletesOnSetRequestedAmountOK() {
        orderUtil.setRequestedAmount(orderUnderTest, newAmount).subscribe(subscriber);

        sendOrderEvent(OrderEventType.AMOUNT_CHANGE_OK);

        assertCompletedOrderEvent(OrderEventType.AMOUNT_CHANGE_OK);
    }

    @Test
    public void testSetRequestedAmountObservableHasRejectExceptionOnSetRequestedAmountReject() {
        orderUtil.setRequestedAmount(orderUnderTest, newAmount).subscribe(subscriber);

        sendOrderEvent(OrderEventType.CHANGE_AMOUNT_REJECTED);

        assertRejectException();
    }

    @Test
    public void testSetStopLossPriceCallsOnOrder() throws JFException {
        orderUtil.setStopLossPrice(orderUnderTest, newSL);

        captureAndRunOrderCall();

        verify(orderUnderTest).setStopLossPrice(newSL);
    }

    @Test
    public void testSetStopLossPriceRegistersOnEventGateway() {
        orderUtil.setStopLossPrice(orderUnderTest, newSL);

        verify(orderEventGatewayMock).registerOrderRequest(orderUnderTest, OrderCallRequest.CHANGE_SL);
    }

    @Test
    public void testSetStopLossPriceObservableIgnoresOtherOrders() {
        orderUtil.setStopLossPrice(orderUnderTest, newSL).subscribe(subscriber);

        assertOtherOrderIsIgnored();
    }

    @Test
    public void testSetStopLossPriceObservableGetsNotifiedOnJFException() {
        prepareJFException();

        orderUtil.setStopLossPrice(orderUnderTest, newSL).subscribe(subscriber);

        assertJFException();
    }

    @Test
    public void testSetStopLossPriceObservableCompletesOnSetStopLossPriceOK() {
        orderUtil.setStopLossPrice(orderUnderTest, newSL).subscribe(subscriber);

        sendOrderEvent(OrderEventType.SL_CHANGE_OK);

        assertCompletedOrderEvent(OrderEventType.SL_CHANGE_OK);
    }

    @Test
    public void testSetStopLossPriceObservableHasRejectExceptionOnSetStopLossPriceReject() {
        orderUtil.setStopLossPrice(orderUnderTest, newSL).subscribe(subscriber);

        sendOrderEvent(OrderEventType.CHANGE_SL_REJECTED);

        assertRejectException();
    }

    @Test
    public void testSetTakeProfitPriceCallsOnOrder() throws JFException {
        orderUtil.setTakeProfitPrice(orderUnderTest, newTP);

        captureAndRunOrderCall();

        verify(orderUnderTest).setTakeProfitPrice(newTP);
    }

    @Test
    public void testSetTakeProfitPriceRegistersOnEventGateway() {
        orderUtil.setTakeProfitPrice(orderUnderTest, newTP);

        verify(orderEventGatewayMock).registerOrderRequest(orderUnderTest, OrderCallRequest.CHANGE_TP);
    }

    @Test
    public void testSetTakeProfitPriceObservableIgnoresOtherOrders() {
        orderUtil.setTakeProfitPrice(orderUnderTest, newTP).subscribe(subscriber);

        assertOtherOrderIsIgnored();
    }

    @Test
    public void testSetTakeProfitPriceObservableGetsNotifiedOnJFException() {
        prepareJFException();

        orderUtil.setTakeProfitPrice(orderUnderTest, newTP).subscribe(subscriber);

        assertJFException();
    }

    @Test
    public void testSetTakeProfitPriceObservableCompletesOnSetTakeProfitPriceOK() {
        orderUtil.setTakeProfitPrice(orderUnderTest, newTP).subscribe(subscriber);

        sendOrderEvent(OrderEventType.TP_CHANGE_OK);

        assertCompletedOrderEvent(OrderEventType.TP_CHANGE_OK);
    }

    @Test
    public void testSetTakeProfitPriceObservableHasRejectExceptionOnSetTakeProfitPriceReject() {
        orderUtil.setTakeProfitPrice(orderUnderTest, newTP).subscribe(subscriber);

        sendOrderEvent(OrderEventType.CHANGE_TP_REJECTED);

        assertRejectException();
    }
}
