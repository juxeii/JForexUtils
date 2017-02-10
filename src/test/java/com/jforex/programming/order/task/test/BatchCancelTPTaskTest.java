package com.jforex.programming.order.task.test;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.google.common.collect.Sets;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.task.BatchCancelTPTask;
import com.jforex.programming.order.task.BatchChangeTask;
import com.jforex.programming.order.task.params.ComposeDataImpl;
import com.jforex.programming.order.task.params.TaskParamsUtil;
import com.jforex.programming.order.task.params.position.MergePositionParams;
import com.jforex.programming.test.common.InstrumentUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;

@RunWith(HierarchicalContextRunner.class)
public class BatchCancelTPTaskTest extends InstrumentUtilForTest {

    private BatchCancelTPTask batchCancelTPTask;

    @Mock
    private BatchChangeTask batchChangeTaskMock;
    @Mock
    private MergePositionParams mergePositionParamsMock;
    @Mock
    private TaskParamsUtil taskParamsUtilMock;
    private TestObserver<OrderEvent> testObserver;
    private final Set<IOrder> toCancelTPTPOrders = Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD);
    private final ComposeDataImpl composeParams = new ComposeDataImpl();
    private final Observable<OrderEvent> observableFromBatch = eventObservable(changedTPEvent);
    private final Observable<OrderEvent> observableFromTaskParamsUtil = eventObservable(changedRejectEvent);

    @Before
    public void setUp() {
        when(batchChangeTaskMock.cancelTP(toCancelTPTPOrders, mergePositionParamsMock))
            .thenReturn(observableFromBatch);
        when(taskParamsUtilMock.composeParams(observableFromBatch, composeParams))
            .thenReturn(observableFromTaskParamsUtil);
        when(mergePositionParamsMock.batchCancelTPComposeData())
            .thenReturn(composeParams);

        batchCancelTPTask = new BatchCancelTPTask(batchChangeTaskMock, taskParamsUtilMock);
    }

    @Test
    public void observableIsDeferred() {
        batchCancelTPTask.observe(toCancelTPTPOrders, mergePositionParamsMock);

        verifyZeroInteractions(batchChangeTaskMock);
    }

    public class ObserveTaskSetup {

        @Before
        public void setUp() {
            testObserver = batchCancelTPTask
                .observe(toCancelTPTPOrders, mergePositionParamsMock)
                .test();
        }

        @Test
        public void batchChangeTaskIsCalled() {
            verify(batchChangeTaskMock).cancelTP(toCancelTPTPOrders, mergePositionParamsMock);
        }

        @Test
        public void composeOnTaskParamsIsCalled() {
            verify(taskParamsUtilMock).composeParams(any(), eq(composeParams));
        }

        @Test
        public void eventFromTaskParamsUtilIsEmitted() {
            testObserver.assertComplete();
            testObserver.assertValue(changedRejectEvent);
        }
    }
}
