package com.jforex.programming.order.task.test;

import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.task.BasicTaskForBatch;
import com.jforex.programming.order.task.BatchComposer;
import com.jforex.programming.order.task.params.ComposeData;
import com.jforex.programming.order.task.params.ComposeDataImpl;
import com.jforex.programming.order.task.params.TaskParamsBase;
import com.jforex.programming.order.task.params.TaskParamsUtil;
import com.jforex.programming.order.task.params.basic.CancelSLParams;
import com.jforex.programming.order.task.params.basic.CancelTPParams;
import com.jforex.programming.order.task.params.basic.CloseParams;
import com.jforex.programming.order.task.params.position.ClosePositionParams;
import com.jforex.programming.order.task.params.position.MergePositionParams;
import com.jforex.programming.test.common.InstrumentUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;

@RunWith(HierarchicalContextRunner.class)
public class BatchComposerTest extends InstrumentUtilForTest {

    private BatchComposer batchComposer;

    @Mock
    private TaskParamsUtil taskParamsUtilMock;
    @Mock
    private BasicTaskForBatch basicTaskForBatchMock;
    @Mock
    private ClosePositionParams closePositionParamsMock;
    @Mock
    private MergePositionParams mergePositionParamsMock;
    @Mock
    private CloseParams closeParamsMock;
    @Mock
    private TaskParamsBase composeParamsMock;
    @Mock
    private CancelSLParams cancelSLParamsMock;
    @Mock
    private CancelTPParams cancelTPParamsMock;
    @Mock
    private Function<IOrder, CancelSLParams> cancelSLParamsFactoryMock;
    @Mock
    private Function<IOrder, CancelTPParams> cancelTPParamsFactoryMock;
    @Mock
    private Function<IOrder, CloseParams> closeParamsFactoryMock;
    private final ComposeData composeData = new ComposeDataImpl();
    private final IOrder orderForTest = buyOrderEURUSD;
    private TestObserver<OrderEvent> testObserver;

    @Before
    public void setUp() {
        batchComposer = new BatchComposer(taskParamsUtilMock, basicTaskForBatchMock);
    }

    private void setupTaskParamsUtil(final Observable<OrderEvent> basicObservable,
                                     final OrderEvent orderEvent,
                                     final TaskParamsBase taskparams) {
        when(taskParamsUtilMock.compose(basicObservable, taskparams))
            .thenReturn(eventObservable(orderEvent));
    }

    private void verifyTaskParamsUtilCall(final Observable<OrderEvent> basicObservable,
                                          final TaskParamsBase taskparams) {
        verify(taskParamsUtilMock).compose(basicObservable, taskparams);
    }

    private void verifyReturnedObservable(final OrderEvent orderEvent) {
        testObserver.assertComplete();
        testObserver.assertValue(orderEvent);
    }

    public class CloseCompose {

        private final Observable<OrderEvent> basicObservable = eventObservable(closeEvent);

        @Before
        public void setUp() {
            when(closeParamsMock.composeData())
                .thenReturn(composeData);
            when(basicTaskForBatchMock.forClose(closeParamsMock))
                .thenReturn(basicObservable);
            when(closePositionParamsMock.closeParamsFactory())
                .thenReturn(closeParamsFactoryMock);
            when(closeParamsFactoryMock.apply(orderForTest))
                .thenReturn(closeParamsMock);
            when(closeParamsMock.composeData())
                .thenReturn(composeData);

            setupTaskParamsUtil(basicObservable,
                                closeRejectEvent,
                                closeParamsMock);

            testObserver = batchComposer
                .composeClose(closePositionParamsMock)
                .apply(orderForTest)
                .test();
        }

        @Test
        public void basicTaskForBatchIsCalled() {
            verify(basicTaskForBatchMock).forClose(closeParamsMock);
        }

        @Test
        public void taskParamsUtilIsCalled() {
            verifyTaskParamsUtilCall(basicObservable, closeParamsMock);
        }

        @Test
        public void returnedObservableIsCorrect() {
            verifyReturnedObservable(closeRejectEvent);
        }
    }

    public class CancelSLCompose {

        private final Observable<OrderEvent> basicObservable = eventObservable(changedSLEvent);

        @Before
        public void setUp() {
            when(basicTaskForBatchMock.forCancelSL(orderForTest))
                .thenReturn(basicObservable);
            when(mergePositionParamsMock.cancelSLParamsFactory())
                .thenReturn(cancelSLParamsFactoryMock);

            when(cancelSLParamsFactoryMock.apply(orderForTest))
                .thenReturn(cancelSLParamsMock);
            when(cancelSLParamsMock.composeData())
                .thenReturn(composeData);

            setupTaskParamsUtil(basicObservable,
                                changedRejectEvent,
                                cancelSLParamsMock);

            testObserver = batchComposer
                .composeCancelSL(mergePositionParamsMock)
                .apply(orderForTest)
                .test();
        }

        @Test
        public void basicTaskForBatchIsCalled() {
            verify(basicTaskForBatchMock).forCancelSL(orderForTest);
        }

        @Test
        public void taskParamsUtilIsCalled() {
            verifyTaskParamsUtilCall(basicObservable, cancelSLParamsMock);
        }

        @Test
        public void returnedObservableIsCorrect() {
            verifyReturnedObservable(changedRejectEvent);
        }
    }

    public class CancelTPCompose {

        private final Observable<OrderEvent> basicObservable = eventObservable(changedTPEvent);

        @Before
        public void setUp() {
            when(basicTaskForBatchMock.forCancelTP(orderForTest))
                .thenReturn(basicObservable);
            when(mergePositionParamsMock.cancelTPParamsFactory())
                .thenReturn(cancelTPParamsFactoryMock);

            when(cancelTPParamsFactoryMock.apply(orderForTest))
                .thenReturn(cancelTPParamsMock);
            when(cancelTPParamsMock.composeData())
                .thenReturn(composeData);

            setupTaskParamsUtil(basicObservable,
                                changedRejectEvent,
                                cancelTPParamsMock);

            testObserver = batchComposer
                .composeCancelTP(mergePositionParamsMock)
                .apply(orderForTest)
                .test();
        }

        @Test
        public void basicTaskForBatchIsCalled() {
            verify(basicTaskForBatchMock).forCancelTP(orderForTest);
        }

        @Test
        public void taskParamsUtilIsCalled() {
            verifyTaskParamsUtilCall(basicObservable, cancelTPParamsMock);
        }

        @Test
        public void returnedObservableIsCorrect() {
            verifyReturnedObservable(changedRejectEvent);
        }
    }
}
