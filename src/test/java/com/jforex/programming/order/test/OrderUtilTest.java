package com.jforex.programming.order.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import com.google.common.collect.Sets;
import com.jforex.programming.order.OrderParams;
import com.jforex.programming.order.OrderUtil;
import com.jforex.programming.order.call.OrderCallExecutor;
import com.jforex.programming.order.call.OrderCallExecutorResult;
import com.jforex.programming.order.call.OrderCallRequest;
import com.jforex.programming.order.call.OrderSupplierCall;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventGateway;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.test.common.InstrumentUtilForTest;
import com.jforex.programming.test.common.OrderParamsForTest;
import com.jforex.programming.test.fakes.IOrderForTest;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.JFException;

import rx.observers.TestSubscriber;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

public class OrderUtilTest extends InstrumentUtilForTest {

    private OrderUtil orderUtil;

    @Mock
    private OrderCallExecutor orderCallExecutorMock;
    @Mock
    private OrderEventGateway orderEventGatewayMock;
    @Captor
    private ArgumentCaptor<OrderSupplierCall> orderCallCaptor;
    private Subject<OrderEvent, OrderEvent> orderEventSubject = PublishSubject.create();
    private final TestSubscriber<OrderEvent> subscriber = new TestSubscriber<>();
    private final OrderParams orderParams = OrderParamsForTest.paramsBuyEURUSD();
    private final IOrderForTest orderUnderTest = IOrderForTest.buyOrderEURUSD();
    private OrderCallExecutorResult orderExecutorResult;
    private OrderCallExecutorResult orderExecutorResultWithException;
    private final String mergeLabel = "MergeLabel";
    private final Set<IOrder> toMergeOrders =
            Sets.newHashSet(IOrderForTest.buyOrderEURUSD(), IOrderForTest.orderAUDUSD());
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
        orderExecutorResultWithException = new OrderCallExecutorResult(Optional.empty(),
                                                                       Optional.of(jfException));
        setUpMocks();
        orderUtil = new OrderUtil(engineMock,
                                  orderCallExecutorMock,
                                  orderEventGatewayMock);
    }

    private void setUpMocks() {
        when(orderEventGatewayMock.observable()).thenReturn(orderEventSubject);
        when(orderCallExecutorMock.run(any(OrderSupplierCall.class))).thenReturn(orderExecutorResult);
    }

    private void prepareResultException() {
        when(orderCallExecutorMock.run(any(OrderSupplierCall.class)))
                .thenReturn(orderExecutorResultWithException);
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

    private void assertOrderFilter() {
        orderEventSubject.onNext(new OrderEvent(IOrderForTest.orderAUDUSD(), OrderEventType.MERGE_OK));
        subscriber.assertNoErrors();
        subscriber.assertValueCount(0);
    }

    @Test
    public void testSubmitCallsOnIEngine() throws JFException {
        orderUtil.submit(orderParams);

        captureAndRunOrderCall();

        engineForTest.verifySubmit(orderParams, 1);
    }

    @Test
    public void testSubmitRegistersOnEventGateway() {
        orderUtil.submit(orderParams);

        verify(orderEventGatewayMock).registerOrderRequest(orderUnderTest, OrderCallRequest.SUBMIT);
    }

    @Test
    public void testSubmitReturnsFilteredOrderEventObservable() {
        orderUtil.submit(orderParams).subscribe(subscriber);

        orderEventSubject.onNext(new OrderEvent(orderUnderTest, OrderEventType.SUBMIT_OK));

        assertOrderEvent(OrderEventType.SUBMIT_OK);
    }

    @Test
    public void testSubmitObservableIgnoresOtherOrders() {
        orderUtil.submit(orderParams).subscribe(subscriber);

        assertOrderFilter();
    }

    @Test
    public void testSubmitObservableGetsNotifiedOnException() {
        prepareResultException();

        orderUtil.submit(orderParams).subscribe(subscriber);

        subscriber.assertError(JFException.class);
    }

    @Test
    public void testSubmitObservableCompletesOnFullFill() {
        orderUtil.submit(orderParams).subscribe(subscriber);

        orderEventSubject.onNext(new OrderEvent(orderUnderTest,
                                                OrderEventType.FULL_FILL_OK));

        subscriber.assertValueCount(1);
        subscriber.getOnNextEvents().get(0);
        subscriber.assertCompleted();
    }

    @Test
    public void testMergeCallsOnIEngine() throws JFException {
        orderUtil.merge(mergeLabel, toMergeOrders);

        captureAndRunOrderCall();

        engineForTest.verifyMerge(mergeLabel, toMergeOrders, 1);
    }

    @Test
    public void testMergeRegistersOnEventGateway() {
        orderUtil.merge(mergeLabel, toMergeOrders);

        verify(orderEventGatewayMock).registerOrderRequest(orderUnderTest, OrderCallRequest.MERGE);
    }

    @Test
    public void testMergeReturnsFilteredOrderEventObservable() {
        orderUtil.merge(mergeLabel, toMergeOrders).subscribe(subscriber);

        orderEventSubject.onNext(new OrderEvent(orderUnderTest, OrderEventType.MERGE_OK));

        assertOrderEvent(OrderEventType.MERGE_OK);
    }

    @Test
    public void testMergeObservableIgnoresOtherOrders() {
        orderUtil.merge(mergeLabel, toMergeOrders).subscribe(subscriber);

        assertOrderFilter();
    }

    @Test
    public void testMergeObservableGetsNotifiedOnException() {
        prepareResultException();

        orderUtil.merge(mergeLabel, toMergeOrders).subscribe(subscriber);

        subscriber.assertError(JFException.class);
    }

    // TODO: adapt from orderChangeTest!

    private void verifyOrderCallAndOrderRegistration(final IOrder order,
                                                     final OrderCallRequest orderCallRequest) throws JFException {
        verify(orderCallExecutorMock).run(orderCallCaptor.capture());
        orderCallCaptor.getValue().get();
        verify(orderEventGatewayMock).registerOrderRequest(order, orderCallRequest);
    }

    @Test
    public void testCloseIsCorrect() throws JFException {
        orderUtil.close(orderUnderTest);

        verifyOrderCallAndOrderRegistration(orderUnderTest,
                                            OrderCallRequest.CLOSE);

        verify(orderUnderTest).close();
    }

    @Test
    public void testChangeLabelIsCorrect() throws JFException {
        orderUtil.setLabel(orderUnderTest, newLabel);

        verifyOrderCallAndOrderRegistration(orderUnderTest,
                                            OrderCallRequest.CHANGE_LABEL);

        verify(orderUnderTest).setLabel(newLabel);
    }

    @Test
    public void testChangeGTTIsCorrect() throws JFException {
        orderUtil.setGTT(orderUnderTest, newGTT);

        verifyOrderCallAndOrderRegistration(orderUnderTest,
                                            OrderCallRequest.CHANGE_GTT);

        verify(orderUnderTest).setGoodTillTime(newGTT);
    }

    @Test
    public void testChangeAmountIsCorrect() throws JFException {
        orderUtil.setAmount(orderUnderTest, newAmount);

        verifyOrderCallAndOrderRegistration(orderUnderTest,
                                            OrderCallRequest.CHANGE_AMOUNT);

        verify(orderUnderTest).setRequestedAmount(newAmount);
    }

    @Test
    public void testChangeOpenPriceIsCorrect() throws JFException {
        orderUtil.setOpenPrice(orderUnderTest, newOpenPrice);

        verifyOrderCallAndOrderRegistration(orderUnderTest,
                                            OrderCallRequest.CHANGE_OPENPRICE);

        verify(orderUnderTest).setOpenPrice(newOpenPrice);
    }

    @Test
    public void testChangeSLIsCorrect() throws JFException {
        orderUtil.setSL(orderUnderTest, newSL);

        verifyOrderCallAndOrderRegistration(orderUnderTest,
                                            OrderCallRequest.CHANGE_SL);

        verify(orderUnderTest).setStopLossPrice(newSL);
    }

    @Test
    public void testChangeSLWithPipsIsCorrect() throws JFException {
        final double pips = 20.3;
        final double newSLForPips = askEURUSD - pips * instrumentEURUSD.getPipValue();
        orderUtil.setSLWithPips(orderUnderTest, askEURUSD, pips);

        verifyOrderCallAndOrderRegistration(orderUnderTest,
                                            OrderCallRequest.CHANGE_SL);

        verify(orderUnderTest).setStopLossPrice(newSLForPips);
    }

    @Test
    public void testChangeTPIsCorrect() throws JFException {
        orderUtil.setTP(orderUnderTest, newTP);

        verifyOrderCallAndOrderRegistration(orderUnderTest,
                                            OrderCallRequest.CHANGE_TP);

        verify(orderUnderTest).setTakeProfitPrice(newTP);
    }

    @Test
    public void testChangeTPWithPipsIsCorrect() throws JFException {
        final double pips = 20.3;
        final double newTPForPips = askEURUSD + pips * instrumentEURUSD.getPipValue();
        orderUtil.setTPWithPips(orderUnderTest, askEURUSD, pips);

        verifyOrderCallAndOrderRegistration(orderUnderTest,
                                            OrderCallRequest.CHANGE_TP);

        verify(orderUnderTest).setTakeProfitPrice(newTPForPips);
    }
}
