package com.jforex.programming.order.task.test;

import java.util.List;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.google.common.collect.Lists;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.task.BatchChangeTask;
import com.jforex.programming.order.task.BatchComposer;
import com.jforex.programming.order.task.BatchCreator;
import com.jforex.programming.order.task.BatchMode;
import com.jforex.programming.order.task.params.position.ClosePositionParams;
import com.jforex.programming.order.task.params.position.MergePositionParams;
import com.jforex.programming.test.common.InstrumentUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;

@RunWith(HierarchicalContextRunner.class)
public class BatchChangeTaskTest extends InstrumentUtilForTest {

    private BatchChangeTask batchChangeTask;

    @Mock
    private BatchComposer batchComposerMock;
    @Mock
    private BatchCreator batchCreatorMock;
    @Mock
    private ClosePositionParams closePositionParamsMock;
    @Mock
    private MergePositionParams mergePositionParamsMock;
    @Mock
    private Function<IOrder, Observable<OrderEvent>> composerFunctionMock;
    private TestObserver<OrderEvent> testObserver;
    private final List<IOrder> ordersForBatch = Lists.newArrayList(buyOrderEURUSD, sellOrderEURUSD);

    @Before
    public void setUp() {
        batchChangeTask = new BatchChangeTask(batchComposerMock, batchCreatorMock);
    }

    private void setupBatchCreator(final BatchMode batchMode,
                                   final OrderEvent orderEvent) {
        when(batchCreatorMock.create(ordersForBatch,
                                     batchMode,
                                     composerFunctionMock))
                                         .thenReturn(eventObservable(orderEvent));
    }

    private void verifyReturnedObservable(final OrderEvent orderEvent) {
        testObserver.assertComplete();
        testObserver.assertValue(orderEvent);
    }

    private void verifyCreatorCall(final BatchMode batchMode) {
        verify(batchCreatorMock).create(ordersForBatch,
                                        batchMode,
                                        composerFunctionMock);
    }

    public class CloseBatch {

        private final BatchMode batchMode = BatchMode.MERGE;

        @Before
        public void setUp() {
            when(closePositionParamsMock.closeBatchMode())
                .thenReturn(batchMode);
            when(batchComposerMock.composeClose(closePositionParamsMock))
                .thenReturn(composerFunctionMock);
            setupBatchCreator(batchMode, closeEvent);

            testObserver = batchChangeTask
                .close(ordersForBatch, closePositionParamsMock)
                .test();
        }

        @Test
        public void composerIsCalled() {
            verify(batchComposerMock).composeClose(closePositionParamsMock);
        }

        @Test
        public void creatorIsCalled() {
            verifyCreatorCall(batchMode);
        }

        @Test
        public void returnedObservableIsCorrect() {
            verifyReturnedObservable(closeEvent);
        }
    }

    public class CancelSLTPBatch {

        public class CancelSLBatch {

            private final BatchMode batchMode = BatchMode.CONCAT;

            @Before
            public void setUp() {
                when(mergePositionParamsMock.batchCancelSLMode())
                    .thenReturn(batchMode);
                when(batchComposerMock.composeCancelSL(mergePositionParamsMock))
                    .thenReturn(composerFunctionMock);
                setupBatchCreator(batchMode, changedSLEvent);

                testObserver = batchChangeTask
                    .cancelSL(ordersForBatch, mergePositionParamsMock)
                    .test();
            }

            @Test
            public void composerIsCalled() {
                verify(batchComposerMock).composeCancelSL(mergePositionParamsMock);
            }

            @Test
            public void creatorIsCalled() {
                verifyCreatorCall(batchMode);
            }

            @Test
            public void returnedObservableIsCorrect() {
                verifyReturnedObservable(changedSLEvent);
            }
        }

        public class CancelTPBatch {

            private final BatchMode batchMode = BatchMode.MERGE;

            @Before
            public void setUp() {
                when(mergePositionParamsMock.batchCancelTPMode())
                    .thenReturn(batchMode);
                when(batchComposerMock.composeCancelTP(mergePositionParamsMock))
                    .thenReturn(composerFunctionMock);
                setupBatchCreator(batchMode, changedTPEvent);

                testObserver = batchChangeTask
                    .cancelTP(ordersForBatch, mergePositionParamsMock)
                    .test();
            }

            @Test
            public void composerIsCalled() {
                verify(batchComposerMock).composeCancelTP(mergePositionParamsMock);
            }

            @Test
            public void creatorIsCalled() {
                verifyCreatorCall(batchMode);
            }

            @Test
            public void returnedObservableIsCorrect() {
                verifyReturnedObservable(changedTPEvent);
            }
        }
    }
}
