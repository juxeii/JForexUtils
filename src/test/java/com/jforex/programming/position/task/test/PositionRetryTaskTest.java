package com.jforex.programming.position.task.test;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.function.Predicate;
import java.util.function.Supplier;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.position.task.PositionRetryTask;
import com.jforex.programming.test.common.PositionCommonTest;
import com.jforex.programming.test.fakes.IOrderForTest;

import com.dukascopy.api.IOrder;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import rx.Observable;
import rx.observers.TestSubscriber;

@RunWith(HierarchicalContextRunner.class)
public class PositionRetryTaskTest extends PositionCommonTest {

    private PositionRetryTask<IOrder> positionRetryTask;

    @Mock
    private Supplier<Observable<OrderEvent>> observableCallMock;
    private Predicate<IOrder> predicate;
    private final IOrder orderUnderTest = IOrderForTest.buyOrderEURUSD();
    private Runnable taskCall;
    private final TestSubscriber<OrderEvent> taskSubscriber = new TestSubscriber<>();
    private final OrderEvent rejectEvent = new OrderEvent(orderUnderTest, OrderEventType.CHANGE_SL_REJECTED);

    @Before
    public void setUp() {
        initCommonTestFramework();

        positionRetryTask = new PositionRetryTask<IOrder>();
        taskCall = () -> positionRetryTask.create(observableCallMock, predicate, orderUnderTest)
                .subscribe(taskSubscriber);
    }

    public class PredicateFalse {

        @Before
        public void setUp() {
            predicate = order -> false;

            taskCall.run();
        }

        @Test
        public void testObservableCallWasNotExecuted() {
            verify(observableCallMock, never()).get();
        }

        @Test
        public void testSubscriberCompleted() {
            taskSubscriber.assertCompleted();
        }
    }

    public class PredicateTrue {

        @Before
        public void setUp() {
            predicate = order -> true;
        }

        public class TaskCallBusy {

            @Before
            public void setUp() {
                when(observableCallMock.get()).thenReturn(Observable.never());

                taskCall.run();
            }

            @Test
            public void testObservableCallWasExecuted() {
                verify(observableCallMock).get();
            }

            @Test
            public void testSubscriberNotYetCompleted() {
                taskSubscriber.assertNotCompleted();
            }
        }

        public class TaskCallOK {

            @Before
            public void setUp() {
                when(observableCallMock.get()).thenReturn(Observable.empty());

                taskCall.run();
            }

            @Test
            public void testObservableCallWasExecuted() {
                verify(observableCallMock).get();
            }

            @Test
            public void testSubscriberCompleted() {
                taskSubscriber.assertCompleted();
            }
        }

        public class TaskCallWithJFException {

            @Before
            public void setUp() {
                when(observableCallMock.get()).thenReturn(exceptionObservable());

                taskCall.run();
            }

            @Test
            public void testObservableCalledWithoutRetry() {
                verify(observableCallMock).get();
            }

            @Test
            public void testSubscriberGetsJFExceptionNotification() {
                assertJFException(taskSubscriber);
            }
        }

        public class TaskCallWhichExceedsRetries {

            @Before
            public void setUp() {
                setRetryExceededMock(() -> observableCallMock.get(), rejectEvent);

                taskCall.run();

                rxTestUtil.advanceTimeForAllOrderRetries();
            }

            @Test
            public void testTaskCalledWithAllRetries() {
                verify(observableCallMock, times(retryExceedCount)).get();
            }

            @Test
            public void testSubscriberGetsRejectExceptionNotification() {
                assertRejectException(taskSubscriber);
            }
        }

        public class TaskCallWithFullRetriesThenSuccess {

            @Before
            public void setUp() {
                setFullRetryMock(() -> observableCallMock.get(), rejectEvent);

                taskCall.run();

                rxTestUtil.advanceTimeForAllOrderRetries();
            }

            @Test
            public void testTaskCalledWithAllRetries() {
                verify(observableCallMock, times(retryExceedCount)).get();
            }

            @Test
            public void testSubscriberCompleted() {
                taskSubscriber.assertCompleted();
            }
        }
    }
}
