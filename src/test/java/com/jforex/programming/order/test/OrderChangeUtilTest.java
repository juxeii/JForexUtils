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
import com.jforex.programming.order.OrderChangeUtil;
import com.jforex.programming.order.call.OrderCallExecutor;
import com.jforex.programming.order.call.OrderCallExecutorResult;
import com.jforex.programming.order.call.OrderCallRejectException;
import com.jforex.programming.order.call.OrderCallRequest;
import com.jforex.programming.order.call.OrderSupplierCall;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventGateway;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.test.common.InstrumentUtilForTest;
import com.jforex.programming.test.fakes.IOrderForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import rx.Observable;
import rx.observers.TestSubscriber;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

@RunWith(HierarchicalContextRunner.class)
public class OrderChangeUtilTest extends InstrumentUtilForTest {

    private OrderChangeUtil orderChangeUtil;

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
        orderUnderTest.setState(IOrder.State.FILLED);

        orderChangeUtil = new OrderChangeUtil(orderCallExecutorMock, orderEventGatewayMock);
    }

    private void setUpMocks() {
        when(orderEventGatewayMock.observable()).thenReturn(orderEventSubject);
        when(orderCallExecutorMock.run(any(OrderSupplierCall.class))).thenReturn(orderExecutorResult);
    }

    private void prepareJFException() {
        when(orderCallExecutorMock.run(any(OrderSupplierCall.class)))
                .thenReturn(orderExecutorResultWithJFException);
    }

    private void assertJFException(final TestSubscriber<?> subscriber) {
        subscriber.assertValueCount(0);
        subscriber.assertError(JFException.class);
    }

    private void assertRejectException(final TestSubscriber<?> subscriber) {
        subscriber.assertValueCount(0);
        subscriber.assertError(OrderCallRejectException.class);
    }

    private void assertSubscriberCompleted(final TestSubscriber<?> subscriber) {
        subscriber.assertNoErrors();
        subscriber.assertCompleted();
    }

    private void captureAndRunOrderCall() throws JFException {
        verify(orderCallExecutorMock).run(orderCallCaptor.capture());
        orderCallCaptor.getValue().get();
    }

    private void assertOtherOrderIsIgnored(final TestSubscriber<OrderEvent> subscriber) {
        orderEventSubject.onNext(new OrderEvent(IOrderForTest.orderAUDUSD(), OrderEventType.MERGE_OK));
        subscriber.assertNoErrors();
        subscriber.assertNotCompleted();
    }

    private void sendOrderEvent(final OrderEventType orderEventType) {
        orderEventSubject.onNext(new OrderEvent(orderUnderTest, orderEventType));
    }

    private void assertJFException(final Supplier<Observable<OrderEvent>> orderCall) {
        prepareJFException();

        final TestSubscriber<OrderEvent> subscriber = new TestSubscriber<>();
        final Observable<OrderEvent> eventObservable = orderCall.get();

        eventObservable.subscribe(subscriber);
        assertJFException(subscriber);
    }

    private void verifyGatewayRegister(final OrderCallRequest orderCallRequest) {
        verify(orderEventGatewayMock).registerOrderRequest(orderUnderTest, orderCallRequest);
    }

    public class CloseSetup {

        private final TestSubscriber<OrderEvent> closeSubscriber = new TestSubscriber<>();
        private Observable<OrderEvent> closeObservable;

        @Test
        public void testOnJFException() {
            assertJFException(() -> orderChangeUtil.close(orderUnderTest));
        }

        public class CloseCall {

            @Before
            public void setUp() {
                closeObservable = orderChangeUtil.close(orderUnderTest);
            }

            @Test
            public void testCloseCallsOnOrder() throws JFException {
                captureAndRunOrderCall();

                verify(orderUnderTest).close();
            }

            @Test
            public void testCloseRegistersOnEventGateway() {
                verifyGatewayRegister(OrderCallRequest.CLOSE);
            }

            @Test
            public void testCloseObservableIgnoresOtherOrders() {
                closeObservable.subscribe(closeSubscriber);

                assertOtherOrderIsIgnored(closeSubscriber);
            }

            public class WhenCloseRejected {

                @Before
                public void setUp() {
                    sendOrderEvent(OrderEventType.CLOSE_REJECTED);
                }

                @Test
                public void testCloseSubscriberCompletedWithRejection() {
                    closeObservable.subscribe(closeSubscriber);

                    assertRejectException(closeSubscriber);
                }

                @Test
                public void testNoRetryIsDone() throws JFException {
                    captureAndRunOrderCall();

                    verify(orderUnderTest).close();
                }
            }

            public class WhenClosed {

                @Before
                public void setUp() {
                    orderUnderTest.setState(IOrder.State.CLOSED);

                    sendOrderEvent(OrderEventType.CLOSE_OK);
                }

                @Test
                public void testCloseSubscriberCompleted() {
                    closeObservable.subscribe(closeSubscriber);

                    assertSubscriberCompleted(closeSubscriber);
                }
            }
        }
    }

    public class SetLabelSetup {

        private final TestSubscriber<OrderEvent> setLabelSubscriber = new TestSubscriber<>();
        private Observable<OrderEvent> setLabelObservable;

        @Test
        public void testOnJFException() {
            assertJFException(() -> orderChangeUtil.setLabel(orderUnderTest, newLabel));
        }

        public class SetLabelCall {

            @Before
            public void setUp() {
                setLabelObservable = orderChangeUtil.setLabel(orderUnderTest, newLabel);
            }

            @Test
            public void testSetLabelCallsOnOrder() throws JFException {
                captureAndRunOrderCall();

                verify(orderUnderTest).setLabel(newLabel);
            }

            @Test
            public void testSetLabelRegistersOnEventGateway() {
                verifyGatewayRegister(OrderCallRequest.CHANGE_LABEL);
            }

            @Test
            public void testSetLabelObservableIgnoresOtherOrders() {
                setLabelObservable.subscribe(setLabelSubscriber);

                assertOtherOrderIsIgnored(setLabelSubscriber);
            }

            public class WhenSetLabelRejected {

                @Before
                public void setUp() {
                    sendOrderEvent(OrderEventType.CHANGE_LABEL_REJECTED);
                }

                @Test
                public void testSetLabelSubscriberCompletedWithRejection() {
                    setLabelObservable.subscribe(setLabelSubscriber);

                    assertRejectException(setLabelSubscriber);
                }

                @Test
                public void testNoRetryIsDone() throws JFException {
                    captureAndRunOrderCall();

                    verify(orderUnderTest).setLabel(newLabel);
                }
            }

            public class WhenSetLabelOK {

                @Before
                public void setUp() {
                    sendOrderEvent(OrderEventType.LABEL_CHANGE_OK);
                }

                @Test
                public void testGTTSubscriberCompleted() {
                    setLabelObservable.subscribe(setLabelSubscriber);

                    assertSubscriberCompleted(setLabelSubscriber);
                }
            }
        }

        public class SetGTTSetup {

            private final TestSubscriber<OrderEvent> setGTTSubscriber = new TestSubscriber<>();
            private Observable<OrderEvent> setGTTObservable;

            @Test
            public void testOnJFException() {
                assertJFException(() -> orderChangeUtil.setGoodTillTime(orderUnderTest, newGTT));
            }

            public class SetGTTCall {

                @Before
                public void setUp() {
                    setGTTObservable = orderChangeUtil.setGoodTillTime(orderUnderTest, newGTT);
                }

                @Test
                public void testSetGTTCallsOnOrder() throws JFException {
                    captureAndRunOrderCall();

                    verify(orderUnderTest).setGoodTillTime(newGTT);
                }

                @Test
                public void testSetGTTRegistersOnEventGateway() {
                    verifyGatewayRegister(OrderCallRequest.CHANGE_GTT);
                }

                @Test
                public void testSetGTTObservableIgnoresOtherOrders() {
                    setGTTObservable.subscribe(setGTTSubscriber);

                    assertOtherOrderIsIgnored(setGTTSubscriber);
                }

                public class WhenSetGTTRejected {

                    @Before
                    public void setUp() {
                        sendOrderEvent(OrderEventType.CHANGE_GTT_REJECTED);
                    }

                    @Test
                    public void testSetGTTSubscriberCompletedWithRejection() {
                        setGTTObservable.subscribe(setGTTSubscriber);

                        assertRejectException(setGTTSubscriber);
                    }

                    @Test
                    public void testNoRetryIsDone() throws JFException {
                        captureAndRunOrderCall();

                        verify(orderUnderTest).setGoodTillTime(newGTT);
                    }
                }

                public class WhenSetGTTOK {

                    @Before
                    public void setUp() {
                        sendOrderEvent(OrderEventType.GTT_CHANGE_OK);
                    }

                    @Test
                    public void testGTTSubscriberCompleted() {
                        setGTTObservable.subscribe(setGTTSubscriber);

                        assertSubscriberCompleted(setGTTSubscriber);
                    }
                }
            }
        }

        public class SetOpenPriceSetup {

            private final TestSubscriber<OrderEvent> setOpenPriceSubscriber = new TestSubscriber<>();
            private Observable<OrderEvent> setOpenPriceObservable;

            @Test
            public void testOnJFException() {
                assertJFException(() -> orderChangeUtil.setOpenPrice(orderUnderTest, newOpenPrice));
            }

            public class SetOpenPriceCall {

                @Before
                public void setUp() {
                    setOpenPriceObservable = orderChangeUtil.setOpenPrice(orderUnderTest, newOpenPrice);
                }

                @Test
                public void testSetOpenPriceCallsOnOrder() throws JFException {
                    captureAndRunOrderCall();

                    verify(orderUnderTest).setOpenPrice(newOpenPrice);
                }

                @Test
                public void testSetOpenPriceRegistersOnEventGateway() {
                    verifyGatewayRegister(OrderCallRequest.CHANGE_OPENPRICE);
                }

                @Test
                public void testSetOpenPriceObservableIgnoresOtherOrders() {
                    setOpenPriceObservable.subscribe(setOpenPriceSubscriber);

                    assertOtherOrderIsIgnored(setOpenPriceSubscriber);
                }

                public class WhenSetOpenPriceRejected {

                    @Before
                    public void setUp() {
                        sendOrderEvent(OrderEventType.CHANGE_OPENPRICE_REJECTED);
                    }

                    @Test
                    public void testSetOpenPriceSubscriberCompletedWithRejection() {
                        setOpenPriceObservable.subscribe(setOpenPriceSubscriber);

                        assertRejectException(setOpenPriceSubscriber);
                    }

                    @Test
                    public void testNoRetryIsDone() throws JFException {
                        captureAndRunOrderCall();

                        verify(orderUnderTest).setOpenPrice(newOpenPrice);
                    }
                }

                public class WhenSetOpenPriceOK {

                    @Before
                    public void setUp() {
                        sendOrderEvent(OrderEventType.OPENPRICE_CHANGE_OK);
                    }

                    @Test
                    public void testOpenPriceSubscriberCompleted() {
                        setOpenPriceObservable.subscribe(setOpenPriceSubscriber);

                        assertSubscriberCompleted(setOpenPriceSubscriber);
                    }
                }
            }
        }

        public class SetRequestedAmountSetup {

            private final TestSubscriber<OrderEvent> setRequestedAmountSubscriber = new TestSubscriber<>();
            private Observable<OrderEvent> setRequestedAmountObservable;

            @Test
            public void testOnJFException() {
                assertJFException(() -> orderChangeUtil.setRequestedAmount(orderUnderTest, newAmount));
            }

            public class SetRequestedAmountCall {

                @Before
                public void setUp() {
                    setRequestedAmountObservable = orderChangeUtil.setRequestedAmount(orderUnderTest, newAmount);
                }

                @Test
                public void testSetRequestedAmountCallsOnOrder() throws JFException {
                    captureAndRunOrderCall();

                    verify(orderUnderTest).setRequestedAmount(newAmount);
                }

                @Test
                public void testSetRequestedAmountRegistersOnEventGateway() {
                    verifyGatewayRegister(OrderCallRequest.CHANGE_REQUESTED_AMOUNT);
                }

                @Test
                public void testSetRequestedAmountObservableIgnoresOtherOrders() {
                    setRequestedAmountObservable.subscribe(setRequestedAmountSubscriber);

                    assertOtherOrderIsIgnored(setRequestedAmountSubscriber);
                }

                public class WhenSetRequestedAmountRejected {

                    @Before
                    public void setUp() {
                        sendOrderEvent(OrderEventType.CHANGE_AMOUNT_REJECTED);
                    }

                    @Test
                    public void testSetRequestedAmountSubscriberCompletedWithRejection() {
                        setRequestedAmountObservable.subscribe(setRequestedAmountSubscriber);

                        assertRejectException(setRequestedAmountSubscriber);
                    }

                    @Test
                    public void testNoRetryIsDone() throws JFException {
                        captureAndRunOrderCall();

                        verify(orderUnderTest).setRequestedAmount(newAmount);
                    }
                }

                public class WhenSetRequestedAmountPriceOK {

                    @Before
                    public void setUp() {
                        sendOrderEvent(OrderEventType.AMOUNT_CHANGE_OK);
                    }

                    @Test
                    public void testSetRequestedAmountSubscriberCompleted() {
                        setRequestedAmountObservable.subscribe(setRequestedAmountSubscriber);

                        assertSubscriberCompleted(setRequestedAmountSubscriber);
                    }
                }
            }
        }

        public class SetStopLossPriceSetup {

            private final TestSubscriber<OrderEvent> setStopLossPriceSubscriber = new TestSubscriber<>();
            private Observable<OrderEvent> setStopLossPriceObservable;

            @Test
            public void testOnJFException() {
                assertJFException(() -> orderChangeUtil.setStopLossPrice(orderUnderTest, newSL));
            }

            public class SetStopLossPriceCall {

                @Before
                public void setUp() {
                    setStopLossPriceObservable = orderChangeUtil.setStopLossPrice(orderUnderTest, newSL);
                }

                @Test
                public void testSetStopLossPriceCallsOnOrder() throws JFException {
                    captureAndRunOrderCall();

                    verify(orderUnderTest).setStopLossPrice(newSL);
                }

                @Test
                public void testSetStopLossPriceRegistersOnEventGateway() {
                    verifyGatewayRegister(OrderCallRequest.CHANGE_SL);
                }

                @Test
                public void testSetStopLossPriceObservableIgnoresOtherOrders() {
                    setStopLossPriceObservable.subscribe(setStopLossPriceSubscriber);

                    assertOtherOrderIsIgnored(setStopLossPriceSubscriber);
                }

                public class WhenSetStopLossPriceRejected {

                    @Before
                    public void setUp() {
                        sendOrderEvent(OrderEventType.CHANGE_SL_REJECTED);
                    }

                    @Test
                    public void testSetStopLossPriceSubscriberCompletedWithRejection() {
                        setStopLossPriceObservable.subscribe(setStopLossPriceSubscriber);

                        assertRejectException(setStopLossPriceSubscriber);
                    }

                    @Test
                    public void testNoRetryIsDone() throws JFException {
                        captureAndRunOrderCall();

                        verify(orderUnderTest).setStopLossPrice(newSL);
                    }
                }

                public class WhenSetStopLossPricePriceOK {

                    @Before
                    public void setUp() {
                        sendOrderEvent(OrderEventType.SL_CHANGE_OK);
                    }

                    @Test
                    public void testSetStopLossPriceSubscriberCompleted() {
                        setStopLossPriceObservable.subscribe(setStopLossPriceSubscriber);

                        assertSubscriberCompleted(setStopLossPriceSubscriber);
                    }
                }
            }
        }

        public class SetTakeProfitPriceSetup {

            private final TestSubscriber<OrderEvent> setTakeProfitPriceSubscriber = new TestSubscriber<>();
            private Observable<OrderEvent> setTakeProfitPriceObservable;

            @Test
            public void testOnJFException() {
                assertJFException(() -> orderChangeUtil.setTakeProfitPrice(orderUnderTest, newTP));
            }

            public class SetTakeProfitPriceCall {

                @Before
                public void setUp() {
                    setTakeProfitPriceObservable = orderChangeUtil.setTakeProfitPrice(orderUnderTest, newTP);
                }

                @Test
                public void testSetTakeProfitPriceCallsOnOrder() throws JFException {
                    captureAndRunOrderCall();

                    verify(orderUnderTest).setTakeProfitPrice(newTP);
                }

                @Test
                public void testSetTakeProfitPriceRegistersOnEventGateway() {
                    verifyGatewayRegister(OrderCallRequest.CHANGE_TP);
                }

                @Test
                public void testSetTakeProfitPriceObservableIgnoresOtherOrders() {
                    setTakeProfitPriceObservable.subscribe(setTakeProfitPriceSubscriber);

                    assertOtherOrderIsIgnored(setTakeProfitPriceSubscriber);
                }

                public class WhenSetTakeProfitPriceRejected {

                    @Before
                    public void setUp() {
                        sendOrderEvent(OrderEventType.CHANGE_TP_REJECTED);
                    }

                    @Test
                    public void testSetTakeProfitPriceSubscriberCompletedWithRejection() {
                        setTakeProfitPriceObservable.subscribe(setTakeProfitPriceSubscriber);

                        assertRejectException(setTakeProfitPriceSubscriber);
                    }

                    @Test
                    public void testNoRetryIsDone() throws JFException {
                        captureAndRunOrderCall();

                        verify(orderUnderTest).setTakeProfitPrice(newTP);
                    }
                }

                public class WhenSetTakeProfitPricePriceOK {

                    @Before
                    public void setUp() {
                        sendOrderEvent(OrderEventType.TP_CHANGE_OK);
                    }

                    @Test
                    public void testSetTakeProfitPriceSubscriberCompleted() {
                        setTakeProfitPriceObservable.subscribe(setTakeProfitPriceSubscriber);

                        assertSubscriberCompleted(setTakeProfitPriceSubscriber);
                    }
                }
            }
        }
    }
}
