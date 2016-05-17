package com.jforex.programming.position.test;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.aeonbits.owner.ConfigFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.JFException;
import com.google.common.collect.Sets;
import com.jforex.programming.misc.ConcurrentUtil;
import com.jforex.programming.order.OrderParams;
import com.jforex.programming.order.OrderUtil;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.position.Position;
import com.jforex.programming.position.PositionTask;
import com.jforex.programming.position.RestoreSLTPPolicy;
import com.jforex.programming.settings.PlatformSettings;
import com.jforex.programming.test.common.InstrumentUtilForTest;
import com.jforex.programming.test.common.OrderParamsForTest;
import com.jforex.programming.test.fakes.IOrderForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import rx.Completable;
import rx.Observable;
import rx.observers.TestSubscriber;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

@RunWith(HierarchicalContextRunner.class)
public class PositionTest extends InstrumentUtilForTest {

    private Position position;

    private final static PlatformSettings platformSettings = ConfigFactory.create(PlatformSettings.class);
    @Mock private RestoreSLTPPolicy restoreSLTPPolicyMock;
    @Mock private OrderUtil orderUtilMock;
    @Mock private PositionTask positionTaskMock;
    @Mock private ConcurrentUtil concurrentUtilMock;
    private final Subject<OrderEvent, OrderEvent> orderEventSubject = PublishSubject.create();
    private final OrderParams orderParamsBuy = OrderParamsForTest.paramsBuyEURUSD();
    private final OrderParams orderParamsSell = OrderParamsForTest.paramsSellEURUSD();
    private final IOrderForTest buyOrder = IOrderForTest.buyOrderEURUSD();
    private final IOrderForTest sellOrder = IOrderForTest.sellOrderEURUSD();
    private final String mergeLabel = "MergeLabel";
    private final Set<IOrder> toMergeOrders = Sets.newHashSet(buyOrder, sellOrder);
    private final IOrderForTest mergeOrder = IOrderForTest.buyOrderEURUSD();
    private final double restoreSL = 1.12345;
    private final double restoreTP = 1.12543;
    private final double noSLPrice = platformSettings.noSLPrice();
    private final double noTPPrice = platformSettings.noTPPrice();

    @Before
    public void setUp() throws JFException {
        initCommonTestFramework();

        mergeOrder.setLabel(mergeLabel);
        setUpMocks();

        position = new Position(instrumentEURUSD,
                                orderUtilMock,
                                positionTaskMock,
                                orderEventSubject,
                                restoreSLTPPolicyMock,
                                concurrentUtilMock);
    }

    private void setUpMocks() {
        when(restoreSLTPPolicyMock.restoreSL(any())).thenReturn(restoreSL);
        when(restoreSLTPPolicyMock.restoreTP(any())).thenReturn(restoreTP);
        when(concurrentUtilMock.timerObservable(platformSettings.delayOnOrderFailRetry(), TimeUnit.MILLISECONDS))
                .thenReturn(Observable.just(0L));
    }

    private void sendOrderEvent(final IOrder order,
                                final OrderEventType orderEventType) {
        final OrderEvent orderEvent = new OrderEvent(order, orderEventType);
        orderEventSubject.onNext(orderEvent);
    }

    private boolean positionHasOrder(final IOrder orderToFind) {
        return position.filter(order -> order.getLabel().equals(orderToFind.getLabel())).size() == 1;
    }

    private boolean isRepositoryEmpty() {
        return position.filter(order -> true).isEmpty();
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
    public void testCloseOnEmptyPositionDoesNotCallOnPositionTask() {
        final TestSubscriber<OrderEvent> closeSubscriber = new TestSubscriber<>();

        position.close().subscribe(closeSubscriber);

        verifyZeroInteractions(positionTaskMock);
        assertSubscriberCompleted(closeSubscriber);
    }

    public class SubmitSetup {

        protected final TestSubscriber<OrderEvent> buySubmitSubscriber = new TestSubscriber<>();
        protected Runnable buySubmitCall =
                () -> position.submit(orderParamsBuy).subscribe(buySubmitSubscriber);

        public class SubmitInProcess {

            @Before
            public void setUp() {
                buyOrder.setState(IOrder.State.CREATED);

                when(positionTaskMock.submitObservable(orderParamsBuy)).thenReturn(Observable.never());

                buySubmitCall.run();
            }

            @Test
            public void testSubmitIsCalledPositionTask() {
                verify(positionTaskMock).submitObservable(orderParamsBuy);
            }

            @Test
            public void testSubmitSubscriberNotYetCompleted() {
                assertSubscriberNotYetCompleted(buySubmitSubscriber);
            }

            @Test
            public void testPositionHasNoOrder() {
                assertTrue(isRepositoryEmpty());
            }

            public class SecondSubmitCallIsOK {

                protected final TestSubscriber<OrderEvent> sellsubmitSubscriber = new TestSubscriber<>();

                @Before
                public void setUp() {
                    sellOrder.setState(IOrder.State.FILLED);

                    when(positionTaskMock.submitObservable(orderParamsSell)).thenReturn(Observable.just(sellOrder));

                    position.submit(orderParamsSell).subscribe(sellsubmitSubscriber);
                }

                @Test
                public void testPositionHasSellOrder() {
                    assertTrue(positionHasOrder(sellOrder));
                }

                @Test
                public void testSubmitSubscriberCompleted() {
                    assertSubscriberCompleted(sellsubmitSubscriber);
                }
            }
        }

        public class SubmitFail {

            @Before
            public void setUp() {
                when(positionTaskMock.submitObservable(orderParamsBuy)).thenReturn(Observable.error(jfException));

                buySubmitCall.run();
            }

            @Test
            public void testPositionHasNoOrder() {
                assertTrue(isRepositoryEmpty());
            }

            @Test
            public void testSubmitSubscriberCompletedWithError() {
                buySubmitSubscriber.assertError(JFException.class);
            }
        }

        public class BuySubmitOK {

            @Before
            public void setUp() {
                buyOrder.setState(IOrder.State.FILLED);

                when(positionTaskMock.submitObservable(orderParamsBuy)).thenReturn(Observable.just(buyOrder));

                buySubmitCall.run();
            }

            @Test
            public void testPositionHasBuyOrder() {
                assertTrue(positionHasOrder(buyOrder));
            }

            @Test
            public void testSubmitSubscriberCompleted() {
                assertSubscriberCompleted(buySubmitSubscriber);
            }

            @Test
            public void testMergeCallIsIgnored() {
                final TestSubscriber<OrderEvent> mergeSubscriber = new TestSubscriber<>();

                position.merge(mergeLabel).subscribe(mergeSubscriber);

                verify(positionTaskMock, never()).mergeObservable(eq(mergeLabel), any());
                assertSubscriberCompleted(mergeSubscriber);
            }

            public class SellSubmitOK {

                protected final TestSubscriber<OrderEvent> sellSubmitSubscriber = new TestSubscriber<>();

                @Before
                public void setUp() {
                    sellOrder.setState(IOrder.State.FILLED);

                    when(positionTaskMock.submitObservable(orderParamsSell)).thenReturn(Observable.just(sellOrder));

                    position.submit(orderParamsSell).subscribe(sellSubmitSubscriber);
                }

                @Test
                public void testPositionHasBuyAndSellOrder() {
                    assertTrue(positionHasOrder(buyOrder));
                    assertTrue(positionHasOrder(sellOrder));
                }

                @Test
                public void testSellSubmitSubscriberCompleted() {
                    assertSubscriberCompleted(sellSubmitSubscriber);
                }
            }

            public class CloseSetup {

                protected final TestSubscriber<OrderEvent> closeSubscriber = new TestSubscriber<>();
                protected Runnable closeCall = () -> position.close().subscribe(closeSubscriber);

                public class CloseInProcess {

                    @Before
                    public void setUp() {
                        when(positionTaskMock.closeCompletable(buyOrder)).thenReturn(Completable.never());

                        closeCall.run();
                    }

                    @Test
                    public void testCloseIsCalledPositionTask() {
                        verify(positionTaskMock).closeCompletable(buyOrder);
                    }

                    @Test
                    public void testCloseSubscriberNotYetCompleted() {
                        assertSubscriberNotYetCompleted(closeSubscriber);
                    }

                    @Test
                    public void testPositionHasBuyOrder() {
                        assertTrue(positionHasOrder(buyOrder));
                    }
                }

                public class CloseBuyOK {

                    @Before
                    public void setUp() {
                        when(positionTaskMock.closeCompletable(buyOrder)).thenReturn(Completable.complete());

                        closeCall.run();

                        buyOrder.setState(IOrder.State.CLOSED);
                        sendOrderEvent(buyOrder, OrderEventType.CLOSE_OK);
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
                        when(positionTaskMock.closeCompletable(buyOrder))
                                .thenReturn(Completable.error(jfException));

                        closeCall.run();
                    }

                    @Test
                    public void testPositionHasStillBuyOrder() {
                        assertTrue(positionHasOrder(buyOrder));
                    }

                    @Test
                    public void testSubmitSubscriberCompletedWithError() {
                        closeSubscriber.assertError(JFException.class);
                    }
                }
            }
        }
    }
}