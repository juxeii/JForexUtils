package com.jforex.programming.position.task.test;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.position.task.PositionMultiTask;
import com.jforex.programming.position.task.PositionSingleTask;
import com.jforex.programming.test.common.PositionCommonTest;
import com.jforex.programming.test.fakes.IOrderForTest;

import com.dukascopy.api.IOrder;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import rx.Observable;
import rx.observers.TestSubscriber;

@RunWith(HierarchicalContextRunner.class)
public class PositionMultiTaskTest extends PositionCommonTest {

    private PositionMultiTask positionMultiTask;

    @Mock
    private PositionSingleTask positionSingleTaskMock;
    private final IOrderForTest orderUnderTest = IOrderForTest.buyOrderEURUSD();
    private final Observable<OrderEvent> taskObservable = Observable.empty();
    private final TestSubscriber<OrderEvent> taskSubscriber = new TestSubscriber<>();

    private void setSLMockObservable(final double newSL,
                                     final Observable<OrderEvent> observable) {
        when(positionSingleTaskMock.setSLObservable(orderUnderTest, newSL))
                .thenReturn(observable);
    }

    private void setTPMockObservable(final double newTP,
                                     final Observable<OrderEvent> observable) {
        when(positionSingleTaskMock.setTPObservable(orderUnderTest, newTP))
                .thenReturn(observable);
    }

    private void verifySetSLCall(final double newSL) {
        verify(positionSingleTaskMock).setSLObservable(orderUnderTest, newSL);
    }

    private void verifySetTPCall(final double newTP) {
        verify(positionSingleTaskMock).setTPObservable(orderUnderTest, newTP);
    }

    @Before
    public void setUp() {
        initCommonTestFramework();

        positionMultiTask = new PositionMultiTask(positionSingleTaskMock);
    }

    public class RemoveTPSLSetup {

        private final Runnable removeTPSLCompletableCall =
                () -> positionMultiTask.removeTPSLObservable(orderUnderTest).subscribe(taskSubscriber);

        @Before
        public void setUp() {
            when(positionSingleTaskMock.setSLObservable(orderUnderTest, noSL)).thenReturn(taskObservable);
            when(positionSingleTaskMock.setTPObservable(orderUnderTest, noTP)).thenReturn(taskObservable);

            orderUnderTest.setState(IOrder.State.FILLED);
        }

        public class RemoveTPOK {

            @Before
            public void setUp() {
                setTPMockObservable(noTP, Observable.empty());
            }

            @Test
            public void testSetTPCalledOnSingleTask() {
                removeTPSLCompletableCall.run();

                verifySetTPCall(noTP);
            }

            @Test
            public void testSubscriberNotYetCompleted() {
                setSLMockObservable(noSL, Observable.never());

                removeTPSLCompletableCall.run();

                taskSubscriber.assertNotCompleted();
            }

            public class RemoveSLOK {

                @Before
                public void setUp() {
                    setSLMockObservable(noSL, Observable.empty());

                    removeTPSLCompletableCall.run();
                }

                @Test
                public void testSetSLCalledOnSingleTask() {
                    verifySetSLCall(noSL);
                }

                @Test
                public void testSubscriberCompleted() {
                    taskSubscriber.assertCompleted();
                }
            }
        }

        public class RemoveTPException {

            @Before
            public void setUp() {
                setTPMockObservable(noTP, exceptionObservable());

                removeTPSLCompletableCall.run();
            }

            @Test
            public void testSubscriberGetsJFExceptionNotification() {
                assertJFException(taskSubscriber);
            }

            @Test
            public void testSetSLIsNotCalled() {
                verify(positionSingleTaskMock, never())
                        .setSLObservable(orderUnderTest, noSL);
            }
        }
    }
}
