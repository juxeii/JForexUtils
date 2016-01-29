package com.jforex.programming.position.test;

import static com.jforex.programming.misc.JForexUtil.pfs;
import static com.jforex.programming.misc.JForexUtil.uss;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.JFException;
import com.jforex.programming.order.OrderParams;
import com.jforex.programming.order.OrderUtil;
import com.jforex.programming.order.call.OrderCallRequest;
import com.jforex.programming.order.call.OrderCallResult;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.position.Position;
import com.jforex.programming.position.RestoreSLTPPolicy;
import com.jforex.programming.test.common.InstrumentUtilForTest;
import com.jforex.programming.test.common.OrderParamsForTest;
import com.jforex.programming.test.fakes.IOrderForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

@RunWith(HierarchicalContextRunner.class)
public class PositionTest extends InstrumentUtilForTest {

    private Position position;

    @Mock private RestoreSLTPPolicy restoreSLTPPolicyMock;
    @Mock private OrderUtil orderUtilMock;
    private Subject<OrderEvent, OrderEvent> orderEventSubject;
    private OrderParams orderParamsEURUSDBuy;
    private OrderParams orderParamsEURUSDSell;
    private String mergeLabel;
    private final IOrderForTest buyOrderEURUSD = IOrderForTest.buyOrderEURUSD();
    private final IOrderForTest sellOrderEURUSD = IOrderForTest.sellOrderEURUSD();
    private final IOrderForTest mergeOrderEURUSD = IOrderForTest.buyOrderEURUSD();
    private final double restoreSL = 1.12345;
    private final double restoreTP = 1.12543;
    private OrderCallResult callResultWithExceptionEURUSDBuy;

    @Before
    public void setUp() throws JFException {
        orderParamsEURUSDBuy = OrderParamsForTest.paramsBuyEURUSD();
        orderParamsEURUSDSell = OrderParamsForTest.paramsSellEURUSD();
        mergeLabel = uss.ORDER_MERGE_LABEL_PREFIX() + orderParamsEURUSDBuy.label();
        mergeOrderEURUSD.setLabel(mergeLabel);
        initCommonTestFramework();
        setUpMocks();

        orderEventSubject = PublishSubject.create();

        position = new Position(instrumentEURUSD,
                                orderUtilMock,
                                orderEventSubject,
                                restoreSLTPPolicyMock);
    }

    private void setUpMocks() {
        when(restoreSLTPPolicyMock.restoreSL(any())).thenReturn(restoreSL);
        when(restoreSLTPPolicyMock.restoreTP(any())).thenReturn(restoreTP);

        when(orderUtilMock.submit(orderParamsEURUSDBuy)).thenReturn(new OrderCallResult(Optional.of(buyOrderEURUSD),
                                                                                        emptyJFExceptionOpt,
                                                                                        OrderCallRequest.SUBMIT));
        when(orderUtilMock.submit(orderParamsEURUSDSell)).thenReturn(new OrderCallResult(Optional.of(sellOrderEURUSD),
                                                                                         emptyJFExceptionOpt,
                                                                                         OrderCallRequest.SUBMIT));
        when(orderUtilMock.changeSL(eq(buyOrderEURUSD),
                                    anyDouble())).thenReturn(new OrderCallResult(Optional.of(buyOrderEURUSD),
                                                                                 emptyJFExceptionOpt,
                                                                                 OrderCallRequest.CHANGE_SL));
        when(orderUtilMock.changeSL(eq(sellOrderEURUSD),
                                    anyDouble())).thenReturn(new OrderCallResult(Optional.of(sellOrderEURUSD),
                                                                                 emptyJFExceptionOpt,
                                                                                 OrderCallRequest.CHANGE_SL));
        when(orderUtilMock.changeTP(eq(buyOrderEURUSD),
                                    anyDouble())).thenReturn(new OrderCallResult(Optional.of(buyOrderEURUSD),
                                                                                 emptyJFExceptionOpt,
                                                                                 OrderCallRequest.CHANGE_TP));
        when(orderUtilMock.changeTP(eq(sellOrderEURUSD),
                                    anyDouble())).thenReturn(new OrderCallResult(Optional.of(sellOrderEURUSD),
                                                                                 emptyJFExceptionOpt,
                                                                                 OrderCallRequest.CHANGE_TP));
        when(orderUtilMock.merge(eq(mergeLabel),
                                 any())).thenReturn(new OrderCallResult(Optional.of(mergeOrderEURUSD),
                                                                        emptyJFExceptionOpt,
                                                                        OrderCallRequest.MERGE));
        when(orderUtilMock.changeSL(eq(mergeOrderEURUSD),
                                    anyDouble())).thenReturn(new OrderCallResult(Optional.of(mergeOrderEURUSD),
                                                                                 emptyJFExceptionOpt,
                                                                                 OrderCallRequest.CHANGE_SL));
        when(orderUtilMock.changeTP(eq(mergeOrderEURUSD),
                                    anyDouble())).thenReturn(new OrderCallResult(Optional.of(mergeOrderEURUSD),
                                                                                 emptyJFExceptionOpt,
                                                                                 OrderCallRequest.CHANGE_TP));
        when(orderUtilMock.close(buyOrderEURUSD)).thenReturn(new OrderCallResult(Optional.of(buyOrderEURUSD),
                                                                                 emptyJFExceptionOpt,
                                                                                 OrderCallRequest.CLOSE));
        when(orderUtilMock.close(sellOrderEURUSD)).thenReturn(new OrderCallResult(Optional.of(sellOrderEURUSD),
                                                                                  emptyJFExceptionOpt,
                                                                                  OrderCallRequest.CLOSE));
        when(orderUtilMock.close(mergeOrderEURUSD)).thenReturn(new OrderCallResult(Optional.of(mergeOrderEURUSD),
                                                                                   emptyJFExceptionOpt,
                                                                                   OrderCallRequest.CLOSE));
    }

    private void sendOrderEvent(final IOrder order,
                                final OrderEventType orderEventType) {
        orderEventSubject.onNext(new OrderEvent(order, orderEventType));
    }

    private boolean isRepositoryEmpty() {
        return position.filter(order -> true).isEmpty();
    }

    private boolean positionHasOrder(final IOrder orderToFind) {
        return position.filter(order -> order.getLabel().equals(orderToFind.getLabel())).size() == 1;
    }

    @Test
    public void testIsNotBusyForNoRunningTask() {
        assertFalse(position.isBusy());
    }

    @Test
    public void testCloseOnEmptyPositionIsIgnored() {
        position.close();

        verifyZeroInteractions(orderUtilMock);
        assertFalse(position.isBusy());
    }

    @Test
    public void testExternalOrderWillNotBeAddedToRepositoryOnFill() {
        final IOrderForTest externalOrder = IOrderForTest.buyOrderEURUSD();

        sendOrderEvent(externalOrder, OrderEventType.FULL_FILL_OK);

        assertTrue(isRepositoryEmpty());
    }

    @Test
    public void testExternalOrderWillNotBeAddedToRepositoryOnMergeOK() {
        final IOrderForTest externalOrder = IOrderForTest.buyOrderEURUSD();

        sendOrderEvent(externalOrder, OrderEventType.MERGE_OK);

        assertTrue(isRepositoryEmpty());
    }

    @Test
    public void testCallingSubmitTwoTimesWillQueueOneCall() {
        position.submit(orderParamsEURUSDBuy);
        position.submit(orderParamsEURUSDBuy);

        verify(orderUtilMock).submit(orderParamsEURUSDBuy);
    }

    public class AfterSubmitWithException {

        @Before
        public void setUp() {
            when(orderUtilMock.submit(orderParamsEURUSDBuy)).thenReturn(callResultWithExceptionEURUSDBuy);

            position.submit(orderParamsEURUSDBuy);
        }

        @Test
        public void testIsNotBusy() {
            assertFalse(position.isBusy());
        }

        @Test
        public void testSubmitOnOrderUtilIsCalled() {
            verify(orderUtilMock).submit(orderParamsEURUSDBuy);
        }

        @Test
        public void testPositionHasNoOrder() {
            assertTrue(isRepositoryEmpty());
        }
    }

    public class AfterSubmitAndMergeWithException {

        @Before
        public void setUp() {
            when(orderUtilMock.submit(orderParamsEURUSDBuy)).thenReturn(callResultWithExceptionEURUSDBuy);

            position.submitAndMerge(orderParamsEURUSDBuy, mergeLabel);
        }

        @Test
        public void testIsNotBusy() {
            assertFalse(position.isBusy());
        }

        @Test
        public void testSubmitOnOrderUtilIsCalled() {
            verify(orderUtilMock).submit(orderParamsEURUSDBuy);
        }

        @Test
        public void testPositionHasNoOrder() {
            assertTrue(isRepositoryEmpty());
        }
    }

    public class AfterSubmitAndMergeNoException {

        @Before
        public void setUp() {
            buyOrderEURUSD.setState(IOrder.State.CREATED);

            position.submitAndMerge(orderParamsEURUSDBuy, mergeLabel);
        }

        @Test
        public void testIsBusy() {
            assertTrue(position.isBusy());
        }

        @Test
        public void testPositionHasNoOrder() {
            assertTrue(isRepositoryEmpty());
        }

        public class AfterSubmitOKMessage {

            @Before
            public void setUp() {
                buyOrderEURUSD.setState(IOrder.State.OPENED);
                verify(orderUtilMock).submit(orderParamsEURUSDBuy);

                sendOrderEvent(buyOrderEURUSD, OrderEventType.SUBMIT_OK);
            }

            @Test
            public void testIsBusy() {
                assertTrue(position.isBusy());
            }

            public class AfterFullFillMessage {

                @Before
                public void setUp() {
                    buyOrderEURUSD.setState(IOrder.State.FILLED);

                    sendOrderEvent(buyOrderEURUSD, OrderEventType.FULL_FILL_OK);
                }

                @Test
                public void testIsNotBusy() {
                    assertFalse(position.isBusy());
                }

                @Test
                public void testPositionHasBuyOrder() {
                    assertTrue(positionHasOrder(buyOrderEURUSD));
                }

                @Test
                public void testNoOrderUtilInteractions() {
                    verifyNoMoreInteractions(orderUtilMock);
                }
            }
        }
    }

    public class AfterSubmitNoException {

        @Before
        public void setUp() {
            buyOrderEURUSD.setState(IOrder.State.CREATED);

            position.submit(orderParamsEURUSDBuy);

            verify(orderUtilMock).submit(orderParamsEURUSDBuy);
        }

        @Test
        public void testIsBusy() {
            assertTrue(position.isBusy());
        }

        @Test
        public void testPositionHasNoOrder() {
            assertTrue(isRepositoryEmpty());
        }

        public class AfterSubmitOKMessage {

            @Before
            public void setUp() {
                buyOrderEURUSD.setState(IOrder.State.OPENED);

                sendOrderEvent(buyOrderEURUSD, OrderEventType.SUBMIT_OK);
            }

            @Test
            public void testIsBusy() {
                assertTrue(position.isBusy());
            }

            public class AfterFillRejectMessage {

                @Before
                public void setUp() {
                    buyOrderEURUSD.setState(IOrder.State.CANCELED);

                    sendOrderEvent(buyOrderEURUSD, OrderEventType.FILL_REJECTED);
                }

                @Test
                public void testIsNotBusy() {
                    assertFalse(position.isBusy());
                }

                @Test
                public void testPositionHasNoOrder() {
                    assertTrue(isRepositoryEmpty());
                }
            }

            public class AfterFullFillMessage {

                @Before
                public void setUp() {
                    buyOrderEURUSD.setState(IOrder.State.FILLED);

                    sendOrderEvent(buyOrderEURUSD, OrderEventType.FULL_FILL_OK);
                }

                @Test
                public void testIsNotBusy() {
                    assertFalse(position.isBusy());
                }

                @Test
                public void testPositionHasBuyOrder() {
                    assertTrue(positionHasOrder(buyOrderEURUSD));
                }

                public class AfterCloseOnSL {

                    @Before
                    public void setUp() {
                        buyOrderEURUSD.setState(IOrder.State.CLOSED);

                        sendOrderEvent(buyOrderEURUSD, OrderEventType.CLOSED_BY_SL);
                    }

                    @Test
                    public void testIsFalse() {
                        assertFalse(position.isBusy());
                    }

                    @Test
                    public void testPositionHasNoOrder() {
                        assertTrue(isRepositoryEmpty());
                    }
                }

                public class AfterCloseOnTP {

                    @Before
                    public void setUp() {
                        buyOrderEURUSD.setState(IOrder.State.CLOSED);

                        sendOrderEvent(buyOrderEURUSD, OrderEventType.CLOSED_BY_TP);
                    }

                    @Test
                    public void testIsFalse() {
                        assertFalse(position.isBusy());
                    }

                    @Test
                    public void testPositionHasNoOrder() {
                        assertTrue(isRepositoryEmpty());
                    }
                }

                public class AfterSecondSubmit {

                    @Before
                    public void setUp() {
                        sellOrderEURUSD.setState(IOrder.State.CREATED);

                        position.submit(orderParamsEURUSDSell);

                        verify(orderUtilMock).submit(orderParamsEURUSDSell);
                    }

                    @Test
                    public void testIsBusy() {
                        assertTrue(position.isBusy());
                    }

                    public class AfterFullFillMessageForSecondOrder {

                        @Before
                        public void setUp() {
                            sellOrderEURUSD.setState(IOrder.State.FILLED);

                            sendOrderEvent(sellOrderEURUSD, OrderEventType.FULL_FILL_OK);
                        }

                        @Test
                        public void testIsNotBusy() {
                            assertFalse(position.isBusy());
                        }

                        @Test
                        public void testPositionHasBuyAndSellOrder() {
                            assertTrue(positionHasOrder(buyOrderEURUSD));
                            assertTrue(positionHasOrder(sellOrderEURUSD));
                        }
                    }
                }

                public class AfterSecondSubmitAndMerge {

                    @Before
                    public void setUp() {
                        sellOrderEURUSD.setState(IOrder.State.CREATED);

                        position.submitAndMerge(orderParamsEURUSDSell, mergeLabel);

                        verify(orderUtilMock).submit(orderParamsEURUSDSell);
                    }

                    @Test
                    public void testIsBusy() {
                        assertTrue(position.isBusy());
                    }

                    public class AfterFillRejectMessage {

                        @Before
                        public void setUp() {
                            sellOrderEURUSD.setState(IOrder.State.CANCELED);

                            sendOrderEvent(sellOrderEURUSD, OrderEventType.FILL_REJECTED);
                        }

                        @Test
                        public void testIsNotBusy() {
                            assertFalse(position.isBusy());
                        }

                        @Test
                        public void testPositionHasOnlyBuyOrder() {
                            assertTrue(positionHasOrder(buyOrderEURUSD));
                        }
                    }

                    public class AfterFullFillMessageForSecondOrder {

                        @Before
                        public void setUp() {
                            sellOrderEURUSD.setState(IOrder.State.FILLED);
                            sendOrderEvent(sellOrderEURUSD, OrderEventType.FULL_FILL_OK);

                            verify(orderUtilMock).changeTP(buyOrderEURUSD, pfs.NO_TAKE_PROFIT_PRICE());
                            verify(orderUtilMock).changeTP(sellOrderEURUSD, pfs.NO_TAKE_PROFIT_PRICE());
                        }

                        @Test
                        public void testIsBusyBecauseOfMerge() {
                            assertTrue(position.isBusy());
                        }

                        @Test
                        public void testPositionHasBuyAndSellOrder() {
                            assertTrue(positionHasOrder(buyOrderEURUSD));
                            assertTrue(positionHasOrder(sellOrderEURUSD));
                        }

                        public class AfterRemoveTPs {

                            @Before
                            public void setUp() throws JFException {
                                buyOrderEURUSD.setTakeProfitPrice(pfs.NO_TAKE_PROFIT_PRICE());
                                sellOrderEURUSD.setTakeProfitPrice(pfs.NO_TAKE_PROFIT_PRICE());

                                sendOrderEvent(buyOrderEURUSD, OrderEventType.TP_CHANGE_OK);
                                sendOrderEvent(sellOrderEURUSD, OrderEventType.TP_CHANGE_OK);

                                verify(orderUtilMock).changeSL(buyOrderEURUSD, pfs.NO_STOP_LOSS_PRICE());
                                verify(orderUtilMock).changeSL(sellOrderEURUSD, pfs.NO_STOP_LOSS_PRICE());
                            }

                            @Test
                            public void testIsBusyBecauseOfMerge() {
                                assertTrue(position.isBusy());
                            }

                            public class AfterRemoveSLs {

                                @Before
                                public void setUp() throws JFException {
                                    buyOrderEURUSD.setStopLossPrice(pfs.NO_STOP_LOSS_PRICE());
                                    sellOrderEURUSD.setStopLossPrice(pfs.NO_STOP_LOSS_PRICE());

                                    sendOrderEvent(buyOrderEURUSD, OrderEventType.SL_CHANGE_OK);
                                    sendOrderEvent(sellOrderEURUSD, OrderEventType.SL_CHANGE_OK);

                                    verify(orderUtilMock).merge(eq(mergeLabel), any());
                                }

                                @Test
                                public void testIsBusyBecauseOfMerge() {
                                    assertTrue(position.isBusy());
                                }

                                public class AfterMergeCloseOKMessage {

                                    @Before
                                    public void setUp() throws JFException {
                                        buyOrderEURUSD.setState(IOrder.State.CLOSED);
                                        sellOrderEURUSD.setState(IOrder.State.CLOSED);
                                        mergeOrderEURUSD.setState(IOrder.State.CLOSED);
                                        sendOrderEvent(buyOrderEURUSD, OrderEventType.CLOSED_BY_MERGE);
                                        sendOrderEvent(sellOrderEURUSD, OrderEventType.CLOSED_BY_MERGE);
                                        sendOrderEvent(mergeOrderEURUSD, OrderEventType.MERGE_CLOSE_OK);
                                    }

                                    @Test
                                    public void testIsNotBusy() {
                                        assertFalse(position.isBusy());
                                    }

                                    @Test
                                    public void testPositionHasNoOrder() {
                                        assertTrue(isRepositoryEmpty());
                                    }
                                }

                                public class AfterMergeOKMessage {

                                    @Before
                                    public void setUp() throws JFException {
                                        buyOrderEURUSD.setState(IOrder.State.CLOSED);
                                        sellOrderEURUSD.setState(IOrder.State.CLOSED);
                                        mergeOrderEURUSD.setState(IOrder.State.FILLED);
                                        sendOrderEvent(buyOrderEURUSD, OrderEventType.CLOSED_BY_MERGE);
                                        sendOrderEvent(sellOrderEURUSD, OrderEventType.CLOSED_BY_MERGE);
                                        sendOrderEvent(mergeOrderEURUSD, OrderEventType.MERGE_OK);

                                        verify(orderUtilMock).changeSL(mergeOrderEURUSD, restoreSL);
                                    }

                                    @Test
                                    public void testIsBusyBecauseOfMerge() {
                                        assertTrue(position.isBusy());
                                    }

                                    @Test
                                    public void testPositionHasOnlyMergeOrder() {
                                        assertTrue(positionHasOrder(mergeOrderEURUSD));
                                        assertTrue(position.filter(order -> true).size() == 1);
                                    }

                                    public class AfterRestoreSL {

                                        @Before
                                        public void setUp() throws JFException {
                                            mergeOrderEURUSD.setStopLossPrice(restoreSL);
                                            sendOrderEvent(mergeOrderEURUSD, OrderEventType.SL_CHANGE_OK);

                                            verify(orderUtilMock).changeTP(mergeOrderEURUSD, restoreTP);
                                        }

                                        @Test
                                        public void testIsBusyBecauseOfMerge() {
                                            assertTrue(position.isBusy());
                                        }

                                        public class AfterRestoreTP {

                                            @Before
                                            public void setUp() throws JFException {
                                                mergeOrderEURUSD.setTakeProfitPrice(restoreTP);
                                                sendOrderEvent(mergeOrderEURUSD, OrderEventType.TP_CHANGE_OK);
                                            }

                                            @Test
                                            public void testIsNotBusy() {
                                                assertFalse(position.isBusy());
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                public class AfterMergeCall {

                    @Before
                    public void setUp() {
                        position.merge(mergeLabel);
                    }

                    @Test
                    public void testIsNotBusy() {
                        assertFalse(position.isBusy());
                    }

                    @Test
                    public void testNoOrderUtilInteractions() {
                        verifyNoMoreInteractions(orderUtilMock);
                    }
                }

                public class AfterCloseCall {

                    @Before
                    public void setUp() {
                        position.close();

                        verify(orderUtilMock).close(buyOrderEURUSD);
                    }

                    @Test
                    public void testIsBusy() {
                        assertTrue(position.isBusy());
                    }

                    @Test
                    public void testRepositoryHoldsStillBuyOrder() {
                        assertTrue(positionHasOrder(buyOrderEURUSD));
                    }

                    public class AfterCloseOKMessage {

                        @Before
                        public void setUp() {
                            buyOrderEURUSD.setState(IOrder.State.CLOSED);

                            sendOrderEvent(buyOrderEURUSD, OrderEventType.CLOSE_OK);
                        }

                        @Test
                        public void testIsNotBusy() {
                            assertFalse(position.isBusy());
                        }

                        @Test
                        public void testPositionHasNoOrder() {
                            assertTrue(isRepositoryEmpty());
                        }
                    }
                }
            }
        }

        public class AfterSubmitRejectMessage {

            @Before
            public void setUp() {
                buyOrderEURUSD.setState(IOrder.State.CANCELED);

                sendOrderEvent(buyOrderEURUSD, OrderEventType.SUBMIT_REJECTED);
            }

            @Test
            public void testIsNotBusy() {
                assertFalse(position.isBusy());
            }

            @Test
            public void testPositionHasNoOrder() {
                assertTrue(isRepositoryEmpty());
            }
        }
    }
}
