package com.jforex.programming.order.task.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.task.BasicTaskForBatch;
import com.jforex.programming.order.task.BasicTask;
import com.jforex.programming.order.task.params.SetSLTPMode;
import com.jforex.programming.order.task.params.basic.CloseParams;
import com.jforex.programming.order.task.params.basic.SetSLParams;
import com.jforex.programming.order.task.params.basic.SetTPParams;
import com.jforex.programming.test.common.InstrumentUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;

@RunWith(HierarchicalContextRunner.class)
public class BasicTaskForBatchTest extends InstrumentUtilForTest {

    private BasicTaskForBatch basicTaskForBatch;

    @Mock
    private BasicTask basicTaskMock;
    @Captor
    private ArgumentCaptor<SetSLParams> setSLParamsCaptor;
    @Captor
    private ArgumentCaptor<SetTPParams> setTPParamsCaptor;
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

        private final CloseParams closeParams = CloseParams.withOrder(orderForTest).build();

        @Before
        public void setUp() {
            returnedEvent = closeEvent;
            returnedObservable = eventObservable(closeEvent);
            when(basicTaskMock.close(closeParams)).thenReturn(returnedObservable);

            testObserver = basicTaskForBatch
                .forClose(closeParams)
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
        public void closeOnBasicTaskIsWithCorrectParams() {
            verify(basicTaskMock).setStopLossPrice(setSLParamsCaptor.capture());

            final SetSLParams setSLParams = setSLParamsCaptor.getValue();
            assertThat(setSLParams.priceOrPips(), equalTo(noSL));
            assertThat(setSLParams.setSLTPMode(), equalTo(SetSLTPMode.PRICE));
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
        public void closeOnBasicTaskIsWithCorrectParams() {
            verify(basicTaskMock).setTakeProfitPrice(setTPParamsCaptor.capture());

            final SetTPParams setTPParams = setTPParamsCaptor.getValue();
            assertThat(setTPParams.priceOrPips(), equalTo(noTP));
            assertThat(setTPParams.setSLTPMode(), equalTo(SetSLTPMode.PRICE));
        }

        @Test
        public void returnedObservableIsCorrect() {
            verifyReturnedObservable(returnedEvent);
        }
    }
}
