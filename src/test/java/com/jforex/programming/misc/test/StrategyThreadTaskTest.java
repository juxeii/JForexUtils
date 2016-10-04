package com.jforex.programming.misc.test;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.jforex.programming.misc.StrategyThreadTask;
import com.jforex.programming.test.common.CommonUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Observable;
import io.reactivex.functions.Action;
import io.reactivex.observers.TestObserver;

@RunWith(HierarchicalContextRunner.class)
public class StrategyThreadTaskTest extends CommonUtilForTest {

    private StrategyThreadTask strategyThreadTask;

    @Mock
    private Action actionMock;
    @Mock
    private Callable<IOrder> callableMock;
    @Mock
    private Future<IOrder> futureMock;
    @Mock
    private Future<Object> futureVoidMock;
    private TestObserver<Void> orderActionSubscriber;
    private TestObserver<IOrder> orderCallableSubscriber;
    private final Runnable executeActionCall = () -> orderActionSubscriber = strategyThreadTask
        .execute(actionMock)
        .test();
    private final Runnable executeCallableCall = () -> orderCallableSubscriber = strategyThreadTask
        .execute(callableMock)
        .test();

    @Before
    public void setUp() throws Exception {
        setUpMocks();

        strategyThreadTask = new StrategyThreadTask(contextMock);
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
        strategyThreadTask.execute(callableMock);
        strategyThreadTask.execute(actionMock);

        verifyNoExecutions();
    }

    @Test
    public void executeOnContextForActionIsCorrect() throws Exception {
        when(contextMock.executeTask(any())).thenReturn(futureVoidMock);

        strategyThreadTask
            .execute(actionMock)
            .subscribe();

        verify(contextMock).executeTask(argThat(callable -> {
            Observable
                .fromCallable(callable)
                .test();
            return true;
        }));
        verify(actionMock, times(2)).run();
    }

    @Test
    public void executeOnContextForCallableIsCorrect() throws Exception {
        strategyThreadTask
            .execute(callableMock)
            .subscribe();

        verify(contextMock).executeTask(callableMock);
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
                    executeActionCall.run();
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
        }

        public class CallableTests {

            public class WhenOnStrategyThreadSubscribe {

                @Before
                public void setUp() {
                    executeCallableCall.run();
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

                    executeActionCall.run();

                    orderActionSubscriber.assertError(RuntimeException.class);
                }

                public class WhenOnStrategyThreadSubscribe {

                    @Before
                    public void setUp() {
                        when(contextMock.executeTask(any())).thenReturn(futureVoidMock);

                        executeActionCall.run();
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
        }

        public class CallableTests {

            public class WhenOnStrategyThreadCall {

                @Test
                public void onErrorExceptionIsEmitted() throws Exception {
                    when(contextMock.executeTask(callableMock)).thenThrow(new RuntimeException());

                    executeCallableCall.run();

                    orderCallableSubscriber.assertError(RuntimeException.class);
                }

                public class WhenOnStrategyThreadSubscribe {

                    @Before
                    public void setUp() {
                        executeCallableCall.run();
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
        }
    }
}
