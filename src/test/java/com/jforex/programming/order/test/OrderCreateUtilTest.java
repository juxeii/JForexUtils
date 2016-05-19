package com.jforex.programming.order.test;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.function.Supplier;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.JFException;
import com.jforex.programming.order.OrderCreateUtil;
import com.jforex.programming.order.OrderParams;
import com.jforex.programming.order.call.OrderCallExecutor;
import com.jforex.programming.order.call.OrderCallExecutorResult;
import com.jforex.programming.order.call.OrderCallRejectException;
import com.jforex.programming.order.call.OrderCallRequest;
import com.jforex.programming.order.call.OrderSupplierCall;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventGateway;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.test.common.InstrumentUtilForTest;
import com.jforex.programming.test.common.OrderParamsForTest;
import com.jforex.programming.test.fakes.IOrderForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import rx.Observable;
import rx.observers.TestSubscriber;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

@RunWith(HierarchicalContextRunner.class)
public class OrderCreateUtilTest extends InstrumentUtilForTest {

    private OrderCreateUtil orderCreateUtil;

    @Mock
    private OrderCallExecutor orderCallExecutorMock;
    @Mock
    private OrderEventGateway orderEventGatewayMock;
    @Captor
    private ArgumentCaptor<OrderSupplierCall> orderCallCaptor;
    private Subject<OrderEvent, OrderEvent> orderEventSubject = PublishSubject.create();
    private final IOrderForTest orderUnderTest = IOrderForTest.buyOrderEURUSD();
    private OrderCallExecutorResult orderExecutorResult;
    private OrderCallExecutorResult orderExecutorResultWithJFException;

    @Before
    public void setUp() {
        initCommonTestFramework();

        orderEventSubject = PublishSubject.create();
        orderExecutorResult = new OrderCallExecutorResult(Optional.of(orderUnderTest),
                                                          Optional.empty());
        orderExecutorResultWithJFException = new OrderCallExecutorResult(Optional.empty(),
                                                                         Optional.of(jfException));
        setUpMocks();

        orderCreateUtil = new OrderCreateUtil(engineMock,
                                              orderCallExecutorMock,
                                              orderEventGatewayMock);
    }

    private void setUpMocks() {
        when(orderEventGatewayMock.observable()).thenReturn(orderEventSubject);
        when(orderCallExecutorMock.run(any(OrderSupplierCall.class))).thenReturn(orderExecutorResult);
    }

    private void prepareJFException() {
        when(orderCallExecutorMock.run(any(OrderSupplierCall.class)))
                .thenReturn(orderExecutorResultWithJFException);
    }

    private void captureAndRunOrderCall() throws JFException {
        verify(orderCallExecutorMock).run(orderCallCaptor.capture());
        orderCallCaptor.getValue().get();
    }

    private void assertJFException(final TestSubscriber<?> subscriber) {
        subscriber.assertValueCount(0);
        subscriber.assertError(JFException.class);
    }

    private void assertOtherOrderIsIgnored(final TestSubscriber<OrderEvent> subscriber) {
        orderEventSubject.onNext(new OrderEvent(IOrderForTest.orderAUDUSD(), OrderEventType.MERGE_OK));
        subscriber.assertNoErrors();
        subscriber.assertNotCompleted();
    }

    private void sendOrderEvent(final IOrder order,
                                final OrderEventType orderEventType) {
        orderEventSubject.onNext(new OrderEvent(order, orderEventType));
    }

    private void assertSubscriberCompleted(final TestSubscriber<?> subscriber) {
        subscriber.assertNoErrors();
        subscriber.assertCompleted();
    }

    public class BuySubmitSetup {

        private final IOrderForTest buyOrder = IOrderForTest.buyOrderEURUSD();
        private final OrderParams orderParamsBUY = OrderParamsForTest.paramsBuyEURUSD();
        private final TestSubscriber<OrderEvent> buySubmitSubscriber = new TestSubscriber<>();
        private Observable<OrderEvent> buyObservable;
        private final Supplier<Observable<OrderEvent>> buySubmitCall =
                () -> buyObservable = orderCreateUtil.submitOrder(orderParamsBUY);

        public class JFExceptionOnSubmit {

            @Before
            public void setUp() {
                prepareJFException();

                buySubmitCall.get();
            }

            @Test
            public void testSubmitCallsOnIEngine() throws JFException {
                captureAndRunOrderCall();

                engineForTest.verifySubmit(orderParamsBUY, 1);
            }

            @Test
            public void testBuySubscriberCompletedWithError() {
                buyObservable.subscribe(buySubmitSubscriber);

                assertJFException(buySubmitSubscriber);
            }
        }

        public class SubmitCall {

            @Before
            public void setUp() {
                final OrderCallExecutorResult orderExecutorResult =
                        new OrderCallExecutorResult(Optional.of(buyOrder), Optional.empty());
                when(orderCallExecutorMock.run(any(OrderSupplierCall.class)))
                        .thenReturn(orderExecutorResult);

                buySubmitCall.get();

                buyOrder.setState(IOrder.State.CREATED);
            }

            @Test
            public void testSubmitCallsOnIEngine() throws JFException {
                captureAndRunOrderCall();

                engineForTest.verifySubmit(orderParamsBUY, 1);
            }

            @Test
            public void testBuyOrderIsRegisteredOnEventGateway() {
                verify(orderEventGatewayMock).registerOrderRequest(buyOrder, OrderCallRequest.SUBMIT);
            }

            @Test
            public void testSubmitObservableIgnoresOtherOrders() {
                assertOtherOrderIsIgnored(buySubmitSubscriber);
            }

            public class WhenSubmitRejected {

                @Before
                public void setUp() {
                    buyOrder.setState(IOrder.State.CANCELED);

                    sendOrderEvent(buyOrder, OrderEventType.SUBMIT_REJECTED);
                }

                @Test
                public void testBuySubscriberCompletedWithRejection() {
                    buyObservable.subscribe(buySubmitSubscriber);

                    buySubmitSubscriber.assertError(OrderCallRejectException.class);
                }

                @Test
                public void testNoRetryIsDone() throws JFException {
                    captureAndRunOrderCall();

                    engineForTest.verifySubmit(orderParamsBUY, 1);
                }
            }

            public class WhenFillRejected {

                @Before
                public void setUp() {
                    buyOrder.setState(IOrder.State.CANCELED);

                    sendOrderEvent(buyOrder, OrderEventType.FILL_REJECTED);
                }

                @Test
                public void testBuySubscriberCompletedWithRejection() {
                    buyObservable.subscribe(buySubmitSubscriber);

                    buySubmitSubscriber.assertError(OrderCallRejectException.class);
                }

                @Test
                public void testNoRetryIsDone() throws JFException {
                    captureAndRunOrderCall();

                    engineForTest.verifySubmit(orderParamsBUY, 1);
                }
            }

            @Test
            public void testOnSubmitDone() {
                buyObservable.subscribe(buySubmitSubscriber);

                sendOrderEvent(buyOrder, OrderEventType.SUBMIT_OK);

                buySubmitSubscriber.assertNoErrors();
                buySubmitSubscriber.assertNotCompleted();
            }

            @Test
            public void testOnFilled() {
                buyOrder.setState(IOrder.State.FILLED);
                buyObservable.subscribe(buySubmitSubscriber);

                sendOrderEvent(buyOrder, OrderEventType.FULL_FILL_OK);

                assertSubscriberCompleted(buySubmitSubscriber);
            }
        }
    }
}
