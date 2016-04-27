package com.jforex.programming.order.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.google.common.collect.Sets;
import com.jforex.programming.order.OrderCreateResult;
import com.jforex.programming.order.OrderParams;
import com.jforex.programming.order.OrderUtil;
import com.jforex.programming.order.OrderUtilObservable;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.position.PositionTaskRejectException;
import com.jforex.programming.test.common.InstrumentUtilForTest;
import com.jforex.programming.test.common.OrderParamsForTest;
import com.jforex.programming.test.fakes.IOrderForTest;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.JFException;

import rx.observers.TestSubscriber;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

public class OrderUtilObservableTest extends InstrumentUtilForTest {

    private OrderUtilObservable orderUtilObservable;

    @Mock
    private OrderUtil orderUtilMock;
    private Subject<OrderEvent, OrderEvent> orderEventSubject;
    private final IOrderForTest buyOrder = IOrderForTest.buyOrderEURUSD();
    private final IOrderForTest sellOrder = IOrderForTest.sellOrderEURUSD();
    private final OrderParams orderParams = OrderParamsForTest.paramsBuyEURUSD();
    private final TestSubscriber<IOrder> orderSubscriber = new TestSubscriber<>();
    private OrderCreateResult submitOKResult;
    private OrderCreateResult submitExceptionResult;
    private OrderCreateResult mergeOKResult;
    private OrderCreateResult mergeExceptionResult;
    private Optional<Exception> changeOKResult;
    private Optional<Exception> changeExceptionResult;
    private final String mergeLabel = "MergeLabel";
    private Set<IOrder> orderSet;
    private final double newSL = askEURUSD;
    private final double newTP = bidEURUSD;

    @Before
    public void setUp() {
        initCommonTestFramework();
        orderEventSubject = PublishSubject.create();
        submitOKResult = new OrderCreateResult(Optional.of(buyOrder),
                                               Optional.empty());
        submitExceptionResult = new OrderCreateResult(Optional.empty(),
                                                      Optional.of(jfException));
        mergeOKResult = new OrderCreateResult(Optional.of(buyOrder),
                                              Optional.empty());
        mergeExceptionResult = new OrderCreateResult(Optional.empty(),
                                                     Optional.of(jfException));
        changeOKResult = Optional.empty();
        changeExceptionResult = Optional.of(jfException);
        orderSet = Sets.newHashSet(buyOrder, sellOrder);

        orderUtilObservable = new OrderUtilObservable(orderUtilMock, orderEventSubject);
    }

    private void assertExceptionNotification() {
        orderSubscriber.assertError(JFException.class);
        orderSubscriber.assertValueCount(0);
    }

    private void assertRejectExceptionNotification() {
        orderSubscriber.assertError(PositionTaskRejectException.class);
        orderSubscriber.assertValueCount(0);
    }

    private void assertOrderNotification() {
        orderSubscriber.assertNoErrors();
        orderSubscriber.assertValueCount(1);
        assertThat(orderSubscriber.getOnNextEvents().get(0), equalTo(buyOrder));
    }

    private void testSubmitWithOrderEventType(final OrderEventType orderEventType) {
        when(orderUtilMock.submit(orderParams)).thenReturn(submitOKResult);
        final OrderEvent event = new OrderEvent(buyOrder, orderEventType);

        orderUtilObservable.submit(orderParams)
                .subscribe(orderSubscriber);
        orderEventSubject.onNext(event);

        verify(orderUtilMock).submit(orderParams);
    }

    private void testMergeWithOrderEventType(final OrderEventType orderEventType) {
        when(orderUtilMock.merge(mergeLabel, orderSet)).thenReturn(mergeOKResult);
        final OrderEvent event = new OrderEvent(buyOrder, orderEventType);

        orderUtilObservable.merge(mergeLabel, orderSet)
                .subscribe(orderSubscriber);
        orderEventSubject.onNext(event);

        verify(orderUtilMock).merge(mergeLabel, orderSet);
    }

    private void testCloseWithOrderEventType(final OrderEventType orderEventType) {
        when(orderUtilMock.close(buyOrder)).thenReturn(changeOKResult);
        final OrderEvent event = new OrderEvent(buyOrder, orderEventType);

        orderUtilObservable.close(buyOrder)
                .subscribe(orderSubscriber);
        orderEventSubject.onNext(event);

        verify(orderUtilMock).close(buyOrder);
    }

    private void testChangeLabelWithOrderEventType(final OrderEventType orderEventType) {
        final String newLabel = "New Label";
        when(orderUtilMock.setLabel(buyOrder, newLabel)).thenReturn(changeOKResult);
        final OrderEvent event = new OrderEvent(buyOrder, orderEventType);

        orderUtilObservable.setLabel(buyOrder, newLabel)
                .subscribe(orderSubscriber);
        orderEventSubject.onNext(event);

        verify(orderUtilMock).setLabel(buyOrder, newLabel);
    }

    private void testChangeGTTWithOrderEventType(final OrderEventType orderEventType) {
        final long newGTT = 0L;
        when(orderUtilMock.setGTT(buyOrder, newGTT)).thenReturn(changeOKResult);
        final OrderEvent event = new OrderEvent(buyOrder, orderEventType);

        orderUtilObservable.setGTT(buyOrder, newGTT)
                .subscribe(orderSubscriber);
        orderEventSubject.onNext(event);

        verify(orderUtilMock).setGTT(buyOrder, newGTT);
    }

    private void testChangeOpenPriceWithOrderEventType(final OrderEventType orderEventType) {
        final double newOpenPrice = 0;
        when(orderUtilMock.setOpenPrice(buyOrder, newOpenPrice)).thenReturn(changeOKResult);
        final OrderEvent event = new OrderEvent(buyOrder, orderEventType);

        orderUtilObservable.setOpenPrice(buyOrder, newOpenPrice)
                .subscribe(orderSubscriber);
        orderEventSubject.onNext(event);

        verify(orderUtilMock).setOpenPrice(buyOrder, newOpenPrice);
    }

    private void testChangeAmountWithOrderEventType(final OrderEventType orderEventType) {
        final double newAmount = 0;
        when(orderUtilMock.setAmount(buyOrder, newAmount)).thenReturn(changeOKResult);
        final OrderEvent event = new OrderEvent(buyOrder, orderEventType);

        orderUtilObservable.setAmount(buyOrder, newAmount)
                .subscribe(orderSubscriber);
        orderEventSubject.onNext(event);

        verify(orderUtilMock).setAmount(buyOrder, newAmount);
    }

    private void testChangeSLWithOrderEventType(final OrderEventType orderEventType) {
        when(orderUtilMock.setSL(buyOrder, newSL)).thenReturn(changeOKResult);
        final OrderEvent event = new OrderEvent(buyOrder, orderEventType);

        orderUtilObservable.setSL(buyOrder, newSL)
                .subscribe(orderSubscriber);
        orderEventSubject.onNext(event);

        verify(orderUtilMock).setSL(buyOrder, newSL);
    }

    private void testChangeTPWithOrderEventType(final OrderEventType orderEventType) {
        when(orderUtilMock.setTP(buyOrder, newTP)).thenReturn(changeOKResult);
        final OrderEvent event = new OrderEvent(buyOrder, orderEventType);

        orderUtilObservable.setTP(buyOrder, newTP)
                .subscribe(orderSubscriber);
        orderEventSubject.onNext(event);

        verify(orderUtilMock).setTP(buyOrder, newTP);
    }

    @Test
    public void testSubmitWithExceptionObservableIsCorrect() {
        when(orderUtilMock.submit(orderParams)).thenReturn(submitExceptionResult);

        orderUtilObservable.submit(orderParams)
                .subscribe(orderSubscriber);

        verify(orderUtilMock).submit(orderParams);
        assertExceptionNotification();
    }

    @Test
    public void testSubmitObservableForFullFill() {
        testSubmitWithOrderEventType(OrderEventType.FULL_FILL_OK);
        assertOrderNotification();
    }

    @Test
    public void testSubmitObservableForFillReject() {
        testSubmitWithOrderEventType(OrderEventType.FILL_REJECTED);
        assertRejectExceptionNotification();
    }

    @Test
    public void testSubmitObservableForSubmitRejected() {
        testSubmitWithOrderEventType(OrderEventType.SUBMIT_REJECTED);
        assertRejectExceptionNotification();
    }

    @Test
    public void testMergeWithExceptionObservableIsCorrect() {
        when(orderUtilMock.merge(mergeLabel, orderSet)).thenReturn(mergeExceptionResult);

        orderUtilObservable.merge(mergeLabel, orderSet)
                .subscribe(orderSubscriber);

        verify(orderUtilMock).merge(mergeLabel, orderSet);
        assertExceptionNotification();
    }

    @Test
    public void testMergeObservableForMergeOK() {
        testMergeWithOrderEventType(OrderEventType.MERGE_OK);
        assertOrderNotification();
    }

    @Test
    public void testMergeObservableForMergeCloseOK() {
        testMergeWithOrderEventType(OrderEventType.MERGE_CLOSE_OK);
        assertOrderNotification();
    }

    @Test
    public void testMergeObservableForMergeRejected() {
        testMergeWithOrderEventType(OrderEventType.MERGE_REJECTED);
        assertRejectExceptionNotification();
    }

    @Test
    public void testCloseWithExceptionObservableIsCorrect() {
        when(orderUtilMock.close(buyOrder)).thenReturn(changeExceptionResult);

        orderUtilObservable.close(buyOrder)
                .subscribe(orderSubscriber);

        verify(orderUtilMock).close(buyOrder);
        assertExceptionNotification();
    }

    @Test
    public void testCloseObservableForCloseOK() {
        testCloseWithOrderEventType(OrderEventType.CLOSE_OK);
        assertOrderNotification();
    }

    @Test
    public void testCloseObservableForCloseRejected() {
        testCloseWithOrderEventType(OrderEventType.CLOSE_REJECTED);
        assertRejectExceptionNotification();
    }

    @Test
    public void testSetLabelWithExceptionObservableIsCorrect() {
        when(orderUtilMock.setLabel(buyOrder, "")).thenReturn(changeExceptionResult);

        orderUtilObservable.setLabel(buyOrder, "")
                .subscribe(orderSubscriber);

        verify(orderUtilMock).setLabel(buyOrder, "");
        assertExceptionNotification();
    }

    @Test
    public void testChangeLabelObservableForOK() {
        testChangeLabelWithOrderEventType(OrderEventType.LABEL_CHANGE_OK);
        assertOrderNotification();
    }

    @Test
    public void testChangeLabelObservableForRejected() {
        testChangeLabelWithOrderEventType(OrderEventType.CHANGE_LABEL_REJECTED);
        assertRejectExceptionNotification();
    }

    @Test
    public void testSetGTTWithExceptionObservableIsCorrect() {
        buyOrder.setGoodTillTime(42L);
        when(orderUtilMock.setGTT(buyOrder, 24L)).thenReturn(changeExceptionResult);

        orderUtilObservable.setGTT(buyOrder, 24L)
                .subscribe(orderSubscriber);

        verify(orderUtilMock).setGTT(buyOrder, 24L);
        assertExceptionNotification();
    }

    @Test
    public void testChangeGTTObservableForOK() {
        buyOrder.setGoodTillTime(42L);

        testChangeGTTWithOrderEventType(OrderEventType.GTT_CHANGE_OK);
        assertOrderNotification();
    }

    @Test
    public void testChangeGTTObservableForRejected() {
        buyOrder.setGoodTillTime(42L);

        testChangeGTTWithOrderEventType(OrderEventType.CHANGE_GTT_REJECTED);
        assertRejectExceptionNotification();
    }

    @Test
    public void testSetOpenPriceWithExceptionObservableIsCorrect() {
        buyOrder.setOpenPrice(1.5432);
        when(orderUtilMock.setOpenPrice(buyOrder, 0)).thenReturn(changeExceptionResult);

        orderUtilObservable.setOpenPrice(buyOrder, 0)
                .subscribe(orderSubscriber);

        verify(orderUtilMock).setOpenPrice(buyOrder, 0);
        assertExceptionNotification();
    }

    @Test
    public void testChangeOpenPriceObservableForOK() {
        buyOrder.setOpenPrice(1.33421);

        testChangeOpenPriceWithOrderEventType(OrderEventType.PRICE_CHANGE_OK);
        assertOrderNotification();
    }

    @Test
    public void testChangeOpenPriceObservableForRejected() {
        buyOrder.setOpenPrice(1.33421);

        testChangeOpenPriceWithOrderEventType(OrderEventType.CHANGE_OPENPRICE_REJECTED);
        assertRejectExceptionNotification();
    }

    @Test
    public void testSetAmountWithExceptionObservableIsCorrect() {
        buyOrder.setRequestedAmount(0.21);
        when(orderUtilMock.setAmount(buyOrder, 0)).thenReturn(changeExceptionResult);

        orderUtilObservable.setAmount(buyOrder, 0)
                .subscribe(orderSubscriber);

        verify(orderUtilMock).setAmount(buyOrder, 0);
        assertExceptionNotification();
    }

    @Test
    public void testChangeAmountObservableForOK() {
        buyOrder.setRequestedAmount(0.12);

        testChangeAmountWithOrderEventType(OrderEventType.AMOUNT_CHANGE_OK);
        assertOrderNotification();
    }

    @Test
    public void testChangeAmountObservableForRejected() {
        buyOrder.setRequestedAmount(42.42);

        testChangeAmountWithOrderEventType(OrderEventType.CHANGE_AMOUNT_REJECTED);
        assertRejectExceptionNotification();
    }

    @Test
    public void testChangeSLWithExceptionObservableIsCorrect() {
        buyOrder.setStopLossPrice(1.4367);
        when(orderUtilMock.setSL(buyOrder, newSL)).thenReturn(changeExceptionResult);

        orderUtilObservable.setSL(buyOrder, newSL)
                .subscribe(orderSubscriber);

        verify(orderUtilMock).setSL(buyOrder, newSL);
        assertExceptionNotification();
    }

    @Test
    public void testChangeSLObservableForChangeSLOK() {
        buyOrder.setStopLossPrice(1.4321);

        testChangeSLWithOrderEventType(OrderEventType.SL_CHANGE_OK);
        assertOrderNotification();
    }

    @Test
    public void testChangeSLObservableForChangeSLRejected() {
        buyOrder.setStopLossPrice(1.4321);

        testChangeSLWithOrderEventType(OrderEventType.CHANGE_SL_REJECTED);
        assertRejectExceptionNotification();
    }

    @Test
    public void testChangeTPWithExceptionObservableIsCorrect() {
        when(orderUtilMock.setTP(buyOrder, newTP)).thenReturn(changeExceptionResult);

        orderUtilObservable.setTP(buyOrder, newTP)
                .subscribe(orderSubscriber);

        verify(orderUtilMock).setTP(buyOrder, newTP);
        assertExceptionNotification();
    }

    @Test
    public void testChangeSLObservableForChangeTPOK() {
        testChangeTPWithOrderEventType(OrderEventType.TP_CHANGE_OK);
        assertOrderNotification();
    }

    @Test
    public void testChangeSLObservableForChangeTPRejected() {
        testChangeTPWithOrderEventType(OrderEventType.CHANGE_TP_REJECTED);
        assertRejectExceptionNotification();
    }
}
