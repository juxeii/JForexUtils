package com.jforex.programming.order.test;

import static info.solidsoft.mockito.java8.LambdaMatcher.argLambda;

import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import com.google.common.collect.Sets;
import com.jforex.programming.order.OrderUtilHandler;
import com.jforex.programming.order.call.OrderCallExecutor;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.call.OrderCallRejectException;
import com.jforex.programming.order.command.MergeCommand;
import com.jforex.programming.order.command.OrderCallCommand;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventGateway;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.test.common.InstrumentUtilForTest;
import com.jforex.programming.test.fakes.IOrderForTest;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.JFException;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import rx.Observable;
import rx.observers.TestSubscriber;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

@RunWith(HierarchicalContextRunner.class)
public class OrderUtilHandlerTest extends InstrumentUtilForTest {

    private OrderUtilHandler orderUtilHandler;

    @Mock
    private OrderCallExecutor orderCallExecutorMock;
    @Mock
    private OrderEventGateway orderEventGatewayMock;
    @Captor
    private ArgumentCaptor<Callable<IOrder>> orderCallCaptor;
    private final IOrderForTest externalOrder = IOrderForTest.orderAUDUSD();
    private final Subject<OrderEvent, OrderEvent> orderEventSubject = PublishSubject.create();

    @Before
    public void setUp() {
        initCommonTestFramework();
        setUpMocks();

        orderUtilHandler = new OrderUtilHandler(orderCallExecutorMock, orderEventGatewayMock);
    }

    private void setUpMocks() {
        when(orderEventGatewayMock.observable()).thenReturn(orderEventSubject);
    }

    private void prepareJFException() {
        when(orderCallExecutorMock.callObservable(any())).thenReturn(Observable.error(jfException));
    }

    private void captureAndRunOrderCall() throws Exception {
        verify(orderCallExecutorMock).callObservable(orderCallCaptor.capture());
        orderCallCaptor.getValue().call();
    }

    private void assertSubscriberJFError(final TestSubscriber<?> subscriber) {
        subscriber.assertValueCount(0);
        subscriber.assertError(JFException.class);
    }

    private void assertSubscriberRejectError(final TestSubscriber<?> subscriber) {
        subscriber.assertValueCount(0);
        subscriber.assertError(OrderCallRejectException.class);
    }

    private void sendOrderEvent(final IOrder order,
                                final OrderEventType orderEventType) {
        orderEventSubject.onNext(new OrderEvent(order, orderEventType));
    }

    private void assertSubscriberCompleted(final TestSubscriber<?> subscriber) {
        subscriber.assertNoErrors();
        subscriber.assertCompleted();
    }

    public class OrderMergeCallSetup {

        private final IOrderForTest mergeOrder = IOrderForTest.buyOrderEURUSD();
        private final Set<IOrder> mergeOrders = Sets.newHashSet(IOrderForTest.buyOrderEURUSD(),
                                                                IOrderForTest.sellOrderEURUSD());
        private final String mergeOrderLabel = "MergeLabel";
        private final TestSubscriber<OrderEvent> callSubscriber = new TestSubscriber<>();
        private final MergeCommand command = new MergeCommand(mergeOrderLabel, mergeOrders, engineMock);
        private final Supplier<Observable<OrderEvent>> runCall =
                () -> orderUtilHandler.observable(command);

        @Before
        public void setUp() {
            when(orderCallExecutorMock.callObservable(any()))
                    .thenReturn(Observable.just(mergeOrder));
        }

        public class ExecutesWithJFException {

            @Before
            public void setUp() {
                prepareJFException();

                runCall.get().subscribe(callSubscriber);
            }

            @Test
            public void testCorrectCallIsExecuted() throws Exception {
//                captureAndRunOrderCall();
//
//                verify(engineMock).mergeOrders(mergeOrderLabel, mergeOrders);
            }

            @Test
            public void testSubscriberCompletesWithJFError() {
                assertSubscriberJFError(callSubscriber);
            }

            @Test
            public void testMergeOrderIsNotRegisteredAtGateway() {
                verify(orderEventGatewayMock, never()).registerOrderCallRequest(any());
            }
        }

        public class ExecutesOK {

            @Before
            public void setUp() {
                runCall.get().subscribe(callSubscriber);
            }

            @Test
            public void testCorrectCallIsExecuted() throws Exception {
//                captureAndRunOrderCall();
//
//                verify(engineMock).mergeOrders(mergeOrderLabel, mergeOrders);
            }

            @Test
            public void testSubscriberNotYetCompletedWhenNoEventWasSent() {
                callSubscriber.assertNotCompleted();
            }

            @Test
            public void testMergeOrderRegisteredAtGateway() {
                verify(orderEventGatewayMock)
                        .registerOrderCallRequest(argLambda(req -> req.order() == mergeOrder
                                && req.reason() == OrderCallReason.MERGE));
            }

            @Test
            public void testSubscriberNotCompletedWhenNotDoneType() {
                sendOrderEvent(mergeOrder, OrderEventType.SUBMIT_OK);

                callSubscriber.assertNotCompleted();
            }

            @Test
            public void testSubscriberCompletesOnDoneEvent() {
                sendOrderEvent(mergeOrder, OrderEventType.MERGE_OK);

                assertSubscriberCompleted(callSubscriber);
            }

            @Test
            public void testSubscriberCompletesWithRejectError() {
                sendOrderEvent(mergeOrder, OrderEventType.MERGE_REJECTED);

                assertSubscriberRejectError(callSubscriber);
            }

            @Test
            public void testOtherOrderEventIsIgnored() {
                sendOrderEvent(externalOrder, OrderEventType.MERGE_OK);

                callSubscriber.assertNotCompleted();
            }

            @Test
            public void testNotCallRelatedEventIsIgnored() {
                sendOrderEvent(mergeOrder, OrderEventType.CHANGED_GTT);

                callSubscriber.assertNotCompleted();
            }
        }
    }

    public class OrderChangeCallSetup {

        private final IOrderForTest orderToChange = IOrderForTest.buyOrderEURUSD();
        private final TestSubscriber<OrderEvent> callSubscriber = new TestSubscriber<>();
        private final OrderCallCommand command = OrderCallCommand.closeCommand(orderToChange);
        private final Supplier<Observable<OrderEvent>> runCall =
                () -> orderUtilHandler.observable(command);

        @Before
        public void setUp() {
            when(orderCallExecutorMock.callObservable(any()))
                    .thenReturn(Observable.just(orderToChange));
        }

        public class ExecutesWithJFException {

            @Before
            public void setUp() {
                prepareJFException();

                runCall.get().subscribe(callSubscriber);
            }

            @Test
            public void testCorrectCallIsExecuted() throws Exception {
                captureAndRunOrderCall();

                verify(orderToChange).close();
            }

            @Test
            public void testSubscriberCompletesWithJFError() {
                assertSubscriberJFError(callSubscriber);
            }

            @Test
            public void testMergeOrderIsNotRegisteredAtGateway() {
                verify(orderEventGatewayMock, never()).registerOrderCallRequest(any());
            }
        }

        public class ExecutesOK {

            @Before
            public void setUp() {
                runCall.get().subscribe(callSubscriber);
            }

            @Test
            public void testCorrectCallIsExecuted() throws Exception {
                captureAndRunOrderCall();

                verify(orderToChange).close();
            }

            @Test
            public void testSubscriberNotYetCompletedWhenNoEventWasSent() {
                callSubscriber.assertNotCompleted();
            }

            @Test
            public void testMergeOrderRegisteredAtGateway() {
                verify(orderEventGatewayMock)
                        .registerOrderCallRequest(argLambda(req -> req.order() == orderToChange
                                && req.reason() == OrderCallReason.CLOSE));
            }

            @Test
            public void testSubscriberCompletesOnDoneEvent() {
                sendOrderEvent(orderToChange, OrderEventType.CLOSE_OK);

                assertSubscriberCompleted(callSubscriber);
            }

            @Test
            public void testSubscriberCompletesWithRejectError() {
                sendOrderEvent(orderToChange, OrderEventType.CLOSE_REJECTED);

                assertSubscriberRejectError(callSubscriber);
            }

            @Test
            public void testOtherOrderEventIsIgnored() {
                sendOrderEvent(externalOrder, OrderEventType.CLOSE_OK);

                callSubscriber.assertNotCompleted();
            }

            @Test
            public void testNotCallRelatedEventIsIgnored() {
                sendOrderEvent(orderToChange, OrderEventType.CHANGED_GTT);

                callSubscriber.assertNotCompleted();
            }
        }
    }
}
