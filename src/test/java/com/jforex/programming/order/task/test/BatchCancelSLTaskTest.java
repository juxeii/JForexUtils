package com.jforex.programming.order.task.test;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.google.common.collect.Sets;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.task.BatchCancelSLTask;
import com.jforex.programming.order.task.BatchChangeTask;
import com.jforex.programming.order.task.params.ComposeParams;
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
    private final TaskParamsUtil taskParamsUtil = spy(new TaskParamsUtil());
    private TestObserver<OrderEvent> testObserver;
    private final Set<IOrder> toCancelSLTPOrders = Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD);
    private final ComposeParams composeParams = new ComposeParams();
    private final Observable<OrderEvent> observable = eventObservable(changedSLEvent);

    @Before
    public void setUp() {
        when(mergePositionParamsMock.batchCancelSLComposeParams())
            .thenReturn(composeParams);

        when(batchChangeTaskMock.cancelSL(toCancelSLTPOrders, mergePositionParamsMock))
            .thenReturn(observable);

        batchCancelSLTask = new BatchCancelSLTask(batchChangeTaskMock, taskParamsUtil);
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
            verify(taskParamsUtil).composeParams(any(), eq(composeParams));
        }

        @Test
        public void eventIsEmitted() {
            testObserver.assertComplete();
            testObserver.assertValue(changedSLEvent);
        }
    }
}