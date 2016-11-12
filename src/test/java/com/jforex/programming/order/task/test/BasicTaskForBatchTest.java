package com.jforex.programming.order.task.test;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.task.BasicTaskForBatch;
import com.jforex.programming.order.task.BasicTaskObservable;
import com.jforex.programming.test.common.InstrumentUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;

@RunWith(HierarchicalContextRunner.class)
public class BasicTaskForBatchTest extends InstrumentUtilForTest {

    private BasicTaskForBatch basicTaskForBatch;

    @Mock
    private BasicTaskObservable basicTaskMock;
    private TestObserver<OrderEvent> testObserver;
    private final IOrder orderForTest = buyOrderEURUSD;
    private Observable<OrderEvent> returnedObservable;
    private OrderEvent returnedEvent;

    @Before
    public void setUp() {
        basicTaskForBatch = new BasicTaskForBatch(basicTaskMock);
    }

    private void verifyReturnedObservable(final OrderEvent orderEvent) {
        testObserver.assertComplete();
        testObserver.assertValue(orderEvent);
    }

    public class ForClose {

        @Before
        public void setUp() {
            returnedEvent = closeEvent;
            returnedObservable = eventObservable(closeEvent);
            when(basicTaskMock.close(any())).thenReturn(returnedObservable);

            testObserver = basicTaskForBatch
                .forClose(orderForTest)
                .test();
        }

        @Test
        public void closeOnBasicTaskIsCalled() {
            verify(basicTaskMock).close(any());
        }

        @Test
        public void returnedObservableIsCorrect() {
            verifyReturnedObservable(returnedEvent);
        }
    }

    public class ForCancelSL {

        @Before
        public void setUp() {
            returnedEvent = changedSLEvent;
            returnedObservable = eventObservable(returnedEvent);
            when(basicTaskMock.setStopLossPrice(any())).thenReturn(returnedObservable);

            testObserver = basicTaskForBatch
                .forCancelSL(orderForTest)
                .test();
        }

        @Test
        public void closeOnBasicTaskIsCalled() {
            verify(basicTaskMock).setStopLossPrice(any());
        }

        @Test
        public void returnedObservableIsCorrect() {
            verifyReturnedObservable(returnedEvent);
        }
    }

    public class ForCancelTP {

        @Before
        public void setUp() {
            returnedEvent = changedTPEvent;
            returnedObservable = eventObservable(returnedEvent);
            when(basicTaskMock.setTakeProfitPrice(any())).thenReturn(returnedObservable);

            testObserver = basicTaskForBatch
                .forCancelTP(orderForTest)
                .test();
        }

        @Test
        public void closeOnBasicTaskIsCalled() {
            verify(basicTaskMock).setTakeProfitPrice(any());
        }

        @Test
        public void returnedObservableIsCorrect() {
            verifyReturnedObservable(returnedEvent);
        }
    }
}
