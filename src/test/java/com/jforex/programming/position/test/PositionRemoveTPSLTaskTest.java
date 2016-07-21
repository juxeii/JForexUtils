package com.jforex.programming.position.test;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.google.common.collect.Sets;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.position.PositionRemoveTPSLTask;
import com.jforex.programming.position.PositionSingleTask;
import com.jforex.programming.test.common.CommonUtilForTest;
import com.jforex.programming.test.fakes.IOrderForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import rx.Observable;
import rx.observers.TestSubscriber;

@RunWith(HierarchicalContextRunner.class)
public class PositionRemoveTPSLTaskTest extends CommonUtilForTest {

    private PositionRemoveTPSLTask positionMultiTask;

    @Mock
    private PositionSingleTask positionSingleTaskMock;

    @Before
    public void setUp() {
        positionMultiTask = new PositionRemoveTPSLTask(positionSingleTaskMock);
    }

    public class RemoveTPSLCompletableSetup {

        private final IOrderForTest buyOrder = IOrderForTest.buyOrderEURUSD();
        private final IOrderForTest sellOrder = IOrderForTest.sellOrderEURUSD();
        private final TestSubscriber<OrderEvent> taskSubscriber = new TestSubscriber<>();
        private final Set<IOrder> filledOrders = Sets.newHashSet(buyOrder, sellOrder);

        private final Runnable removeTPSLCall =
                () -> positionMultiTask
                        .observable(filledOrders)
                        .subscribe(taskSubscriber);

        private void setSLTaskMockResult(final IOrder order,
                                         final double newSL,
                                         final Observable<OrderEvent> observable) {
            when(positionSingleTaskMock.setSLObservable(order, newSL))
                    .thenReturn(observable);
        }

        private void setTPTaskMockResult(final IOrder order,
                                         final double newTP,
                                         final Observable<OrderEvent> observable) {
            when(positionSingleTaskMock.setTPObservable(order, newTP))
                    .thenReturn(observable);
        }

        @Before
        public void setUp() {
            buyOrder.setState(IOrder.State.FILLED);
            sellOrder.setState(IOrder.State.FILLED);
        }

        @Test
        public void testSubscriberNotYetCompleted() {
            setTPTaskMockResult(buyOrder, noTP, busyObservable());
            setSLTaskMockResult(buyOrder, noSL, busyObservable());

            removeTPSLCall.run();

            taskSubscriber.assertNotCompleted();
        }

        public class RemoveTPSLOnBuyOK {

            @Before
            public void setUp() {
                setTPTaskMockResult(buyOrder, noTP, doneObservable());
                setSLTaskMockResult(buyOrder, noSL, doneObservable());
            }

            public class RemoveTPSLOnSellOK {

                @Before
                public void setUp() {
                    setTPTaskMockResult(sellOrder, noTP, doneObservable());
                    setSLTaskMockResult(sellOrder, noSL, doneObservable());

                    removeTPSLCall.run();
                }

                @Test
                public void testRemoveTPSLOnSingleTaskForAllOrdersHasBeenCalled() {
                    verify(positionSingleTaskMock).setTPObservable(buyOrder, noTP);
                    verify(positionSingleTaskMock).setSLObservable(buyOrder, noSL);
                    verify(positionSingleTaskMock).setTPObservable(sellOrder, noTP);
                    verify(positionSingleTaskMock).setSLObservable(sellOrder, noSL);
                }

                @Test
                public void testSubscriberCompleted() {
                    taskSubscriber.assertCompleted();
                }
            }
        }

        public class SingleTaskCallWithJFException {

            @Before
            public void setUp() {
                setTPTaskMockResult(buyOrder, noTP, exceptionObservable());
                setTPTaskMockResult(sellOrder, noTP, exceptionObservable());

                removeTPSLCall.run();
            }

            @Test
            public void testSetTPOnOrderHasBeenCalledWithoutRetry() {
                verify(positionSingleTaskMock).setTPObservable(any(), eq(noTP));
            }

            @Test
            public void testSubscriberGetsJFExceptionNotification() {
                assertJFException(taskSubscriber);
            }
        }
    }
}
