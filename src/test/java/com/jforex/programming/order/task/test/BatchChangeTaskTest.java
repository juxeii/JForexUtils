package com.jforex.programming.order.task.test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.google.common.collect.Lists;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.task.BasicTaskObservable;
import com.jforex.programming.order.task.BatchChangeTask;
import com.jforex.programming.order.task.BatchMode;
import com.jforex.programming.order.task.params.ComposeParams;
import com.jforex.programming.order.task.params.TaskParamsUtil;
import com.jforex.programming.order.task.params.position.ClosePositionParams;
import com.jforex.programming.order.task.params.position.MergePositionParams;
import com.jforex.programming.test.common.InstrumentUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Observable;

@RunWith(HierarchicalContextRunner.class)
public class BatchChangeTaskTest extends InstrumentUtilForTest {

    private BatchChangeTask batchChangeTask;

    @Mock
    private BasicTaskObservable basicTaskMock;
    @Mock
    private TaskParamsUtil taskParamsUtilMock;
    @Mock
    private ClosePositionParams closePositionParamsMock;
    @Mock
    private MergePositionParams mergePositionParamsMock;
    private final List<IOrder> ordersForBatch =
            Lists.newArrayList(buyOrderEURUSD, sellOrderEURUSD);
    private final Map<OrderEventType, Consumer<OrderEvent>> consumersForParams =
            new HashMap<>();
    private final ComposeParams composeParams = new ComposeParams();
    private Observable<OrderEvent> composedObservable;

    @Before
    public void setUp() throws Exception {
        when(closePositionParamsMock.consumerForEvent()).thenReturn(consumersForParams);
        when(mergePositionParamsMock.consumerForEvent()).thenReturn(consumersForParams);

        when(closePositionParamsMock.closeComposeParams(any())).thenReturn(composeParams);
        when(mergePositionParamsMock.batchCancelTPMode()).thenReturn(BatchMode.MERGE);

        batchChangeTask = new BatchChangeTask(basicTaskMock, taskParamsUtilMock);
    }

    public class CloseBatch {

        @Before
        public void setUp() {
            composedObservable = eventObservable(closeEvent);

            when(basicTaskMock.close(any()))
                .thenReturn(neverObservable())
                .thenReturn(eventObservable(closeEvent));
        }

        @Test
        public void isMerged() {
            when(taskParamsUtilMock.composeParamsWithEvents(any(),
                                                            any(),
                                                            eq(composeParams),
                                                            eq(consumersForParams)))
                                                                .thenReturn(neverObservable())
                                                                .thenReturn(composedObservable);

            batchChangeTask
                .close(ordersForBatch, closePositionParamsMock)
                .test()
                .assertNotComplete()
                .assertValue(closeEvent);
        }
    }

    public class CancelSLBatch {

        @Before
        public void setUp() {
            composedObservable = eventObservable(changedSLEvent);

            when(basicTaskMock.setStopLossPrice(any()))
                .thenReturn(neverObservable());
            when(basicTaskMock.setStopLossPrice(any()))
                .thenReturn(eventObservable(changedSLEvent));

            when(mergePositionParamsMock.cancelSLComposeParams(any()))
                .thenReturn(composeParams);
        }

        @Test
        public void forMergeIsNotConcatenated() {
            when(mergePositionParamsMock.batchCancelSLMode()).thenReturn(BatchMode.MERGE);
            when(taskParamsUtilMock.composeParamsWithEvents(any(),
                                                            any(),
                                                            eq(composeParams),
                                                            eq(consumersForParams)))
                                                                .thenReturn(neverObservable())
                                                                .thenReturn(composedObservable);

            batchChangeTask
                .cancelSL(ordersForBatch, mergePositionParamsMock)
                .test()
                .assertNotComplete()
                .assertValue(changedSLEvent);
        }

        @Test
        public void forConcatIsNotMerged() {
            when(mergePositionParamsMock.batchCancelSLMode()).thenReturn(BatchMode.CONCAT);
            when(taskParamsUtilMock.composeParamsWithEvents(any(),
                                                            any(),
                                                            eq(composeParams),
                                                            eq(consumersForParams)))
                                                                .thenReturn(neverObservable())
                                                                .thenReturn(composedObservable);

            batchChangeTask
                .cancelSL(ordersForBatch, mergePositionParamsMock)
                .test()
                .assertNotComplete()
                .assertNoValues();
        }
    }

    public class CancelTPBatch {

        @Before
        public void setUp() {
            composedObservable = eventObservable(changedTPEvent);

            when(basicTaskMock.setTakeProfitPrice(any()))
                .thenReturn(neverObservable());
            when(basicTaskMock.setTakeProfitPrice(any()))
                .thenReturn(eventObservable(changedTPEvent));

            when(mergePositionParamsMock.cancelTPComposeParams(any()))
                .thenReturn(composeParams);
        }

        @Test
        public void forMergeIsNotConcatenated() {
            when(mergePositionParamsMock.batchCancelTPMode()).thenReturn(BatchMode.MERGE);
            when(taskParamsUtilMock.composeParamsWithEvents(any(),
                                                            any(),
                                                            eq(composeParams),
                                                            eq(consumersForParams)))
                                                                .thenReturn(neverObservable())
                                                                .thenReturn(composedObservable);

            batchChangeTask
                .cancelTP(ordersForBatch, mergePositionParamsMock)
                .test()
                .assertNotComplete()
                .assertValue(changedTPEvent);
        }

        @Test
        public void forConcatIsNotMerged() {
            when(mergePositionParamsMock.batchCancelTPMode()).thenReturn(BatchMode.CONCAT);
            when(taskParamsUtilMock.composeParamsWithEvents(any(),
                                                            any(),
                                                            eq(composeParams),
                                                            eq(consumersForParams)))
                                                                .thenReturn(neverObservable())
                                                                .thenReturn(composedObservable);

            batchChangeTask
                .cancelTP(ordersForBatch, mergePositionParamsMock)
                .test()
                .assertNotComplete()
                .assertNoValues();
        }
    }
}
