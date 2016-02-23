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
import com.jforex.programming.order.OrderChangeResult;
import com.jforex.programming.order.OrderCreateResult;
import com.jforex.programming.order.OrderParams;
import com.jforex.programming.order.OrderUtil;
import com.jforex.programming.order.call.OrderCallRequest;
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
    private OrderCreateResult callResultWithExceptionEURUSDBuy;

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

        when(orderUtilMock.submit(orderParamsEURUSDBuy)).thenReturn(new OrderCreateResult(Optional.of(buyOrderEURUSD),
                                                                                          emptyJFExceptionOpt,
                                                                                          OrderCallRequest.SUBMIT));
        when(orderUtilMock.submit(orderParamsEURUSDSell)).thenReturn(new OrderCreateResult(Optional.of(sellOrderEURUSD),
                                                                                           emptyJFExceptionOpt,
                                                                                           OrderCallRequest.SUBMIT));
        when(orderUtilMock.setSL(eq(buyOrderEURUSD),
                                 anyDouble())).thenReturn(new OrderChangeResult(buyOrderEURUSD,
                                                                                emptyJFExceptionOpt,
                                                                                OrderCallRequest.CHANGE_SL));
        when(orderUtilMock.setSL(eq(sellOrderEURUSD),
                                 anyDouble())).thenReturn(new OrderChangeResult(sellOrderEURUSD,
                                                                                emptyJFExceptionOpt,
                                                                                OrderCallRequest.CHANGE_SL));
        when(orderUtilMock.setTP(eq(buyOrderEURUSD),
                                 anyDouble())).thenReturn(new OrderChangeResult(buyOrderEURUSD,
                                                                                emptyJFExceptionOpt,
                                                                                OrderCallRequest.CHANGE_TP));
        when(orderUtilMock.setTP(eq(sellOrderEURUSD),
                                 anyDouble())).thenReturn(new OrderChangeResult(sellOrderEURUSD,
                                                                                emptyJFExceptionOpt,
                                                                                OrderCallRequest.CHANGE_TP));
        when(orderUtilMock.merge(eq(mergeLabel),
                                 any())).thenReturn(new OrderCreateResult(Optional.of(mergeOrderEURUSD),
                                                                          emptyJFExceptionOpt,
                                                                          OrderCallRequest.MERGE));
        when(orderUtilMock.setSL(eq(mergeOrderEURUSD),
                                 anyDouble())).thenReturn(new OrderChangeResult(mergeOrderEURUSD,
                                                                                emptyJFExceptionOpt,
                                                                                OrderCallRequest.CHANGE_SL));
        when(orderUtilMock.setTP(eq(mergeOrderEURUSD),
                                 anyDouble())).thenReturn(new OrderChangeResult(mergeOrderEURUSD,
                                                                                emptyJFExceptionOpt,
                                                                                OrderCallRequest.CHANGE_TP));
        when(orderUtilMock.close(buyOrderEURUSD)).thenReturn(new OrderChangeResult(buyOrderEURUSD,
                                                                                   emptyJFExceptionOpt,
                                                                                   OrderCallRequest.CLOSE));
        when(orderUtilMock.close(sellOrderEURUSD)).thenReturn(new OrderChangeResult(sellOrderEURUSD,
                                                                                    emptyJFExceptionOpt,
                                                                                    OrderCallRequest.CLOSE));
        when(orderUtilMock.close(mergeOrderEURUSD)).thenReturn(new OrderChangeResult(mergeOrderEURUSD,
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

    public class SubmitWithException {

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

    public class SubmitAndMergeWithException {

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

    public class SubmitAndMergeNoException {

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

        public class SubmitOKMessage {

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

            public class FullFillMessage {

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

    public class SubmitNoException {

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

        public class SubmitOKMessage {

            @Before
            public void setUp() {
                buyOrderEURUSD.setState(IOrder.State.OPENED);

                sendOrderEvent(buyOrderEURUSD, OrderEventType.SUBMIT_OK);
            }

            @Test
            public void testIsBusy() {
                assertTrue(position.isBusy());
            }

            public class FillRejectMessage {

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

            public class FullFillMessage {

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

                public class CloseOnSL {

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

                public class CloseOnTP {

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

                public class SecondSubmit {

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

                    public class FullFillMessageForSecondOrder {

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

                public class SecondSubmitAndMerge {

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

                    public class FillRejectMessage {

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

                    public class FullFillMessageForSecondOrder {

                        @Before
                        public void setUp() {
                            sellOrderEURUSD.setState(IOrder.State.FILLED);
                            sendOrderEvent(sellOrderEURUSD, OrderEventType.FULL_FILL_OK);

                            verify(orderUtilMock).setTP(buyOrderEURUSD, pfs.NO_TAKE_PROFIT_PRICE());
                            verify(orderUtilMock).setTP(sellOrderEURUSD, pfs.NO_TAKE_PROFIT_PRICE());
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

                        public class RemoveTPs {

                            @Before
                            public void setUp() throws JFException {
                                buyOrderEURUSD.setTakeProfitPrice(pfs.NO_TAKE_PROFIT_PRICE());
                                sellOrderEURUSD.setTakeProfitPrice(pfs.NO_TAKE_PROFIT_PRICE());

                                sendOrderEvent(buyOrderEURUSD, OrderEventType.TP_CHANGE_OK);
                                sendOrderEvent(sellOrderEURUSD, OrderEventType.TP_CHANGE_OK);

                                verify(orderUtilMock).setSL(buyOrderEURUSD, pfs.NO_STOP_LOSS_PRICE());
                                verify(orderUtilMock).setSL(sellOrderEURUSD, pfs.NO_STOP_LOSS_PRICE());
                            }

                            @Test
                            public void testIsBusyBecauseOfMerge() {
                                assertTrue(position.isBusy());
                            }

                            public class RemoveSLs {

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

                                public class MergeCloseOKMessage {

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

                                public class MergeOKMessage {

                                    @Before
                                    public void setUp() throws JFException {
                                        buyOrderEURUSD.setState(IOrder.State.CLOSED);
                                        sellOrderEURUSD.setState(IOrder.State.CLOSED);
                                        mergeOrderEURUSD.setState(IOrder.State.FILLED);
                                        sendOrderEvent(buyOrderEURUSD, OrderEventType.CLOSED_BY_MERGE);
                                        sendOrderEvent(sellOrderEURUSD, OrderEventType.CLOSED_BY_MERGE);
                                        sendOrderEvent(mergeOrderEURUSD, OrderEventType.MERGE_OK);

                                        verify(orderUtilMock).setSL(mergeOrderEURUSD, restoreSL);
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

                                    public class RestoreSL {

                                        @Before
                                        public void setUp() throws JFException {
                                            mergeOrderEURUSD.setStopLossPrice(restoreSL);
                                            sendOrderEvent(mergeOrderEURUSD, OrderEventType.SL_CHANGE_OK);

                                            verify(orderUtilMock).setTP(mergeOrderEURUSD, restoreTP);
                                        }

                                        @Test
                                        public void testIsBusyBecauseOfMerge() {
                                            assertTrue(position.isBusy());
                                        }

                                        public class RestoreTP {

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

                public class MergeCall {

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

                public class CloseCall {

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

                    public class CloseOKMessage {

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

        public class SubmitRejectMessage {

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
