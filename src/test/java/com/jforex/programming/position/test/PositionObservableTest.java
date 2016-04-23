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
    private final TestSubscriber<OrderEvent> orderEventSubscriber = new TestSubscriber<>();
    private OrderCreateResult submitOKResult;
    private OrderCreateResult submitExceptionResult;
    private OrderCreateResult mergeOKResult;
    private OrderCreateResult mergeExceptionResult;
    private OrderChangeResult changeOKResult;
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
        changeExceptionResult = new OrderChangeResult(buyOrder,
                                                      Optional.of(jfException),
                                                      OrderCallRequest.CHANGE_SL);
        orderSet = Sets.newHashSet(buyOrder, sellOrder);

        positionObservable = new PositionObservable(orderUtilMock, orderEventSubject);
    }

    private void assertExceptionNotification() {
        orderEventSubscriber.assertError(JFException.class);
        orderEventSubscriber.assertValueCount(0);
    }

    private void assertOrderEventNotification(final OrderEvent orderEvent) {
        orderEventSubscriber.assertNoErrors();
        orderEventSubscriber.assertValueCount(1);
        assertThat(orderEventSubscriber.getOnNextEvents().get(0), equalTo(orderEvent));
    }

    private void testSubmitOKWithOrderEventType(final OrderEventType orderEventType) {
        when(orderUtilMock.submit(orderParams)).thenReturn(submitOKResult);
        final OrderEvent event = new OrderEvent(buyOrder, orderEventType);

        positionObservable.forSubmitAndServerReply(orderParams)
                          .subscribe(orderEventSubscriber);
        orderEventSubject.onNext(event);

        verify(orderUtilMock).submit(orderParams);
        assertOrderEventNotification(event);
    }

    private void testMergeOKWithOrderEventType(final OrderEventType orderEventType) {
        when(orderUtilMock.merge(mergeLabel, orderSet)).thenReturn(mergeOKResult);
        final OrderEvent event = new OrderEvent(buyOrder, orderEventType);

        positionObservable.forMergeAndServerReply(mergeLabel, orderSet)
                          .subscribe(orderEventSubscriber);
        orderEventSubject.onNext(event);

        verify(orderUtilMock).merge(mergeLabel, orderSet);
        assertOrderEventNotification(event);
    }

    private void testChangeSLOKWithOrderEventType(final OrderEventType orderEventType) {
        when(orderUtilMock.setSL(buyOrder, newSL)).thenReturn(changeOKResult);
        final OrderEvent event = new OrderEvent(buyOrder, orderEventType);

        positionObservable.forChangeSLAndServerReply(buyOrder, newSL)
                          .subscribe(orderEventSubscriber);
        orderEventSubject.onNext(event);

        verify(orderUtilMock).setSL(buyOrder, newSL);
        assertOrderEventNotification(event);
    }

    private void testChangeTPOKWithOrderEventType(final OrderEventType orderEventType) {
        when(orderUtilMock.setTP(buyOrder, newTP)).thenReturn(changeOKResult);
        final OrderEvent event = new OrderEvent(buyOrder, orderEventType);

        positionObservable.forChangeTPAndServerReply(buyOrder, newTP)
                          .subscribe(orderEventSubscriber);
        orderEventSubject.onNext(event);

        verify(orderUtilMock).setTP(buyOrder, newTP);
        assertOrderEventNotification(event);
    }

    private void testCloseOKWithOrderEventType(final OrderEventType orderEventType) {
        when(orderUtilMock.close(buyOrder)).thenReturn(changeOKResult);
        final OrderEvent event = new OrderEvent(buyOrder, orderEventType);

        positionObservable.forCloseAndServerReply(buyOrder)
                          .subscribe(orderEventSubscriber);
        orderEventSubject.onNext(event);

        verify(orderUtilMock).close(buyOrder);
        assertOrderEventNotification(event);
    }

    @Test
    public void testSubmitWithExceptionObservableIsCorrect() {
        when(orderUtilMock.submit(orderParams)).thenReturn(submitExceptionResult);

        positionObservable.forSubmitAndServerReply(orderParams)
                          .subscribe(orderEventSubscriber);

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
                          .subscribe(orderEventSubscriber);

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
                          .subscribe(orderEventSubscriber);

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
                          .subscribe(orderEventSubscriber);

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
                          .subscribe(orderEventSubscriber);

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
}
