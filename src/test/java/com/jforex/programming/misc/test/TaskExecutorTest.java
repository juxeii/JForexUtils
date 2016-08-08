package com.jforex.programming.misc.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.jforex.programming.misc.TaskExecutor;
import com.jforex.programming.test.common.CommonUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import rx.Observable;
import rx.observers.TestSubscriber;

@RunWith(HierarchicalContextRunner.class)
public class TaskExecutorTest extends CommonUtilForTest {

    private TaskExecutor taskExecutor;

    @Mock
    private Callable<IOrder> orderCallMock;
    @Mock
    private Future<IOrder> futureMock;
    private final TestSubscriber<IOrder> orderSubscriber = new TestSubscriber<>();
    private final Runnable executorCall =
            () -> taskExecutor
                .onStrategyThreadIfNeeded(orderCallMock)
                .subscribe(orderSubscriber);

    @Before
    public void setUp() throws Exception {
        setUpMocks();

        taskExecutor = spy(new TaskExecutor(contextMock));
    }

    private void setUpMocks() throws Exception {
        when(orderCallMock.call()).thenReturn(buyOrderEURUSD);

        when(futureMock.get()).thenReturn(buyOrderEURUSD);

        when(contextMock.executeTask(orderCallMock)).thenReturn(futureMock);
    }

    private void verifyNoExecutions() {
        verifyZeroInteractions(orderCallMock);
        verifyZeroInteractions(contextMock);
        verifyZeroInteractions(futureMock);
    }

    private void assertOrderEmissionAndCompletion() {
        orderSubscriber.assertCompleted();
        orderSubscriber.assertNoErrors();
        orderSubscriber.assertValueCount(1);

        assertThat(orderSubscriber.getOnNextEvents().get(0), equalTo(buyOrderEURUSD));
    }

    public class OnStrategyIfNeeded {

        private final Observable<IOrder> testObservable = Observable.just(buyOrderEURUSD);
        private Observable<IOrder> returnedObservable;

        private void assertCorrectCalls() {
            assertThat(returnedObservable, equalTo(testObservable));
            returnedObservable.subscribe(orderSubscriber);
            assertOrderEmissionAndCompletion();
        }

        @Test
        public void onStrategyThreadCorrectMethodIsCalled() {
            CommonUtilForTest.setStrategyThread();

            when(taskExecutor.onCurrentThread(orderCallMock))
                .thenReturn(testObservable);

            returnedObservable = taskExecutor.onStrategyThreadIfNeeded(orderCallMock);

            assertCorrectCalls();
            verify(taskExecutor).onCurrentThread(orderCallMock);
        }

        @Test
        public void onNonStrategyThreadCorrectMethodIsCalled() {
            CommonUtilForTest.setNotStrategyThread();

            when(taskExecutor.onStrategyThread(orderCallMock))
                .thenReturn(testObservable);

            returnedObservable = taskExecutor.onStrategyThreadIfNeeded(orderCallMock);

            assertCorrectCalls();
            verify(taskExecutor).onStrategyThread(orderCallMock);
        }
    }

    public class OnStrategyThread {

        private final Runnable onStrategyThreadCall = () -> taskExecutor
            .onStrategyThread(orderCallMock)
            .subscribe(orderSubscriber);

        @Before
        public void setUp() {
            CommonUtilForTest.setStrategyThread();
        }

        @Test
        public void whenNotSubscribedNoExecutionHappens() {
            taskExecutor.onStrategyThread(orderCallMock);

            verifyNoExecutions();
        }

        @Test
        public void onErrorExceptionIsEmitted() throws Exception {
            when(contextMock.executeTask(orderCallMock)).thenThrow(new RuntimeException());

            onStrategyThreadCall.run();

            orderSubscriber.assertError(RuntimeException.class);
        }

        public class OnSubscribe {

            @Before
            public void setUp() {
                onStrategyThreadCall.run();
            }

            @Test
            public void executionWithContextHappens() throws InterruptedException,
                                                      ExecutionException {
                verify(contextMock).executeTask(orderCallMock);
                verify(futureMock).get();
            }

            @Test
            public void testOrderIsEmittedAndCompleted() {
                assertOrderEmissionAndCompletion();
            }
        }
    }

    public class OnCurrentThread {

        private final Runnable onCurrentThreadCall = () -> taskExecutor
            .onCurrentThread(orderCallMock)
            .subscribe(orderSubscriber);

        @Test
        public void whenNotSubscribedNoExecutionHappens() {
            taskExecutor.onCurrentThread(orderCallMock);

            verifyNoExecutions();
        }

        @Test
        public void onErrorExceptionIsEmitted() throws InterruptedException,
                                                ExecutionException {
            when(futureMock.get()).thenThrow(new InterruptedException(""));

            executorCall.run();

            orderSubscriber.assertError(InterruptedException.class);
        }

        @Test
        public void onSubscribeCorrectOrderIsEmitted() {
            onCurrentThreadCall.run();

            assertOrderEmissionAndCompletion();
        }
    }
}
