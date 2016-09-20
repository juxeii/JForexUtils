package com.jforex.programming.misc.test;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.JFException;
import com.jforex.programming.misc.TaskExecutor;
import com.jforex.programming.test.common.CommonUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.functions.Action;
import io.reactivex.subscribers.TestSubscriber;

@RunWith(HierarchicalContextRunner.class)
public class TaskExecutorTest extends CommonUtilForTest {

    private TaskExecutor taskExecutor;

    @Mock
    private Action actionMock;
    @Mock
    private Callable<IOrder> callableMock;
    @Mock
    private Future<IOrder> futureMock;
    @Mock
    private Future<Object> futureVoidMock;
    private TestSubscriber<Void> orderActionSubscriber = TestSubscriber.create();
    private TestSubscriber<IOrder> orderCallableSubscriber = TestSubscriber.create();
    private final Runnable onStrategyThreadForActionCall = () -> orderActionSubscriber = taskExecutor
        .onStrategyThread(actionMock)
        .test();
    private final Runnable onCurrentThreadForActionCall = () -> orderActionSubscriber = taskExecutor
        .onCurrentThread(actionMock)
        .test();
    private final Runnable onStrategyThreadForCallableCall = () -> orderCallableSubscriber = taskExecutor
        .onStrategyThread(callableMock)
        .test();
    private final Runnable onCurrentThreadForCallableCall = () -> orderCallableSubscriber = taskExecutor
        .onCurrentThread(callableMock)
        .test();

    @Before
    public void setUp() throws Exception {
        setUpMocks();

        taskExecutor = new TaskExecutor(contextMock);
    }

    private void setUpMocks() throws Exception {
        when(callableMock.call()).thenReturn(buyOrderEURUSD);

        when(futureMock.get()).thenReturn(buyOrderEURUSD);

        when(contextMock.executeTask(callableMock)).thenReturn(futureMock);
    }

    private void verifyNoExecutions() {
        verifyZeroInteractions(actionMock);
        verifyZeroInteractions(callableMock);
        verifyZeroInteractions(contextMock);
        verifyZeroInteractions(futureMock);
    }

    private void assertOrderEmissionAndCompletion() {
        orderCallableSubscriber.assertComplete();
        orderCallableSubscriber.assertNoErrors();
        orderCallableSubscriber.assertValue(buyOrderEURUSD);
    }

    @Test
    public void whenNotSubscribedNoExecutionHappens() {
        taskExecutor.onStrategyThread(callableMock);
        taskExecutor.onCurrentThread(callableMock);
        taskExecutor.onStrategyThread(actionMock);
        taskExecutor.onCurrentThread(actionMock);

        verifyNoExecutions();
    }

    public class WhenStrategyThread {

        @Before
        public void setUp() {
            CommonUtilForTest.setStrategyThread();
        }

        public class ActionTests {

            public class WhenOnStrategyThreadSubscribe {

                @Before
                public void setUp() {
                    onStrategyThreadForActionCall.run();
                }

                @Test
                public void noExecutionWithContextHappensSinceAlreadyOnStrategyThread() throws InterruptedException,
                                                                                        ExecutionException {
                    verify(contextMock, never()).executeTask(any());
                    verify(futureMock, never()).get();
                }

                @Test
                public void subscriberCompletes() {
                    orderActionSubscriber.assertComplete();
                }
            }

            public class WhenOnCurrentThreadSubscribe {

                @Before
                public void setUp() {
                    onCurrentThreadForActionCall.run();
                }

                @Test
                public void noExecutionWithContextHappens() throws InterruptedException,
                                                            ExecutionException {
                    verify(contextMock, never()).executeTask(any());
                    verify(futureMock, never()).get();
                }

                @Test
                public void subscriberCompletes() {
                    orderActionSubscriber.assertComplete();
                }
            }
        }

        public class CallableTests {

            public class WhenOnStrategyThreadSubscribe {

                @Before
                public void setUp() {
                    onStrategyThreadForCallableCall.run();
                }

                @Test
                public void noExecutionWithContextHappensSinceAlreadyOnStrategyThread() throws InterruptedException,
                                                                                        ExecutionException {
                    verify(contextMock, never()).executeTask(callableMock);
                    verify(futureMock, never()).get();
                }

                @Test
                public void correctOrderIsEmitted() {
                    assertOrderEmissionAndCompletion();
                }
            }

            public class WhenOnCurrentThreadSubscribe {

                @Before
                public void setUp() {
                    onCurrentThreadForCallableCall.run();
                }

                @Test
                public void noExecutionWithContextHappens() throws InterruptedException,
                                                            ExecutionException {
                    verify(contextMock, never()).executeTask(callableMock);
                    verify(futureMock, never()).get();
                }

                @Test
                public void correctOrderIsEmitted() {
                    assertOrderEmissionAndCompletion();
                }
            }
        }
    }

    public class WhenNonStrategyThread {

        @Before
        public void setUp() {
            CommonUtilForTest.setNotStrategyThread();
        }

        public class ActionTests {

            public class WhenOnStrategyThreadCall {

                @Test
                public void onErrorExceptionIsEmitted() throws Exception {
                    when(contextMock.executeTask(any())).thenThrow(new RuntimeException());

                    onStrategyThreadForActionCall.run();

                    orderActionSubscriber.assertError(RuntimeException.class);
                }

                public class WhenOnStrategyThreadSubscribe {

                    @Before
                    public void setUp() {
                        when(contextMock.executeTask(any())).thenReturn(futureVoidMock);

                        onStrategyThreadForActionCall.run();
                    }

                    @Test
                    public void executionWithContextHappens() throws InterruptedException,
                                                              ExecutionException {
                        verify(contextMock).executeTask(any());
                        verify(futureVoidMock).get();
                    }

                    @Test
                    public void subscriberCompletes() {
                        orderActionSubscriber.assertComplete();
                    }
                }
            }

            public class WhenCurrentThreadThreadCall {

                @Test
                public void onErrorExceptionIsEmitted() throws Exception {
                    doThrow(jfException).when(actionMock).run();

                    onCurrentThreadForActionCall.run();

                    orderActionSubscriber.assertError(JFException.class);
                }

                public class WhenOnCurrentThreadSubscribe {

                    @Before
                    public void setUp() {
                        onCurrentThreadForActionCall.run();
                    }

                    @Test
                    public void noExecutionWithContextHappens() throws InterruptedException,
                                                                ExecutionException {
                        verify(contextMock, never()).executeTask(any());
                        verify(futureMock, never()).get();
                    }

                    @Test
                    public void subscriberCompletes() {
                        orderActionSubscriber.assertComplete();
                    }
                }
            }
        }

        public class CallableTests {

            public class WhenOnStrategyThreadCall {

                @Test
                public void onErrorExceptionIsEmitted() throws Exception {
                    when(contextMock.executeTask(callableMock)).thenThrow(new RuntimeException());

                    onStrategyThreadForCallableCall.run();

                    orderCallableSubscriber.assertError(RuntimeException.class);
                }

                public class WhenOnStrategyThreadSubscribe {

                    @Before
                    public void setUp() {
                        onStrategyThreadForCallableCall.run();
                    }

                    @Test
                    public void executionWithContextHappens() throws InterruptedException,
                                                              ExecutionException {
                        verify(contextMock).executeTask(callableMock);
                        verify(futureMock).get();
                    }

                    @Test
                    public void correctOrderIsEmitted() {
                        assertOrderEmissionAndCompletion();
                    }
                }
            }

            public class WhenCurrentThreadThreadCall {

                @Test
                public void onErrorExceptionIsEmitted() throws Exception {
                    when(callableMock.call()).thenThrow(jfException);

                    onCurrentThreadForCallableCall.run();

                    orderCallableSubscriber.assertError(JFException.class);
                }

                public class WhenOnCurrentThreadSubscribe {

                    @Before
                    public void setUp() {
                        onCurrentThreadForCallableCall.run();
                    }

                    @Test
                    public void noExecutionWithContextHappens() throws InterruptedException,
                                                                ExecutionException {
                        verify(contextMock, never()).executeTask(callableMock);
                        verify(futureMock, never()).get();
                    }

                    @Test
                    public void correctOrderIsEmitted() {
                        assertOrderEmissionAndCompletion();
                    }
                }
            }
        }
    }
}
