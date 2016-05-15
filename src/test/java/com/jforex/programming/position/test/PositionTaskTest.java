package com.jforex.programming.position.test;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.concurrent.TimeUnit;

import org.aeonbits.owner.ConfigFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.jforex.programming.misc.CalculationUtil;
import com.jforex.programming.misc.ConcurrentUtil;
import com.jforex.programming.order.OrderUtil;
import com.jforex.programming.order.call.OrderCallRejectException;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.position.PositionTask;
import com.jforex.programming.settings.PlatformSettings;
import com.jforex.programming.test.common.InstrumentUtilForTest;
import com.jforex.programming.test.fakes.IOrderForTest;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.JFException;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import rx.Observable;
import rx.observers.TestSubscriber;

@RunWith(HierarchicalContextRunner.class)
public class PositionTaskTest extends InstrumentUtilForTest {

    private PositionTask positionTask;

    @Mock
    private OrderUtil orderUtilMock;
    @Mock
    private ConcurrentUtil concurrentUtilMock;
    private final IOrderForTest buyOrder = IOrderForTest.buyOrderEURUSD();
    // private final int noOfRetries = platformSettings.maxRetriesOnOrderFail();
    private OrderCallRejectException rejectException;

    private final static PlatformSettings platformSettings = ConfigFactory.create(PlatformSettings.class);
    // private static final Logger logger =
    // LogManager.getLogger(Position.class);

    @Before
    public void setUp() throws JFException {
        initCommonTestFramework();
        setUpMocks();
        final OrderEvent rejectEvent = new OrderEvent(buyOrder,
                                                      OrderEventType.CHANGE_SL_REJECTED);
        rejectException = new OrderCallRejectException("", rejectEvent);

        positionTask = new PositionTask(instrumentEURUSD,
                                        orderUtilMock,
                                        concurrentUtilMock);
    }

    private void setUpMocks() {
        when(concurrentUtilMock.timerObservable(platformSettings.delayOnOrderFailRetry(),
                                                TimeUnit.MILLISECONDS)).thenReturn(Observable.just(0L));
    }

    public class SetSLCompletableSetup {

        protected TestSubscriber<?> setSLSubscriber;
        protected final double orderSL = buyOrder.getStopLossPrice();

        @Before
        public void setUp() {
            setSLSubscriber = new TestSubscriber<>();
            buyOrder.setState(IOrder.State.FILLED);
        }

        public class WhenToSetSLEqualsOrderSL {

            @Before
            public void setUp() {
                positionTask.setSLCompletable(buyOrder, orderSL).subscribe(setSLSubscriber);
            }

            @Test
            public void testNoCallOnOrderUtil() {
                verifyZeroInteractions(orderUtilMock);
            }

            @Test
            public void testSubscriberCompletes() {
                setSLSubscriber.assertNoErrors();
                setSLSubscriber.assertCompleted();
            }
        }

        public class WhenToSetSLIsNotEqualOrderSL {

            protected double toSetSL = CalculationUtil.addPips(instrumentEURUSD, orderSL, 5.3);

            public class SLChangeOK {

                @Before
                public void setUp() {
                    when(orderUtilMock.setStopLossPrice(buyOrder, toSetSL)).thenReturn(Observable.empty());

                    positionTask.setSLCompletable(buyOrder, toSetSL).subscribe(setSLSubscriber);
                }

                @Test
                public void testSetSLOnOrderUtilIsCalled() {
                    verify(orderUtilMock).setStopLossPrice(buyOrder, toSetSL);
                }

                @Test
                public void testSubscriberIsCompleted() {
                    setSLSubscriber.assertNoErrors();
                    setSLSubscriber.assertCompleted();
                }
            }

            public class SLChangeReject {

                @Before
                public void setUp() {
                    when(orderUtilMock.setStopLossPrice(buyOrder, toSetSL))
                            .thenReturn(Observable.error(rejectException))
                            .thenReturn(Observable.empty());

                    positionTask.setSLCompletable(buyOrder, toSetSL).subscribe(setSLSubscriber);
                }

                @Test
                public void testRetryCallIsDone() {
                    verify(orderUtilMock, times(2)).setStopLossPrice(buyOrder, toSetSL);
                }

                @Test
                public void testSubscriberIsCompleted() {
                    setSLSubscriber.assertNoErrors();
                    setSLSubscriber.assertCompleted();
                }
            }

            public class TwoSLChangeRejects {

                @Before
                public void setUp() {
                    when(orderUtilMock.setStopLossPrice(buyOrder, toSetSL))
                            .thenReturn(Observable.error(rejectException))
                            .thenReturn(Observable.error(rejectException))
                            .thenReturn(Observable.empty());

                    positionTask.setSLCompletable(buyOrder, toSetSL).subscribe(setSLSubscriber);
                }

                @Test
                public void testRetryCallIsDone() {
                    verify(orderUtilMock, times(3)).setStopLossPrice(buyOrder, toSetSL);
                }

                @Test
                public void testSubscriberIsCompleted() {
                    setSLSubscriber.assertNoErrors();
                    setSLSubscriber.assertCompleted();
                }

                @Test
                public void testWaitingForRetryIsDone() {
                    verify(concurrentUtilMock, times(2)).timerObservable(platformSettings.delayOnOrderFailRetry(),
                                                                         TimeUnit.MILLISECONDS);
                }
            }
        }
    }
}
