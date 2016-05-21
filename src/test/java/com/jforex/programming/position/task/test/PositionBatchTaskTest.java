package com.jforex.programming.position.task.test;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.google.common.collect.Sets;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.position.task.PositionBatchTask;
import com.jforex.programming.position.task.PositionMultiTask;
import com.jforex.programming.position.task.PositionSingleTask;
import com.jforex.programming.test.common.PositionCommonTest;
import com.jforex.programming.test.fakes.IOrderForTest;

import com.dukascopy.api.IOrder;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import rx.Observable;
import rx.observers.TestSubscriber;

@RunWith(HierarchicalContextRunner.class)
public class PositionBatchTaskTest extends PositionCommonTest {

    private PositionBatchTask positionBatchTask;

    @Mock
    private PositionSingleTask positionSingleTaskMock;
    @Mock
    private PositionMultiTask positionMultiTaskMock;
    private final IOrderForTest buyOrder = IOrderForTest.buyOrderEURUSD();
    private final IOrderForTest sellOrder = IOrderForTest.sellOrderEURUSD();
    private final TestSubscriber<OrderEvent> taskSubscriber = new TestSubscriber<>();
    private final Set<IOrder> filledOrders = Sets.newHashSet(buyOrder, sellOrder);
    private final Observable<OrderEvent> testObservable = Observable.empty();

    @Before
    public void setUp() {
        initCommonTestFramework();

        positionBatchTask = new PositionBatchTask(positionSingleTaskMock, positionMultiTaskMock);
    }

    public class CloseCompletableSetup {

        private final Runnable closeCompletableCall =
                () -> positionBatchTask.closeCompletable(filledOrders).subscribe(taskSubscriber);

        @Before
        public void setUp() {
            buyOrder.setState(IOrder.State.FILLED);
            sellOrder.setState(IOrder.State.FILLED);
        }

        @Test
        public void testSubscriberNotYetCompleted() {
            when(positionSingleTaskMock.closeObservable(buyOrder)).thenReturn(Observable.never());

            closeCompletableCall.run();

            taskSubscriber.assertNotCompleted();
        }

        public class CloseBuyOK {

            @Before
            public void setUp() {
                when(positionSingleTaskMock.closeObservable(buyOrder))
                        .thenReturn(testObservable);
            }

            public class CloseSellOK {

                @Before
                public void setUp() {
                    when(positionSingleTaskMock.closeObservable(sellOrder))
                            .thenReturn(testObservable);

                    closeCompletableCall.run();
                }

                @Test
                public void testCloseOnSingleTaskForAllOrdersHasBeenCalled() {
                    verify(positionSingleTaskMock).closeObservable(buyOrder);
                    verify(positionSingleTaskMock).closeObservable(sellOrder);
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
                when(positionSingleTaskMock.closeObservable(buyOrder))
                        .thenReturn(exceptionObservable());
                when(positionSingleTaskMock.closeObservable(sellOrder))
                        .thenReturn(exceptionObservable());

                closeCompletableCall.run();
            }

            @Test
            public void testCloseOnOrderHasBeenCalledWithoutRetry() {
                verify(positionSingleTaskMock).closeObservable(any());
            }

            @Test
            public void testSubscriberGetsJFExceptionNotification() {
                assertJFException(taskSubscriber);
            }
        }
    }

    public class BatchRemoveTPSLCompletableSetup {

        private final Runnable removeTPSLCompletableCall =
                () -> positionBatchTask.removeTPSLObservable(filledOrders).subscribe(taskSubscriber);

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
