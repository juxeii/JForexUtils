package com.jforex.programming.order.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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
import com.jforex.programming.position.NoRestorePolicy;
import com.jforex.programming.position.Position;
import com.jforex.programming.position.PositionFactory;
import com.jforex.programming.position.RestoreSLTPPolicy;
import com.jforex.programming.test.common.InstrumentUtilForTest;
import com.jforex.programming.test.common.OrderParamsForTest;
import com.jforex.programming.test.fakes.IOrderForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import rx.Observable;
import rx.observers.TestSubscriber;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

@RunWith(HierarchicalContextRunner.class)
public class OrderUtilTest extends InstrumentUtilForTest {

    private OrderUtil orderUtil;

    @Mock
    private OrderCallExecutor orderCallExecutorMock;
    @Mock
    private OrderEventGateway orderEventGatewayMock;
    @Mock
    private PositionFactory positionFactoryMock;
    @Captor
    private ArgumentCaptor<OrderSupplierCall> orderCallCaptor;
    @Captor
    private ArgumentCaptor<Set<IOrder>> toMergeOrdersCaptor;
    private Position position;
    private final RestoreSLTPPolicy noRestoreSLTPPolicy = new NoRestorePolicy();
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
        position = new Position(instrumentEURUSD, orderEventSubject);
        setUpMocks();
        orderUtil = new OrderUtil(engineMock,
                                  orderCallExecutorMock,
                                  orderEventGatewayMock,
                                  positionFactoryMock);
    }

    private void setUpMocks() {
        when(orderEventGatewayMock.observable()).thenReturn(orderEventSubject);
        when(orderCallExecutorMock.run(any(OrderSupplierCall.class))).thenReturn(orderExecutorResult);
        when(positionFactoryMock.forInstrument(instrumentEURUSD)).thenReturn(position);
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

    private void captureAndRunOrderCall(final int times) throws JFException {
        verify(orderCallExecutorMock, times(times)).run(orderCallCaptor.capture());
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
    public void testMergeCallsOnIEngine() throws JFException {
        orderUtil.mergeOrders(mergeLabel, toMergeOrders);

        captureAndRunOrderCall(1);

        engineForTest.verifyMerge(mergeLabel, toMergeOrders, 1);
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

    public class BuySubmitSetup {

        private final IOrderForTest buyOrder = IOrderForTest.buyOrderEURUSD();
        private final OrderParams orderParamsBUY = OrderParamsForTest.paramsBuyEURUSD();
        private final TestSubscriber<OrderEvent> buySubmitSubscriber = new TestSubscriber<>();
        private Observable<OrderEvent> buyObs;
        private final Supplier<Observable<OrderEvent>> buySubmitCall =
                () -> buyObs = orderUtil.submitOrder(orderParamsBUY);

        public class JFExceptionOnSubmit {

            @Before
            public void setUp() {
                prepareJFException();

                buySubmitCall.get();
            }

            @Test
            public void testSubmitCallsOnIEngine() throws JFException {
                captureAndRunOrderCall(1);

                engineForTest.verifySubmit(orderParams, 1);
            }

            @Test
            public void testNoOrderIsAddedToPosition() {
                assertTrue(position.orders().isEmpty());
            }

            @Test
            public void testBuySubscriberCompletedWithError() {
                buyObs.subscribe(buySubmitSubscriber);

                buySubmitSubscriber.assertError(JFException.class);
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
                captureAndRunOrderCall(1);

                engineForTest.verifySubmit(orderParams, 1);
            }

            @Test
            public void testBuyOrderIsRegisteredOnEventGateway() {
                verify(orderEventGatewayMock).registerOrderRequest(buyOrder, OrderCallRequest.SUBMIT);
            }

            @Test
            public void testSubmitObservableIgnoresOtherOrders() {
                assertOtherOrderIsIgnored();
            }

            public class WhenSubmitRejected {

                @Before
                public void setUp() {
                    buyOrder.setState(IOrder.State.CANCELED);

                    sendOrderEvent(buyOrder, OrderEventType.SUBMIT_REJECTED);
                }

                @Test
                public void testNoOrderIsAddedToPosition() {
                    assertTrue(position.orders().isEmpty());
                }

                @Test
                public void testBuySubscriberCompletedWithRejection() {
                    buyObs.subscribe(buySubmitSubscriber);

                    buySubmitSubscriber.assertError(OrderCallRejectException.class);
                }

                @Test
                public void testNoRetryIsDone() throws JFException {
                    captureAndRunOrderCall(1);

                    engineForTest.verifySubmit(orderParams, 1);
                }
            }

            public class WhenFillRejected {

                @Before
                public void setUp() {
                    buyOrder.setState(IOrder.State.CANCELED);

                    sendOrderEvent(buyOrder, OrderEventType.FILL_REJECTED);
                }

                @Test
                public void testNoOrderIsAddedToPosition() {
                    assertTrue(position.orders().isEmpty());
                }

                @Test
                public void testBuySubscriberCompletedWithRejection() {
                    buyObs.subscribe(buySubmitSubscriber);

                    buySubmitSubscriber.assertError(OrderCallRejectException.class);
                }

                @Test
                public void testNoRetryIsDone() throws JFException {
                    captureAndRunOrderCall(1);

                    engineForTest.verifySubmit(orderParams, 1);
                }
            }

            public class WhenSubmitOK {

                @Before
                public void setUp() {
                    sendOrderEvent(buyOrder, OrderEventType.SUBMIT_OK);
                }

                @Test
                public void testNoOrderIsAddedToPosition() {
                    assertTrue(position.orders().isEmpty());
                }

                @Test
                public void testSubscriberDoesNotYetComplete() {
                    buyObs.subscribe(buySubmitSubscriber);

                    assertSubscriberNotYetCompleted(buySubmitSubscriber);
                }
            }

            public class WhenFilled {

                @Before
                public void setUp() {
                    buyOrder.setState(IOrder.State.FILLED);

                    sendOrderEvent(buyOrder, OrderEventType.FULL_FILL_OK);
                }

                @Test
                public void testBuyOrderIsAddedToPosition() {
                    assertTrue(position.contains(buyOrder));
                }

                @Test
                public void testBuySubscriberCompleted() {
                    buyObs.subscribe(buySubmitSubscriber);

                    assertSubscriberCompleted(buySubmitSubscriber);
                }

                public class BuyCloseSetup {

                    private final TestSubscriber<OrderEvent> closeBuySubscriber = new TestSubscriber<>();
                    private Observable<OrderEvent> closeObs;
                    private final Supplier<Observable<OrderEvent>> buyCloseCall =
                            () -> closeObs = orderUtil.close(buyOrder);

                    public class JFExceptionOnClose {

                        @Before
                        public void setUp() {
                            prepareJFException();

                            buyCloseCall.get();
                        }

                        @Test
                        public void testCloseCallsOnOrder() throws JFException {
                            captureAndRunOrderCall(2);

                            verify(buyOrder).close();
                        }

                        @Test
                        public void testBuySubscriberCompletedWithError() {
                            closeObs.subscribe(closeBuySubscriber);

                            closeBuySubscriber.assertError(JFException.class);
                        }
                    }

                    public class BuyCloseCall {

                        @Before
                        public void setUp() {
                            buyCloseCall.get();
                        }

                        @Test
                        public void testCloseCallsOnOrder() throws JFException {
                            captureAndRunOrderCall(2);

                            verify(buyOrder).close();
                        }

                        @Test
                        public void testCloseRegistersOnEventGateway() {
                            verify(orderEventGatewayMock).registerOrderRequest(buyOrder, OrderCallRequest.CLOSE);
                        }

                        @Test
                        public void testCloseObservableIgnoresOtherOrders() {
                            closeObs.subscribe(closeBuySubscriber);

                            assertOtherOrderIsIgnored();
                        }

                        public class WhenCloseRejected {

                            @Before
                            public void setUp() {
                                sendOrderEvent(buyOrder, OrderEventType.CLOSE_REJECTED);
                            }

                            @Test
                            public void testCloseSubscriberCompletedWithRejection() {
                                closeObs.subscribe(closeBuySubscriber);

                                closeBuySubscriber.assertError(OrderCallRejectException.class);
                            }

                            @Test
                            public void testNoRetryIsDone() throws JFException {
                                captureAndRunOrderCall(2);

                                verify(buyOrder).close();
                            }
                        }

                        public class WhenClosed {

                            @Before
                            public void setUp() {
                                buyOrder.setState(IOrder.State.CLOSED);

                                sendOrderEvent(buyOrder, OrderEventType.CLOSE_OK);
                            }

                            @Test
                            public void testCloseSubscriberCompleted() {
                                closeObs.subscribe(closeBuySubscriber);

                                assertSubscriberCompleted(closeBuySubscriber);
                            }

                            @Test
                            public void testBuyOrderWasRemovedFromPosition() {
                                assertTrue(position.orders().isEmpty());
                            }
                        }
                    }
                }

                public class SetLabelSetup {

                    private final TestSubscriber<OrderEvent> setLabelBuySubscriber = new TestSubscriber<>();
                    private Observable<OrderEvent> setLabelObs;
                    private final Supplier<Observable<OrderEvent>> buySetLabelCall =
                            () -> setLabelObs = orderUtil.setLabel(buyOrder, newLabel);

                    public class JFExceptionOnSetLabel {

                        @Before
                        public void setUp() {
                            prepareJFException();

                            buySetLabelCall.get();
                        }

                        @Test
                        public void testSetLabelCallsOnOrder() throws JFException {
                            captureAndRunOrderCall(2);

                            verify(buyOrder).setLabel(newLabel);
                        }

                        @Test
                        public void testSetLabelSubscriberCompletedWithError() {
                            setLabelObs.subscribe(setLabelBuySubscriber);

                            setLabelBuySubscriber.assertError(JFException.class);
                        }
                    }

                    public class SetLabelCall {

                        @Before
                        public void setUp() {
                            buySetLabelCall.get();
                        }

                        @Test
                        public void testSetLabelCallsOnOrder() throws JFException {
                            captureAndRunOrderCall(2);

                            verify(buyOrder).setLabel(newLabel);
                        }

                        @Test
                        public void testSetLabelRegistersOnEventGateway() {
                            verify(orderEventGatewayMock).registerOrderRequest(buyOrder, OrderCallRequest.CHANGE_LABEL);
                        }

                        @Test
                        public void testSetLabelObservableIgnoresOtherOrders() {
                            setLabelObs.subscribe(setLabelBuySubscriber);

                            assertOtherOrderIsIgnored();
                        }

                        public class WhenSetLabelRejected {

                            @Before
                            public void setUp() {
                                sendOrderEvent(buyOrder, OrderEventType.CHANGE_LABEL_REJECTED);
                            }

                            @Test
                            public void testSetLabelSubscriberCompletedWithRejection() {
                                setLabelObs.subscribe(setLabelBuySubscriber);

                                setLabelBuySubscriber.assertError(OrderCallRejectException.class);
                            }

                            @Test
                            public void testNoRetryIsDone() throws JFException {
                                captureAndRunOrderCall(2);

                                verify(buyOrder).setLabel(newLabel);
                            }
                        }

                        public class WhenSetLabelOK {

                            @Before
                            public void setUp() {
                                sendOrderEvent(buyOrder, OrderEventType.LABEL_CHANGE_OK);
                            }

                            @Test
                            public void testCloseSubscriberCompleted() {
                                setLabelObs.subscribe(setLabelBuySubscriber);

                                assertSubscriberCompleted(setLabelBuySubscriber);
                            }
                        }
                    }
                }

                public class SetGTTSetup {

                    private final TestSubscriber<OrderEvent> setGTTBuySubscriber = new TestSubscriber<>();
                    private Observable<OrderEvent> setGTTObs;
                    private final Supplier<Observable<OrderEvent>> setGTTCall =
                            () -> setGTTObs = orderUtil.setGoodTillTime(buyOrder, newGTT);

                    public class JFExceptionOnSetGTT {

                        @Before
                        public void setUp() {
                            prepareJFException();

                            setGTTCall.get();
                        }

                        @Test
                        public void testSetGTTCallsOnOrder() throws JFException {
                            captureAndRunOrderCall(2);

                            verify(buyOrder).setGoodTillTime(newGTT);
                        }

                        @Test
                        public void testSetGTTSubscriberCompletedWithError() {
                            setGTTObs.subscribe(setGTTBuySubscriber);

                            setGTTBuySubscriber.assertError(JFException.class);
                        }
                    }

                    public class SetGTTCall {

                        @Before
                        public void setUp() {
                            setGTTCall.get();
                        }

                        @Test
                        public void testSetGTTCallsOnOrder() throws JFException {
                            captureAndRunOrderCall(2);

                            verify(buyOrder).setGoodTillTime(newGTT);
                        }

                        @Test
                        public void testSetGTTRegistersOnEventGateway() {
                            verify(orderEventGatewayMock).registerOrderRequest(buyOrder, OrderCallRequest.CHANGE_GTT);
                        }

                        @Test
                        public void testSetGTTObservableIgnoresOtherOrders() {
                            setGTTObs.subscribe(setGTTBuySubscriber);

                            assertOtherOrderIsIgnored();
                        }

                        public class WhenSetGTTRejected {

                            @Before
                            public void setUp() {
                                sendOrderEvent(buyOrder, OrderEventType.CHANGE_GTT_REJECTED);
                            }

                            @Test
                            public void testSetGTTSubscriberCompletedWithRejection() {
                                setGTTObs.subscribe(setGTTBuySubscriber);

                                setGTTBuySubscriber.assertError(OrderCallRejectException.class);
                            }

                            @Test
                            public void testNoRetryIsDone() throws JFException {
                                captureAndRunOrderCall(2);

                                verify(buyOrder).setGoodTillTime(newGTT);
                            }
                        }

                        public class WhenSetGTTOK {

                            @Before
                            public void setUp() {
                                sendOrderEvent(buyOrder, OrderEventType.GTT_CHANGE_OK);
                            }

                            @Test
                            public void testCloseSubscriberCompleted() {
                                setGTTObs.subscribe(setGTTBuySubscriber);

                                assertSubscriberCompleted(setGTTBuySubscriber);
                            }
                        }
                    }
                }

                public class SetOpenPriceSetup {

                    private final TestSubscriber<OrderEvent> setOpenPriceBuySubscriber = new TestSubscriber<>();
                    private Observable<OrderEvent> setOpenPriceObs;
                    private final Supplier<Observable<OrderEvent>> setOpenPriceCall =
                            () -> setOpenPriceObs = orderUtil.setOpenPrice(buyOrder, newOpenPrice);

                    public class JFExceptionOnSetOpenPrice {

                        @Before
                        public void setUp() {
                            prepareJFException();

                            setOpenPriceCall.get();
                        }

                        @Test
                        public void testSetOpenPriceCallsOnOrder() throws JFException {
                            captureAndRunOrderCall(2);

                            verify(buyOrder).setOpenPrice(newOpenPrice);
                        }

                        @Test
                        public void testSetOpenPriceSubscriberCompletedWithError() {
                            setOpenPriceObs.subscribe(setOpenPriceBuySubscriber);

                            setOpenPriceBuySubscriber.assertError(JFException.class);
                        }
                    }

                    public class SetOpenPriceCall {

                        @Before
                        public void setUp() {
                            setOpenPriceCall.get();
                        }

                        @Test
                        public void testSetOpenPriceCallsOnOrder() throws JFException {
                            captureAndRunOrderCall(2);

                            verify(buyOrder).setOpenPrice(newOpenPrice);
                        }

                        @Test
                        public void testSetOpenPriceRegistersOnEventGateway() {
                            verify(orderEventGatewayMock).registerOrderRequest(buyOrder,
                                                                               OrderCallRequest.CHANGE_OPENPRICE);
                        }

                        @Test
                        public void testSetOpenPriceObservableIgnoresOtherOrders() {
                            setOpenPriceObs.subscribe(setOpenPriceBuySubscriber);

                            assertOtherOrderIsIgnored();
                        }

                        public class WhenSetOpenPriceRejected {

                            @Before
                            public void setUp() {
                                sendOrderEvent(buyOrder, OrderEventType.CHANGE_OPENPRICE_REJECTED);
                            }

                            @Test
                            public void testSetOpenPriceSubscriberCompletedWithRejection() {
                                setOpenPriceObs.subscribe(setOpenPriceBuySubscriber);

                                setOpenPriceBuySubscriber.assertError(OrderCallRejectException.class);
                            }

                            @Test
                            public void testNoRetryIsDone() throws JFException {
                                captureAndRunOrderCall(2);

                                verify(buyOrder).setOpenPrice(newOpenPrice);
                            }
                        }

                        public class WhenSetOpenPriceOK {

                            @Before
                            public void setUp() {
                                sendOrderEvent(buyOrder, OrderEventType.OPENPRICE_CHANGE_OK);
                            }

                            @Test
                            public void testCloseSubscriberCompleted() {
                                setOpenPriceObs.subscribe(setOpenPriceBuySubscriber);

                                assertSubscriberCompleted(setOpenPriceBuySubscriber);
                            }
                        }
                    }
                }
            }
        }
    }

    @Test
    public void testSetRequestedAmountCallsOnOrder() throws JFException {
        orderUtil.setRequestedAmount(orderUnderTest, newAmount);

        captureAndRunOrderCall(1);

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

        captureAndRunOrderCall(1);

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

        captureAndRunOrderCall(1);

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

    /******************/
    /* Position Tests */
    /******************/

    private void setCallResult(final IOrder order) {
        final OrderCallExecutorResult orderExecutorResult =
                new OrderCallExecutorResult(Optional.of(order), Optional.empty());
        when(orderCallExecutorMock.run(any(OrderSupplierCall.class)))
                .thenReturn(orderExecutorResult);
    }

    private void sendOrderEvent(final IOrder order,
                                final OrderEventType orderEventType) {
        final OrderEvent orderEvent = new OrderEvent(order, orderEventType);
        orderEventSubject.onNext(orderEvent);
    }

    private boolean isRepositoryEmpty() {
        return position.filterOrders(order -> true).isEmpty();
    }

    private boolean positionHasOrder(final IOrder orderToFind) {
        return position.filterOrders(order -> order.getLabel().equals(orderToFind.getLabel())).size() == 1;
    }

    private void assertSubscriberCompleted(final TestSubscriber<?> subscriber) {
        subscriber.assertNoErrors();
        subscriber.assertCompleted();
    }

    private void assertSubscriberNotYetCompleted(final TestSubscriber<?> subscriber) {
        subscriber.assertNoErrors();
        subscriber.assertNotCompleted();
    }

    @Test
    public void testCloseOnEmptyPositionCompletes() {
        final TestSubscriber<OrderEvent> closeSubscriber = new TestSubscriber<>();

        orderUtil.closePosition(instrumentEURUSD).subscribe(closeSubscriber);

        assertSubscriberCompleted(closeSubscriber);
    }

    public class BuySubmitOK {

        private final IOrderForTest buyOrder = IOrderForTest.buyOrderEURUSD();
        protected final OrderParams orderParamsSell = OrderParamsForTest.paramsSellEURUSD();

        @Before
        public void setUp() {
            buyOrder.setState(IOrder.State.FILLED);

            position.addOrder(buyOrder);
        }

        @Test
        public void testPositionHasBuyOrder() {
            assertTrue(positionHasOrder(buyOrder));
        }

        @Test
        public void testMergeCallIsIgnored() {
            final TestSubscriber<OrderEvent> mergeSubscriber = new TestSubscriber<>();

            orderUtil.mergePositionOrders(mergeLabel, instrumentEURUSD, noRestoreSLTPPolicy)
                    .subscribe(mergeSubscriber);

            assertSubscriberCompleted(mergeSubscriber);
        }

        public class SellSubmitOK {

            private final IOrderForTest sellOrder = IOrderForTest.sellOrderEURUSD();

            @Before
            public void setUp() {
                sellOrder.setState(IOrder.State.FILLED);

                position.addOrder(sellOrder);
            }

            @Test
            public void testPositionHasBuyAndSellOrder() {
                assertTrue(positionHasOrder(buyOrder));
                assertTrue(positionHasOrder(sellOrder));
            }

            public class MergeSequenceSetup {

//                private final double noSLPrice = platformSettings.noSLPrice();
//                private final double noTPPrice = platformSettings.noTPPrice();
                protected final TestSubscriber<OrderEvent> mergeSubscriber = new TestSubscriber<>();

                protected Runnable mergeCall =
                        () -> orderUtil.mergePositionOrders(mergeLabel, instrumentEURUSD, noRestoreSLTPPolicy)
                                .subscribe(mergeSubscriber);

                public class RemoveTPFail {

                    @Before
                    public void setUp() {
                        prepareJFException();

                        mergeCall.run();
                    }

                    @Test
                    public void testMergeSubscriberCompletedWithError() {
                        mergeSubscriber.assertError(JFException.class);
                    }
                }

                public class RemoveTPOK {

                    @Before
                    public void setUp() {
//                        when(positionTaskMock.setTPCompletable(buyOrder, noTPPrice))
//                                .thenReturn(Completable.complete());
//                        when(positionTaskMock.setTPCompletable(sellOrder, noTPPrice))
//                                .thenReturn(Completable.complete());
                    }

                    @Test
                    public void testRemoveTPIsCalledPositionTaskWhenNotSubscribed() {
                        orderUtil.mergePositionOrders(mergeLabel, instrumentEURUSD, noRestoreSLTPPolicy);

//                        verify(positionTaskMock).setTPCompletable(buyOrder, noTPPrice);
//                        verify(positionTaskMock).setTPCompletable(sellOrder, noTPPrice);

                    }

                    public class RemoveSLInProgress {

                        @Before
                        public void setUp() {
//                            when(positionTaskMock.setSLCompletable(buyOrder, noTPPrice))
//                                    .thenReturn(Completable.never());
//                            when(positionTaskMock.setSLCompletable(sellOrder, noTPPrice))
//                                    .thenReturn(Completable.never());

                            mergeCall.run();
                        }

                        @Test
                        public void testMergeSubscriberNotYetCompleted() {
                            assertSubscriberNotYetCompleted(mergeSubscriber);
                        }
                    }

                    public class RemoveSLOK {

                        @Before
                        public void setUp() {
//                            when(positionTaskMock.setSLCompletable(buyOrder, noSLPrice))
//                                    .thenReturn(Completable.complete());
//                            when(positionTaskMock.setSLCompletable(sellOrder, noSLPrice))
//                                    .thenReturn(Completable.complete());
                        }

                        public class MergeCallFail {

                            @Before
                            public void setUp() {
                                prepareJFException();

                                mergeCall.run();
                            }

                            @Test
                            public void testPositionHasStillBuyAndSellOrder() {
                                assertTrue(positionHasOrder(buyOrder));
                                assertTrue(positionHasOrder(sellOrder));
                            }

                            @Test
                            public void testMergeSubscriberCompletedWithError() {
                                mergeSubscriber.assertError(JFException.class);
                            }
                        }

                    }

                    public class RemoveSLFail {

                        @Before
                        public void setUp() {
                            prepareJFException();

                            mergeCall.run();
                        }

                        @Test
                        public void testMergeSubscriberCompletedWithError() {
                            mergeSubscriber.assertError(JFException.class);
                        }
                    }
                }
            }

            public class CloseSetup {

                protected final TestSubscriber<OrderEvent> closeSubscriber = new TestSubscriber<>();
                protected Runnable closeCall =
                        () -> orderUtil.closePosition(instrumentEURUSD).subscribe(closeSubscriber);

                public class CloseInProcess {

                    @Before
                    public void setUp() {
                        closeCall.run();

                        buyOrder.setState(IOrder.State.CLOSED);
                    }

                    @Test
                    public void testCloseSubscriberNotYetCompleted() {
                        assertSubscriberNotYetCompleted(closeSubscriber);
                    }

                    @Test
                    public void testPositionHasOnlySellOrder() {
                        assertTrue(positionHasOrder(sellOrder));
                    }
                }

                public class CloseOK {

                    @Before
                    public void setUp() {
                        final OrderCallExecutorResult buyResult =
                                new OrderCallExecutorResult(Optional.of(buyOrder), Optional.empty());
                        final OrderCallExecutorResult sellResult =
                                new OrderCallExecutorResult(Optional.of(sellOrder), Optional.empty());
                        when(orderCallExecutorMock.run(any(OrderSupplierCall.class)))
                                .thenReturn(buyResult)
                                .thenReturn(sellResult);

                        closeCall.run();

                        buyOrder.setState(IOrder.State.CLOSED);
                        sellOrder.setState(IOrder.State.CLOSED);
                        sendOrderEvent(buyOrder, OrderEventType.CLOSE_OK);
                        sendOrderEvent(sellOrder, OrderEventType.CLOSE_OK);
                    }

                    @Test
                    public void testPositionHasNoOrder() {
                        assertTrue(isRepositoryEmpty());
                    }

                    @Test
                    public void testSubmitSubscriberCompleted() {
                        assertSubscriberCompleted(closeSubscriber);
                    }
                }

                public class CloseFail {

                    @Before
                    public void setUp() {
                        prepareJFException();

                        closeCall.run();
                    }

                    @Test
                    public void testPositionHasStillBuyOrder() {
                        assertTrue(positionHasOrder(buyOrder));
                    }

                    @Test
                    public void testCloseSubscriberCompletedWithError() {
                        closeSubscriber.assertError(JFException.class);
                    }
                }
            }
        }
    }
}
