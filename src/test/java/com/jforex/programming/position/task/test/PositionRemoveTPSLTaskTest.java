package com.jforex.programming.position.task.test;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.google.common.collect.Sets;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.position.task.PositionMultiTask;
import com.jforex.programming.position.task.PositionRemoveTPSLTask;
import com.jforex.programming.test.common.PositionCommonTest;
import com.jforex.programming.test.fakes.IOrderForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import rx.Observable;
import rx.observers.TestSubscriber;

@RunWith(HierarchicalContextRunner.class)
public class PositionRemoveTPSLTaskTest extends PositionCommonTest {

    private PositionRemoveTPSLTask removeTPSLTask;

    @Mock
    private PositionMultiTask positionMultiTaskMock;

    @Before
    public void setUp() {
        initCommonTestFramework();

        removeTPSLTask = new PositionRemoveTPSLTask(positionMultiTaskMock);
    }

    public class RemoveTPSLCompletableSetup {

        private final IOrderForTest buyOrder = IOrderForTest.buyOrderEURUSD();
        private final IOrderForTest sellOrder = IOrderForTest.sellOrderEURUSD();
        private final TestSubscriber<OrderEvent> taskSubscriber = new TestSubscriber<>();
        private final Set<IOrder> filledOrders = Sets.newHashSet(buyOrder, sellOrder);

        private final Runnable removeTPSLCompletableCall =
                () -> removeTPSLTask.removeTPSLObservable(filledOrders).subscribe(taskSubscriber);

        @Before
        public void setUp() {
            buyOrder.setState(IOrder.State.FILLED);
            sellOrder.setState(IOrder.State.FILLED);
        }

        @Test
        public void testSubscriberNotYetCompleted() {
            when(positionMultiTaskMock.removeTPSLObservable(buyOrder))
                    .thenReturn(Observable.never());
            when(positionMultiTaskMock.removeTPSLObservable(sellOrder))
                    .thenReturn(Observable.never());

            removeTPSLCompletableCall.run();

            taskSubscriber.assertNotCompleted();
        }

        public class RemoveTPSLOnBuyOK {

            @Before
            public void setUp() {
                when(positionMultiTaskMock.removeTPSLObservable(buyOrder))
                        .thenReturn(Observable.empty());
            }

            public class RemoveTPSLOnSellOK {

                @Before
                public void setUp() {
                    when(positionMultiTaskMock.removeTPSLObservable(sellOrder))
                            .thenReturn(Observable.empty());

                    removeTPSLCompletableCall.run();
                }

                @Test
                public void testRemoveTPSLOnMultiTaskForAllOrdersHasBeenCalled() {
                    verify(positionMultiTaskMock).removeTPSLObservable(buyOrder);
                    verify(positionMultiTaskMock).removeTPSLObservable(sellOrder);
                }

                @Test
                public void testSubscriberCompleted() {
                    taskSubscriber.assertCompleted();
                }
            }
        }

        public class MultiTaskCallWithJFException {

            @Before
            public void setUp() {
                when(positionMultiTaskMock.removeTPSLObservable(buyOrder))
                        .thenReturn(exceptionObservable());
                when(positionMultiTaskMock.removeTPSLObservable(sellOrder))
                        .thenReturn(exceptionObservable());

                removeTPSLCompletableCall.run();
            }

            @Test
            public void testCloseOnOrderHasBeenCalledWithoutRetry() {
                verify(positionMultiTaskMock).removeTPSLObservable(any());
            }

            @Test
            public void testSubscriberGetsJFExceptionNotification() {
                assertJFException(taskSubscriber);
            }
        }
    }
}
