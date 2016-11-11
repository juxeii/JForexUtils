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
import com.jforex.programming.order.task.params.ComposeParams;
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
    private final TaskParamsUtil taskParamsUtil = spy(new TaskParamsUtil());
    private TestObserver<OrderEvent> testObserver;
    private final Set<IOrder> toCancelTPTPOrders = Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD);
    private final ComposeParams composeParams = new ComposeParams();
    private final Observable<OrderEvent> observable = eventObservable(changedTPEvent);

    @Before
    public void setUp() {
        when(mergePositionParamsMock.batchCancelTPComposeParams())
            .thenReturn(composeParams);

        when(batchChangeTaskMock.cancelTP(toCancelTPTPOrders, mergePositionParamsMock))
            .thenReturn(observable);

        batchCancelTPTask = new BatchCancelTPTask(batchChangeTaskMock, taskParamsUtil);
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
            verify(taskParamsUtil).composeParams(any(), eq(composeParams));
        }

        @Test
        public void eventIsEmitted() {
            testObserver.assertComplete();
            testObserver.assertValue(changedTPEvent);
        }
    }
}