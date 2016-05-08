package com.jforex.programming.position.test;

import static com.jforex.programming.misc.JForexUtil.pfs;
import static com.jforex.programming.misc.JForexUtil.uss;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.google.common.base.Supplier;
import com.google.common.collect.Sets;
import com.jforex.programming.misc.ConcurrentUtil;
import com.jforex.programming.order.OrderParams;
import com.jforex.programming.order.OrderUtil;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.position.Position;
import com.jforex.programming.position.PositionTaskRejectException;
import com.jforex.programming.position.RestoreSLTPPolicy;
import com.jforex.programming.test.common.InstrumentUtilForTest;
import com.jforex.programming.test.common.OrderParamsForTest;
import com.jforex.programming.test.fakes.IOrderForTest;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.JFException;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

@RunWith(HierarchicalContextRunner.class)
public class PositionTest extends InstrumentUtilForTest {

    private Position position;

    @Mock
    private RestoreSLTPPolicy restoreSLTPPolicyMock;
    @Mock
    private OrderUtil orderUtilMock;
    @Mock
    private ConcurrentUtil concurrentUtilMock;
    private Subject<OrderEvent, OrderEvent> orderEventSubject;
    private Subject<Long, Long> timerSubject;
    private OrderParams orderParamsBuy;
    private OrderParams orderParamsSell;
    private Set<IOrder> toMergeOrders;
    private String mergeLabel;
    private final IOrderForTest buyOrder = IOrderForTest.buyOrderEURUSD();
    private final IOrderForTest sellOrder = IOrderForTest.sellOrderEURUSD();
    private final IOrderForTest mergeOrder = IOrderForTest.buyOrderEURUSD();
    private final double restoreSL = 1.12345;
    private final double restoreTP = 1.12543;

    @Before
    public void setUp() throws JFException {
        initCommonTestFramework();

        orderEventSubject = PublishSubject.create();
        timerSubject = PublishSubject.create();
        orderParamsBuy = OrderParamsForTest.paramsBuyEURUSD();
        orderParamsSell = OrderParamsForTest.paramsSellEURUSD();
        toMergeOrders = Sets.newHashSet(buyOrder, sellOrder);
        mergeLabel = uss.ORDER_MERGE_LABEL_PREFIX() + orderParamsBuy.label();
        mergeOrder.setLabel(mergeLabel);
        setUpMocks();

        position = new Position(instrumentEURUSD,
                                orderUtilMock,
                                orderEventSubject,
                                restoreSLTPPolicyMock,
                                concurrentUtilMock);
    }

    private void setUpMocks() {
        when(restoreSLTPPolicyMock.restoreSL(any())).thenReturn(restoreSL);
        when(restoreSLTPPolicyMock.restoreTP(any())).thenReturn(restoreTP);
        when(concurrentUtilMock.timerObservable(1500L, TimeUnit.MILLISECONDS)).thenReturn(timerSubject);
    }

    private Subject<OrderEvent, OrderEvent> setUpSubmit(final OrderParams orderParams) {
        return setUpObservable(() -> orderUtilMock.submit(orderParams));
    }

    private Subject<OrderEvent, OrderEvent> setUpSL(final IOrder order,
                                                    final double newSL) {
        return setUpObservable(() -> orderUtilMock.setSL(order, newSL));
    }

    private Subject<OrderEvent, OrderEvent> setUpTP(final IOrder order,
                                                    final double newTP) {
        return setUpObservable(() -> orderUtilMock.setTP(order, newTP));
    }

    private Subject<OrderEvent, OrderEvent> setUpMerge(final String mergeLabel,
                                                       final Collection<IOrder> toMergeOrders) {
        return setUpObservable(() -> orderUtilMock.merge(eq(mergeLabel), any()));
    }

    private Subject<OrderEvent, OrderEvent> setUpClose(final IOrder order) {
        return setUpObservable(() -> orderUtilMock.close(order));
    }

    private Subject<OrderEvent, OrderEvent> setUpObservable(final Supplier<Observable<OrderEvent>> orderEventSupplier) {
        final Subject<OrderEvent, OrderEvent> orderEventSubject = PublishSubject.create();
        when(orderEventSupplier.get()).thenReturn(orderEventSubject);
        return orderEventSubject;
    }

    private boolean isRepositoryEmpty() {
        return position.filter(order -> true).isEmpty();
    }

    private boolean positionHasOrder(final IOrder orderToFind) {
        return position.filter(order -> order.getLabel().equals(orderToFind.getLabel())).size() == 1;
    }

    @Test
    public void testCloseOnEmptyPositionIsIgnored() {
        position.close();
    }

    @Test
    public void testMultipleSubmitCallsAreNotBlocked() {
        setUpSubmit(orderParamsBuy);

        position.submit(orderParamsBuy);
        position.submit(orderParamsBuy);

        verify(orderUtilMock, times(2)).submit(orderParamsBuy);
    }

    public class SubmitWithException {

        @Before
        public void setUp() {
            setUpSubmit(orderParamsBuy);

            position.submit(orderParamsBuy);
        }

        @Test
        public void testSubmitOnOrderUtilIsCalled() {
            verify(orderUtilMock).submit(orderParamsBuy);
        }

        @Test
        public void testPositionHasNoOrder() {
            assertTrue(isRepositoryEmpty());
        }
    }

    public class SubmitNoException {

        protected Subject<OrderEvent, OrderEvent> buySubmitSubject;

        @Before
        public void setUp() {
            buyOrder.setState(IOrder.State.CREATED);
            buySubmitSubject = setUpSubmit(orderParamsBuy);

            position.submit(orderParamsBuy);

            verify(orderUtilMock).submit(orderParamsBuy);
        }

        @Test
        public void testPositionHasNoOrder() {
            assertTrue(isRepositoryEmpty());
        }

        public class SubmitOKMessage {

            @Before
            public void setUp() {
                buyOrder.setState(IOrder.State.OPENED);
            }

            public class FillRejectMessage {

                @Before
                public void setUp() {
                    buyOrder.setState(IOrder.State.CANCELED);

                    buySubmitSubject.onError(new PositionTaskRejectException("", null));
                }

                @Test
                public void testPositionHasNoOrder() {
                    assertTrue(isRepositoryEmpty());
                }
            }

            public class FullFillMessage {

                @Before
                public void setUp() {
                    buyOrder.setState(IOrder.State.FILLED);

                    buySubmitSubject.onNext(new OrderEvent(buyOrder, OrderEventType.FULL_FILL_OK));
                    buySubmitSubject.onCompleted();
                }

                @Test
                public void testPositionHasBuyOrder() {
                    assertTrue(positionHasOrder(buyOrder));
                }

                @Test
                public void testMultipleCloseCallsAreBlocked() {
                    setUpClose(buyOrder);

                    position.close();
                    position.close();

                    verify(orderUtilMock).close(buyOrder);
                }

                public class CloseOnSL {

                    @Before
                    public void setUp() {
                        buyOrder.setState(IOrder.State.CLOSED);

                        orderEventSubject.onNext(new OrderEvent(buyOrder, OrderEventType.CLOSED_BY_SL));
                    }

                    @Test
                    public void testPositionHasNoOrder() {
                        assertTrue(isRepositoryEmpty());
                    }
                }

                public class CloseOnTP {

                    @Before
                    public void setUp() {
                        buyOrder.setState(IOrder.State.CLOSED);

                        orderEventSubject.onNext(new OrderEvent(buyOrder, OrderEventType.CLOSED_BY_TP));
                    }

                    @Test
                    public void testPositionHasNoOrder() {
                        assertTrue(isRepositoryEmpty());
                    }
                }

                public class SecondSubmit {

                    protected Subject<OrderEvent, OrderEvent> sellSubmitSubject;

                    @Before
                    public void setUp() {
                        sellOrder.setState(IOrder.State.CREATED);
                        sellSubmitSubject = setUpSubmit(orderParamsSell);

                        position.submit(orderParamsSell);

                        verify(orderUtilMock).submit(orderParamsSell);
                    }

                    public class FullFillMessageForSecondOrder {

                        @Before
                        public void setUp() {
                            sellOrder.setState(IOrder.State.FILLED);

                            sellSubmitSubject.onNext(new OrderEvent(sellOrder, OrderEventType.FULL_FILL_OK));
                            sellSubmitSubject.onCompleted();
                        }

                        @Test
                        public void testPositionHasBuyAndSellOrder() {
                            assertTrue(positionHasOrder(buyOrder));
                            assertTrue(positionHasOrder(sellOrder));
                        }

                        @Test
                        public void testMultipleMergeCallsAreBlocked() {
                            setUpMerge(mergeLabel, toMergeOrders);

                            position.merge(mergeLabel);
                            position.merge(mergeLabel);

                            // verify(orderUtilMock).merge(eq(mergeLabel),
                            // any());
                        }

                        public class MergeCall {

                            protected Subject<OrderEvent, OrderEvent> buyRemoveTPSubject;
                            protected Subject<OrderEvent, OrderEvent> sellRemoveTPSubject;
                            protected Subject<OrderEvent, OrderEvent> buyRemoveSLSubject;
                            protected Subject<OrderEvent, OrderEvent> sellRemoveSLSubject;
                            protected Subject<OrderEvent, OrderEvent> mergeSubject;
                            protected Subject<OrderEvent, OrderEvent> mergeRestoreSLSubject;
                            protected Subject<OrderEvent, OrderEvent> mergeRestoreTPSubject;

                            @Before
                            public void setUp() {
                                buyRemoveTPSubject = setUpTP(buyOrder, pfs.NO_TAKE_PROFIT_PRICE());
                                sellRemoveTPSubject = setUpTP(sellOrder, pfs.NO_TAKE_PROFIT_PRICE());
                                buyRemoveSLSubject = setUpSL(buyOrder, pfs.NO_STOP_LOSS_PRICE());
                                sellRemoveSLSubject = setUpSL(sellOrder, pfs.NO_STOP_LOSS_PRICE());
                                mergeSubject = setUpMerge(mergeLabel, toMergeOrders);
                                mergeRestoreSLSubject = setUpSL(mergeOrder, restoreSL);
                                mergeRestoreTPSubject = setUpTP(mergeOrder, restoreTP);

                                position.merge(mergeLabel);
                            }

                            @Test
                            public void testRemoveTPIsCalled() {
                                verify(orderUtilMock).setTP(buyOrder, pfs.NO_TAKE_PROFIT_PRICE());
                                verify(orderUtilMock).setTP(sellOrder, pfs.NO_TAKE_PROFIT_PRICE());
                            }

                            public class RemovedTPOnSellRejected {

                                @Before
                                public void setUp() {
                                    buyOrder.setTakeProfitPrice(pfs.NO_TAKE_PROFIT_PRICE());

                                    buyRemoveTPSubject.onNext(new OrderEvent(buyOrder, OrderEventType.TP_CHANGE_OK));
                                    buyRemoveTPSubject.onCompleted();
                                    final OrderEvent orderEvent =
                                            new OrderEvent(sellOrder, OrderEventType.CHANGE_TP_REJECTED);
                                    sellRemoveTPSubject.onError(new PositionTaskRejectException("", orderEvent));
                                    timerSubject.onNext(1L);
                                }

                                @Test
                                public void testRetryIsDoneOnRemoveTP() {
//                                    verify(orderUtilMock, times(2)).setTP(sellOrder,
//                                                                                    pfs.NO_TAKE_PROFIT_PRICE());
                                }
                            }

                            public class RemovedTPs {

                                @Before
                                public void setUp() {
                                    buyOrder.setTakeProfitPrice(pfs.NO_TAKE_PROFIT_PRICE());
                                    sellOrder.setTakeProfitPrice(pfs.NO_TAKE_PROFIT_PRICE());

                                    buyRemoveTPSubject.onNext(new OrderEvent(buyOrder, OrderEventType.TP_CHANGE_OK));
                                    buyRemoveTPSubject.onCompleted();
                                    sellRemoveTPSubject.onNext(new OrderEvent(sellOrder, OrderEventType.TP_CHANGE_OK));
                                    sellRemoveTPSubject.onCompleted();
                                }

                                @Test
                                public void testRemoveSLIsCalled() {
                                    verify(orderUtilMock).setSL(buyOrder, pfs.NO_STOP_LOSS_PRICE());
                                    verify(orderUtilMock).setSL(sellOrder, pfs.NO_STOP_LOSS_PRICE());
                                }

                                public class RemovedSLs {

                                    @Before
                                    public void setUp() {
                                        buyOrder.setStopLossPrice(pfs.NO_STOP_LOSS_PRICE());
                                        sellOrder.setStopLossPrice(pfs.NO_STOP_LOSS_PRICE());

                                        buyRemoveSLSubject
                                                .onNext(new OrderEvent(buyOrder, OrderEventType.SL_CHANGE_OK));
                                        buyRemoveSLSubject.onCompleted();
                                        sellRemoveSLSubject
                                                .onNext(new OrderEvent(sellOrder, OrderEventType.SL_CHANGE_OK));
                                        sellRemoveSLSubject.onCompleted();
                                    }

                                    @Test
                                    public void testMergeIsCalled() {
                                        verify(orderUtilMock).merge(eq(mergeLabel), any());
                                    }

                                    public class MergedOrders {

                                        @Before
                                        public void setUp() {
                                            buyOrder.setState(IOrder.State.CLOSED);
                                            sellOrder.setState(IOrder.State.CLOSED);
                                            mergeOrder.setState(IOrder.State.FILLED);

                                            mergeSubject.onNext(new OrderEvent(mergeOrder, OrderEventType.MERGE_OK));
                                            mergeSubject.onCompleted();
                                        }

                                        @Test
                                        public void testRestoreSLIsCalled() {
                                            verify(orderUtilMock).setSL(mergeOrder, restoreSL);
                                        }

                                        public class RestoredSL {

                                            @Before
                                            public void setUp() {
                                                mergeRestoreSLSubject
                                                        .onNext(new OrderEvent(mergeOrder,
                                                                               OrderEventType.SL_CHANGE_OK));
                                                mergeRestoreSLSubject.onCompleted();
                                            }

                                            @Test
                                            public void testRestoreTPIsCalled() {
                                                verify(orderUtilMock).setTP(mergeOrder, restoreTP);
                                            }

                                            public class RestoredTP {

                                                @Before
                                                public void setUp() {
                                                    mergeRestoreTPSubject
                                                            .onNext(new OrderEvent(mergeOrder,
                                                                                   OrderEventType.TP_CHANGE_OK));
                                                    mergeRestoreTPSubject.onCompleted();
                                                }

                                                @Test
                                                public void testMergeEventWasSent() {
                                                    // TODO
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                public class CloseCall {

                    protected Subject<OrderEvent, OrderEvent> closeSubject;

                    @Before
                    public void setUp() {
                        closeSubject = setUpClose(buyOrder);

                        position.close();

                        verify(orderUtilMock).close(buyOrder);
                    }

                    @Test
                    public void testRepositoryHoldsStillBuyOrder() {
                        assertTrue(positionHasOrder(buyOrder));
                    }

                    public class CloseOKMessage {

                        @Before
                        public void setUp() {
                            buyOrder.setState(IOrder.State.CLOSED);

                            closeSubject.onNext(new OrderEvent(buyOrder, OrderEventType.CLOSE_OK));
                            closeSubject.onCompleted();
                        }

                        @Test
                        public void testPositionHasNoOrder() {
                            assertTrue(isRepositoryEmpty());
                        }
                    }

                    public class CloseRejected {

                        @Before
                        public void setUp() {
                            buyOrder.setState(IOrder.State.FILLED);

                            final OrderEvent orderEvent =
                                    new OrderEvent(buyOrder, OrderEventType.CLOSE_REJECTED);
                            closeSubject.onError(new PositionTaskRejectException("", orderEvent));
                            timerSubject.onNext(1L);
                        }

                        @Test
                        public void testRetryIsDoneForClose() {
                            verify(orderUtilMock, times(2)).close(buyOrder);
                        }
                    }
                }
            }
        }

        public class OnRejectMessage {

            @Before
            public void setUp() {
                buyOrder.setState(IOrder.State.CANCELED);

                buySubmitSubject.onError(new PositionTaskRejectException("", null));
            }

            @Test
            public void testPositionHasNoOrder() {
                assertTrue(isRepositoryEmpty());
            }
        }
    }
}
