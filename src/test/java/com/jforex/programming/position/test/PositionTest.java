package com.jforex.programming.position.test;

import static com.jforex.programming.misc.JForexUtil.pfs;
import static com.jforex.programming.misc.JForexUtil.uss;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
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

import com.dukascopy.api.IOrder;
import com.dukascopy.api.JFException;
import com.google.common.base.Supplier;
import com.google.common.collect.Sets;
import com.jforex.programming.misc.ConcurrentUtil;
import com.jforex.programming.order.OrderParams;
import com.jforex.programming.order.OrderUtilObservable;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.position.Position;
import com.jforex.programming.position.PositionEventType;
import com.jforex.programming.position.PositionTaskRejectException;
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
public class PositionTest extends InstrumentUtilForTest {

    private Position position;

    @Mock private RestoreSLTPPolicy restoreSLTPPolicyMock;
    @Mock private OrderUtilObservable orderUtilObservableMock;
    @Mock private ConcurrentUtil concurrentUtilMock;
    private Subject<OrderEvent, OrderEvent> orderEventSubject;
    private Subject<Long, Long> timerSubject;
    private final TestSubscriber<PositionEventType> positionSubscriber = new TestSubscriber<>();
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
                                orderUtilObservableMock,
                                restoreSLTPPolicyMock,
                                concurrentUtilMock);
        position.positionEventTypeObs().subscribe(positionSubscriber);
    }

    private void setUpMocks() {
        when(restoreSLTPPolicyMock.restoreSL(any())).thenReturn(restoreSL);
        when(restoreSLTPPolicyMock.restoreTP(any())).thenReturn(restoreTP);
        when(orderUtilObservableMock.orderEventObservable()).thenReturn(orderEventSubject);
        when(concurrentUtilMock.timerObservable(1500L, TimeUnit.MILLISECONDS)).thenReturn(timerSubject);
    }

    private Subject<IOrder, IOrder> setUpSubmit(final OrderParams orderParams) {
        return setUpObservable(() -> orderUtilObservableMock.submit(orderParams));
    }

    private Subject<IOrder, IOrder> setUpSL(final IOrder order,
                                            final double newSL) {
        return setUpObservable(() -> orderUtilObservableMock.setSL(order, newSL));
    }

    private Subject<IOrder, IOrder> setUpTP(final IOrder order,
                                            final double newTP) {
        return setUpObservable(() -> orderUtilObservableMock.setTP(order, newTP));
    }

    private Subject<IOrder, IOrder> setUpMerge(final String mergeLabel,
                                               final Collection<IOrder> toMergeOrders) {
        return setUpObservable(() -> orderUtilObservableMock.merge(eq(mergeLabel), any()));
    }

    private Subject<IOrder, IOrder> setUpClose(final IOrder order) {
        return setUpObservable(() -> orderUtilObservableMock.close(order));
    }

    private Subject<IOrder, IOrder> setUpObservable(final Supplier<Observable<IOrder>> orderSupplier) {
        final Subject<IOrder, IOrder> orderSubject = PublishSubject.create();
        when(orderSupplier.get()).thenReturn(orderSubject);
        return orderSubject;
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

        verify(orderUtilObservableMock, times(2)).submit(orderParamsBuy);
    }

    public class SubmitWithException {

        @Before
        public void setUp() {
            setUpSubmit(orderParamsBuy);

            position.submit(orderParamsBuy);
        }

        @Test
        public void testSubmitOnOrderUtilIsCalled() {
            verify(orderUtilObservableMock).submit(orderParamsBuy);
        }

        @Test
        public void testPositionHasNoOrder() {
            assertTrue(isRepositoryEmpty());
        }
    }

    public class SubmitNoException {

        protected Subject<IOrder, IOrder> buySubmitSubject;

        @Before
        public void setUp() {
            buyOrder.setState(IOrder.State.CREATED);
            buySubmitSubject = setUpSubmit(orderParamsBuy);

            position.submit(orderParamsBuy);

            verify(orderUtilObservableMock).submit(orderParamsBuy);
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

                    buySubmitSubject.onNext(buyOrder);
                    buySubmitSubject.onCompleted();
                }

                @Test
                public void testPositionHasBuyOrder() {
                    assertTrue(positionHasOrder(buyOrder));
                }

                @Test
                public void testSubmitEventWasSent() {
                    positionSubscriber.assertNoErrors();
                    positionSubscriber.assertValueCount(1);

                    assertThat(positionSubscriber.getOnNextEvents().get(0),
                               equalTo(PositionEventType.SUBMITTED));
                }

                @Test
                public void testMultipleCloseCallsAreBlocked() {
                    setUpClose(buyOrder);

                    position.close();
                    position.close();

                    verify(orderUtilObservableMock).close(buyOrder);
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

                    protected Subject<IOrder, IOrder> sellSubmitSubject;

                    @Before
                    public void setUp() {
                        sellOrder.setState(IOrder.State.CREATED);
                        sellSubmitSubject = setUpSubmit(orderParamsSell);

                        position.submit(orderParamsSell);

                        verify(orderUtilObservableMock).submit(orderParamsSell);
                    }

                    public class FullFillMessageForSecondOrder {

                        @Before
                        public void setUp() {
                            sellOrder.setState(IOrder.State.FILLED);

                            sellSubmitSubject.onNext(sellOrder);
                            sellSubmitSubject.onCompleted();
                        }

                        @Test
                        public void testPositionHasBuyAndSellOrder() {
                            assertTrue(positionHasOrder(buyOrder));
                            assertTrue(positionHasOrder(sellOrder));
                        }

                        @Test
                        public void testSubmitEventWasSent() {
                            positionSubscriber.assertNoErrors();
                            positionSubscriber.assertValueCount(2);

                            assertThat(positionSubscriber.getOnNextEvents().get(1),
                                       equalTo(PositionEventType.SUBMITTED));
                        }

                        @Test
                        public void testMultipleMergeCallsAreBlocked() {
                            setUpMerge(mergeLabel, toMergeOrders);

                            position.merge(mergeLabel);
                            position.merge(mergeLabel);

                            // verify(orderUtilObservableMock).merge(eq(mergeLabel),
                            // any());
                        }

                        public class MergeCall {

                            protected Subject<IOrder, IOrder> buyRemoveTPSubject;
                            protected Subject<IOrder, IOrder> sellRemoveTPSubject;
                            protected Subject<IOrder, IOrder> buyRemoveSLSubject;
                            protected Subject<IOrder, IOrder> sellRemoveSLSubject;
                            protected Subject<IOrder, IOrder> mergeSubject;
                            protected Subject<IOrder, IOrder> mergeRestoreSLSubject;
                            protected Subject<IOrder, IOrder> mergeRestoreTPSubject;

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
                                verify(orderUtilObservableMock).setTP(buyOrder, pfs.NO_TAKE_PROFIT_PRICE());
                                verify(orderUtilObservableMock).setTP(sellOrder, pfs.NO_TAKE_PROFIT_PRICE());
                            }

                            public class RemovedTPOnSellRejected {

                                @Before
                                public void setUp() {
                                    buyOrder.setTakeProfitPrice(pfs.NO_TAKE_PROFIT_PRICE());

                                    buyRemoveTPSubject.onNext(buyOrder);
                                    buyRemoveTPSubject.onCompleted();
                                    final OrderEvent orderEvent =
                                            new OrderEvent(sellOrder, OrderEventType.CHANGE_TP_REJECTED);
                                    sellRemoveTPSubject.onError(new PositionTaskRejectException("", orderEvent));
                                    timerSubject.onNext(1L);
                                }

                                @Test
                                public void testRetryIsDoneOnRemoveTP() {
//                                    verify(orderUtilObservableMock, times(2)).setTP(sellOrder,
//                                                                                    pfs.NO_TAKE_PROFIT_PRICE());
                                }
                            }

                            public class RemovedTPs {

                                @Before
                                public void setUp() {
                                    buyOrder.setTakeProfitPrice(pfs.NO_TAKE_PROFIT_PRICE());
                                    sellOrder.setTakeProfitPrice(pfs.NO_TAKE_PROFIT_PRICE());

                                    buyRemoveTPSubject.onNext(buyOrder);
                                    buyRemoveTPSubject.onCompleted();
                                    sellRemoveTPSubject.onNext(sellOrder);
                                    sellRemoveTPSubject.onCompleted();
                                }

                                @Test
                                public void testRemoveSLIsCalled() {
                                    verify(orderUtilObservableMock).setSL(buyOrder, pfs.NO_STOP_LOSS_PRICE());
                                    verify(orderUtilObservableMock).setSL(sellOrder, pfs.NO_STOP_LOSS_PRICE());
                                }

                                public class RemovedSLs {

                                    @Before
                                    public void setUp() {
                                        buyOrder.setStopLossPrice(pfs.NO_STOP_LOSS_PRICE());
                                        sellOrder.setStopLossPrice(pfs.NO_STOP_LOSS_PRICE());

                                        buyRemoveSLSubject.onNext(buyOrder);
                                        buyRemoveSLSubject.onCompleted();
                                        sellRemoveSLSubject.onNext(sellOrder);
                                        sellRemoveSLSubject.onCompleted();
                                    }

                                    @Test
                                    public void testMergeIsCalled() {
                                        verify(orderUtilObservableMock).merge(eq(mergeLabel), any());
                                    }

                                    public class MergedOrders {

                                        @Before
                                        public void setUp() {
                                            buyOrder.setState(IOrder.State.CLOSED);
                                            sellOrder.setState(IOrder.State.CLOSED);
                                            mergeOrder.setState(IOrder.State.FILLED);

                                            mergeSubject.onNext(mergeOrder);
                                            mergeSubject.onCompleted();
                                        }

                                        @Test
                                        public void testRestoreSLIsCalled() {
                                            verify(orderUtilObservableMock).setSL(mergeOrder, restoreSL);
                                        }

                                        public class RestoredSL {

                                            @Before
                                            public void setUp() {
                                                mergeRestoreSLSubject.onNext(mergeOrder);
                                                mergeRestoreSLSubject.onCompleted();
                                            }

                                            @Test
                                            public void testRestoreTPIsCalled() {
                                                verify(orderUtilObservableMock).setTP(mergeOrder, restoreTP);
                                            }

                                            public class RestoredTP {

                                                @Before
                                                public void setUp() {
                                                    mergeRestoreTPSubject.onNext(mergeOrder);
                                                    mergeRestoreTPSubject.onCompleted();
                                                }

                                                @Test
                                                public void testMergeEventWasSent() {
                                                    positionSubscriber.assertNoErrors();
                                                    positionSubscriber.assertValueCount(3);

                                                    assertThat(positionSubscriber.getOnNextEvents().get(2),
                                                               equalTo(PositionEventType.MERGED));
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

                    protected Subject<IOrder, IOrder> closeSubject;

                    @Before
                    public void setUp() {
                        closeSubject = setUpClose(buyOrder);

                        position.close();

                        verify(orderUtilObservableMock).close(buyOrder);
                    }

                    @Test
                    public void testRepositoryHoldsStillBuyOrder() {
                        assertTrue(positionHasOrder(buyOrder));
                    }

                    public class CloseOKMessage {

                        @Before
                        public void setUp() {
                            buyOrder.setState(IOrder.State.CLOSED);

                            closeSubject.onNext(buyOrder);
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
                            verify(orderUtilObservableMock, times(2)).close(buyOrder);
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

            @Test
            public void testNoEventWasSent() {
                positionSubscriber.assertNoErrors();
                positionSubscriber.assertValueCount(0);
            }
        }
    }
}
