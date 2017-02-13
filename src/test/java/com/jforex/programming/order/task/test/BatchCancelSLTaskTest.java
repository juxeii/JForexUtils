package com.jforex.programming.order.task.test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.google.common.collect.Sets;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.task.BatchCancelSLTask;
import com.jforex.programming.order.task.BatchChangeTask;
import com.jforex.programming.order.task.params.ComposeData;
import com.jforex.programming.order.task.params.ComposeDataImpl;
import com.jforex.programming.order.task.params.TaskParamsBase;
import com.jforex.programming.order.task.params.TaskParamsUtil;
import com.jforex.programming.order.task.params.position.MergePositionParams;
import com.jforex.programming.test.common.InstrumentUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;

@RunWith(HierarchicalContextRunner.class)
public class BatchCancelSLTaskTest extends InstrumentUtilForTest {

    private BatchCancelSLTask batchCancelSLTask;

    @Mock
    private BatchChangeTask batchChangeTaskMock;
    @Mock
    private MergePositionParams mergePositionParamsMock;
    @Mock
    private TaskParamsUtil taskParamsUtilMock;
    @Mock
    private TaskParamsBase composeParamsMock;
    private TestObserver<OrderEvent> testObserver;
    private final Set<IOrder> toCancelSLTPOrders = Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD);
    private final Observable<OrderEvent> observableFromBatch = eventObservable(changedSLEvent);
    private final Observable<OrderEvent> observableFromTaskParamsUtil = eventObservable(changedRejectEvent);
    private final Map<OrderEventType, Consumer<OrderEvent>> consumerForEvent = new HashMap<>();
    private final ComposeData composeData = new ComposeDataImpl();

    @Before
    public void setUp() {
        when(batchChangeTaskMock.cancelSL(toCancelSLTPOrders, mergePositionParamsMock))
            .thenReturn(observableFromBatch);
        when(taskParamsUtilMock.compose(observableFromBatch,
                                        composeData,
                                        consumerForEvent))
                                            .thenReturn(observableFromTaskParamsUtil);
        when(mergePositionParamsMock.batchCancelSLParams())
            .thenReturn(composeParamsMock);
        when(composeParamsMock.composeData())
            .thenReturn(composeData);

        batchCancelSLTask = new BatchCancelSLTask(batchChangeTaskMock, taskParamsUtilMock);
    }

    @Test
    public void observableIsDeferred() {
        batchCancelSLTask.observe(toCancelSLTPOrders, mergePositionParamsMock);

        verifyZeroInteractions(batchChangeTaskMock);
    }

    public class ObserveTaskSetup {

        @Before
        public void setUp() {
            testObserver = batchCancelSLTask
                .observe(toCancelSLTPOrders, mergePositionParamsMock)
                .test();
        }

        @Test
        public void batchChangeTaskIsCalled() {
            verify(batchChangeTaskMock).cancelSL(toCancelSLTPOrders, mergePositionParamsMock);
        }

        @Test
        public void composeOnTaskParamsIsCalled() {
            verify(taskParamsUtilMock).compose(any(),
                                               eq(composeData),
                                               eq(consumerForEvent));
        }

        @Test
        public void eventFromTaskParamsUtilIsEmitted() {
            testObserver.assertComplete();
            testObserver.assertValue(changedRejectEvent);
        }
    }
}
