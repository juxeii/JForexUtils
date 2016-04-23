package com.jforex.programming.position.test;

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
import com.jforex.programming.order.OrderChangeResult;
import com.jforex.programming.order.OrderCreateResult;
import com.jforex.programming.order.OrderParams;
import com.jforex.programming.order.OrderUtil;
import com.jforex.programming.order.call.OrderCallRequest;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.position.PositionObservable;
import com.jforex.programming.test.common.InstrumentUtilForTest;
import com.jforex.programming.test.common.OrderParamsForTest;
import com.jforex.programming.test.fakes.IOrderForTest;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.JFException;

import rx.observers.TestSubscriber;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

public class PositionObservableTest extends InstrumentUtilForTest {

    private PositionObservable positionObservable;

    @Mock
    private OrderUtil orderUtilMock;
    private Subject<OrderEvent, OrderEvent> orderEventSubject;
    private final IOrderForTest buyOrder = IOrderForTest.buyOrderEURUSD();
    private final IOrderForTest sellOrder = IOrderForTest.sellOrderEURUSD();
    private final OrderParams orderParams = OrderParamsForTest.paramsBuyEURUSD();
    private final TestSubscriber<IOrder> orderSubscriber = new TestSubscriber<>();
    // private final TestSubscriber<OrderEvent> orderEventSubscriber = new
    // TestSubscriber<>();
    private OrderCreateResult submitOKResult;
    private OrderCreateResult submitExceptionResult;
    private OrderCreateResult mergeOKResult;
    private OrderCreateResult mergeExceptionResult;
    private OrderChangeResult changeOKResult;
    private OrderChangeResult changeOKResultSell;
    private OrderChangeResult changeExceptionResult;
    private final String mergeLabel = "MergeLabel";
    private Set<IOrder> orderSet;
    private final double newSL = askEURUSD;
    private final double newTP = bidEURUSD;

    @Before
    public void setUp() {
        initCommonTestFramework();
        orderEventSubject = PublishSubject.create();
        submitOKResult = new OrderCreateResult(Optional.of(buyOrder),
                                               Optional.empty(),
                                               OrderCallRequest.SUBMIT);
        submitExceptionResult = new OrderCreateResult(Optional.empty(),
                                                      Optional.of(jfException),
                                                      OrderCallRequest.SUBMIT);
        mergeOKResult = new OrderCreateResult(Optional.of(buyOrder),
                                              Optional.empty(),
                                              OrderCallRequest.MERGE);
        mergeExceptionResult = new OrderCreateResult(Optional.empty(),
                                                     Optional.of(jfException),
                                                     OrderCallRequest.MERGE);
        changeOKResult = new OrderChangeResult(buyOrder,
                                               Optional.empty(),
                                               OrderCallRequest.CHANGE_SL);
        changeOKResultSell = new OrderChangeResult(sellOrder,
                                                   Optional.empty(),
                                                   OrderCallRequest.CHANGE_SL);
        changeExceptionResult = new OrderChangeResult(buyOrder,
                                                      Optional.of(jfException),
                                                      OrderCallRequest.CHANGE_SL);
        orderSet = Sets.newHashSet(buyOrder, sellOrder);

        positionObservable = new PositionObservable(orderUtilMock, orderEventSubject);
    }

    private void assertExceptionNotification() {
        orderSubscriber.assertError(JFException.class);
        orderSubscriber.assertValueCount(0);
    }

    private void assertOrderNotification() {
        orderSubscriber.assertNoErrors();
        orderSubscriber.assertValueCount(1);
        assertThat(orderSubscriber.getOnNextEvents().get(0), equalTo(buyOrder));
    }

    private void testSubmitOKWithOrderEventType(final OrderEventType orderEventType) {
        when(orderUtilMock.submit(orderParams)).thenReturn(submitOKResult);
        final OrderEvent event = new OrderEvent(buyOrder, orderEventType);

        positionObservable.forSubmitAndServerReply(orderParams)
                .subscribe(orderSubscriber);
        orderEventSubject.onNext(event);

        verify(orderUtilMock).submit(orderParams);
        assertOrderNotification();
    }

    private void testMergeOKWithOrderEventType(final OrderEventType orderEventType) {
        when(orderUtilMock.merge(mergeLabel, orderSet)).thenReturn(mergeOKResult);
        final OrderEvent event = new OrderEvent(buyOrder, orderEventType);

        positionObservable.forMergeAndServerReply(mergeLabel, orderSet)
                .subscribe(orderSubscriber);
        orderEventSubject.onNext(event);

        verify(orderUtilMock).merge(mergeLabel, orderSet);
        assertOrderNotification();
    }

    private void testChangeSLOKWithOrderEventType(final OrderEventType orderEventType) {
        when(orderUtilMock.setSL(buyOrder, newSL)).thenReturn(changeOKResult);
        final OrderEvent event = new OrderEvent(buyOrder, orderEventType);

        positionObservable.forChangeSLAndServerReply(buyOrder, newSL)
                .subscribe(orderSubscriber);
        orderEventSubject.onNext(event);

        verify(orderUtilMock).setSL(buyOrder, newSL);
        assertOrderNotification();
    }

    private void testChangeTPOKWithOrderEventType(final OrderEventType orderEventType) {
        when(orderUtilMock.setTP(buyOrder, newTP)).thenReturn(changeOKResult);
        final OrderEvent event = new OrderEvent(buyOrder, orderEventType);

        positionObservable.forChangeTPAndServerReply(buyOrder, newTP)
                .subscribe(orderSubscriber);
        orderEventSubject.onNext(event);

        verify(orderUtilMock).setTP(buyOrder, newTP);
        assertOrderNotification();
    }

    private void testCloseOKWithOrderEventType(final OrderEventType orderEventType) {
        when(orderUtilMock.close(buyOrder)).thenReturn(changeOKResult);
        final OrderEvent event = new OrderEvent(buyOrder, orderEventType);

        positionObservable.forCloseAndServerReply(buyOrder)
                .subscribe(orderSubscriber);
        orderEventSubject.onNext(event);

        verify(orderUtilMock).close(buyOrder);
        assertOrderNotification();
    }

    @Test
    public void testSubmitWithExceptionObservableIsCorrect() {
        when(orderUtilMock.submit(orderParams)).thenReturn(submitExceptionResult);

        positionObservable.forSubmitAndServerReply(orderParams)
                .subscribe(orderSubscriber);

        verify(orderUtilMock).submit(orderParams);
        assertExceptionNotification();
    }

    @Test
    public void testSubmitObservableForFullFill() {
        testSubmitOKWithOrderEventType(OrderEventType.FULL_FILL_OK);
    }

    @Test
    public void testSubmitObservableForFillReject() {
        testSubmitOKWithOrderEventType(OrderEventType.FILL_REJECTED);
    }

    @Test
    public void testSubmitObservableForSubmitRejected() {
        testSubmitOKWithOrderEventType(OrderEventType.SUBMIT_REJECTED);
    }

    @Test
    public void testMergeWithExceptionObservableIsCorrect() {
        when(orderUtilMock.merge(mergeLabel, orderSet)).thenReturn(mergeExceptionResult);

        positionObservable.forMergeAndServerReply(mergeLabel, orderSet)
                .subscribe(orderSubscriber);

        verify(orderUtilMock).merge(mergeLabel, orderSet);
        assertExceptionNotification();
    }

    @Test
    public void testMergeObservableForMergeOK() {
        testMergeOKWithOrderEventType(OrderEventType.MERGE_OK);
    }

    @Test
    public void testMergeObservableForMergeCloseOK() {
        testMergeOKWithOrderEventType(OrderEventType.MERGE_CLOSE_OK);
    }

    @Test
    public void testMergeObservableForMergeRejected() {
        testMergeOKWithOrderEventType(OrderEventType.MERGE_REJECTED);
    }

    @Test
    public void testChangeSLWithExceptionObservableIsCorrect() {
        when(orderUtilMock.setSL(buyOrder, newSL)).thenReturn(changeExceptionResult);

        positionObservable.forChangeSLAndServerReply(buyOrder, newSL)
                .subscribe(orderSubscriber);

        verify(orderUtilMock).setSL(buyOrder, newSL);
        assertExceptionNotification();
    }

    @Test
    public void testChangeSLObservableForChangeSLOK() {
        testChangeSLOKWithOrderEventType(OrderEventType.SL_CHANGE_OK);
    }

    @Test
    public void testChangeSLObservableForChangeSLRejected() {
        testChangeSLOKWithOrderEventType(OrderEventType.CHANGE_SL_REJECTED);
    }

    @Test
    public void testChangeTPWithExceptionObservableIsCorrect() {
        when(orderUtilMock.setTP(buyOrder, newTP)).thenReturn(changeExceptionResult);

        positionObservable.forChangeTPAndServerReply(buyOrder, newTP)
                .subscribe(orderSubscriber);

        verify(orderUtilMock).setTP(buyOrder, newTP);
        assertExceptionNotification();
    }

    @Test
    public void testChangeSLObservableForChangeTPOK() {
        testChangeTPOKWithOrderEventType(OrderEventType.TP_CHANGE_OK);
    }

    @Test
    public void testChangeSLObservableForChangeTPRejected() {
        testChangeTPOKWithOrderEventType(OrderEventType.CHANGE_TP_REJECTED);
    }

    @Test
    public void testCloseWithExceptionObservableIsCorrect() {
        when(orderUtilMock.close(buyOrder)).thenReturn(changeExceptionResult);

        positionObservable.forCloseAndServerReply(buyOrder)
                .subscribe(orderSubscriber);

        verify(orderUtilMock).close(buyOrder);
        assertExceptionNotification();
    }

    @Test
    public void testCloseObservableForCloseOK() {
        testCloseOKWithOrderEventType(OrderEventType.CLOSE_OK);
    }

    @Test
    public void testCloseObservableForCloseRejected() {
        testCloseOKWithOrderEventType(OrderEventType.CLOSE_REJECTED);
    }

    @Test
    public void testBatchChangeSLIsCorrect() {
        when(orderUtilMock.setSL(buyOrder, newSL)).thenReturn(changeOKResult);
        when(orderUtilMock.setSL(sellOrder, newSL)).thenReturn(changeOKResultSell);
        final OrderEvent eventForBuyOrder = new OrderEvent(buyOrder, OrderEventType.SL_CHANGE_OK);
        final OrderEvent eventForSellOrder = new OrderEvent(sellOrder, OrderEventType.SL_CHANGE_OK);

        positionObservable.batchChangeSL(orderSet, newSL)
                .subscribe(orderSubscriber);
        orderEventSubject.onNext(eventForBuyOrder);
        orderEventSubject.onNext(eventForSellOrder);

        verify(orderUtilMock).setSL(buyOrder, newSL);
        verify(orderUtilMock).setSL(sellOrder, newSL);

        orderSubscriber.assertNoErrors();
        orderSubscriber.assertValueCount(2);
        assertThat(orderSubscriber.getOnNextEvents().get(0),
                   equalTo(buyOrder));
        assertThat(orderSubscriber.getOnNextEvents().get(1),
                   equalTo(sellOrder));
    }

    @Test
    public void testBatchChangeTPIsCorrect() {
        when(orderUtilMock.setTP(buyOrder, newTP)).thenReturn(changeOKResult);
        when(orderUtilMock.setTP(sellOrder, newTP)).thenReturn(changeOKResultSell);
        final OrderEvent eventForBuyOrder = new OrderEvent(buyOrder, OrderEventType.TP_CHANGE_OK);
        final OrderEvent eventForSellOrder = new OrderEvent(sellOrder, OrderEventType.TP_CHANGE_OK);

        positionObservable.batchChangeTP(orderSet, newTP)
                .subscribe(orderSubscriber);
        orderEventSubject.onNext(eventForBuyOrder);
        orderEventSubject.onNext(eventForSellOrder);

        verify(orderUtilMock).setTP(buyOrder, newTP);
        verify(orderUtilMock).setTP(sellOrder, newTP);

        orderSubscriber.assertNoErrors();
        orderSubscriber.assertValueCount(2);
        assertThat(orderSubscriber.getOnNextEvents().get(0),
                   equalTo(buyOrder));
        assertThat(orderSubscriber.getOnNextEvents().get(1),
                   equalTo(sellOrder));
    }
}
