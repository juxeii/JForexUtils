package com.jforex.programming.order.task.params.position.test;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.google.common.collect.Sets;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.task.BasicTaskObservable;
import com.jforex.programming.order.task.CancelSLTPTask;
import com.jforex.programming.order.task.params.ComposeParams;
import com.jforex.programming.order.task.params.TaskParamsUtil;
import com.jforex.programming.order.task.params.position.MergePositionParams;
import com.jforex.programming.order.task.params.position.MergePositionParamsHandler;
import com.jforex.programming.test.common.InstrumentUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;

@RunWith(HierarchicalContextRunner.class)
public class MergePositionParamsHandlerTest extends InstrumentUtilForTest {

    private MergePositionParamsHandler mergePositionParamsHandler;

    @Mock
    private CancelSLTPTask cancelSLTPTaskMock;
    @Mock
    private BasicTaskObservable basicTaskMock;
    @Mock
    private TaskParamsUtil taskParamsUtilMock;
    @Mock
    private MergePositionParams mergePositionParamsMock;
    private TestObserver<OrderEvent> testObserver;
    private final Set<IOrder> toMergeOrders = Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD);
    private final ComposeParams composeParams = new ComposeParams();
    private static final String mergeOrderLabel = "mergeOrderLabel";

    @Before
    public void setUp() {
        setUpMocks();

        mergePositionParamsHandler = new MergePositionParamsHandler(cancelSLTPTaskMock,
                                                                    basicTaskMock,
                                                                    taskParamsUtilMock);
    }

    private void setUpMocks() {
        when(mergePositionParamsMock.mergeOrderLabel())
            .thenReturn(mergeOrderLabel);
        when(mergePositionParamsMock.cancelSLTPComposeParams())
            .thenReturn(composeParams);
        when(mergePositionParamsMock.mergeComposeParams())
            .thenReturn(composeParams);
    }

    public class ObserveCancelSLTP {

        private Observable<OrderEvent> observableFromCancelSLTPTask;
        private Observable<OrderEvent> observableFromTaskParamsUtil;

        @Before
        public void setUp() {
            observableFromCancelSLTPTask = eventObservable(mergeEvent);
            observableFromTaskParamsUtil = eventObservable(mergeRejectEvent);

            when(cancelSLTPTaskMock.observe(toMergeOrders, mergePositionParamsMock))
                .thenReturn(observableFromCancelSLTPTask);

            when(taskParamsUtilMock.composeParams(observableFromCancelSLTPTask,
                                                  composeParams))
                                                      .thenReturn(observableFromTaskParamsUtil);

            testObserver = mergePositionParamsHandler
                .observeCancelSLTP(toMergeOrders, mergePositionParamsMock)
                .test();
        }

        @Test
        public void callToCancelSLTPIsCorrect() {
            verify(cancelSLTPTaskMock).observe(toMergeOrders, mergePositionParamsMock);
        }

        @Test
        public void returnedObservableIsCorrectComposed() {
            testObserver.assertComplete();
            testObserver.assertValue(mergeRejectEvent);
        }
    }

    public class ObserveMerge {

        private Observable<OrderEvent> observableFromBatch;
        private Observable<OrderEvent> observableFromTaskParamsUtil;

        @Before
        public void setUp() {
            observableFromBatch = eventObservable(mergeEvent);
            observableFromTaskParamsUtil = eventObservable(mergeRejectEvent);

            when(basicTaskMock.mergeOrders(mergeOrderLabel, toMergeOrders))
                .thenReturn(observableFromBatch);

            when(taskParamsUtilMock.composeParams(observableFromBatch,
                                                  composeParams))
                                                      .thenReturn(observableFromTaskParamsUtil);

            testObserver = mergePositionParamsHandler
                .observeMerge(toMergeOrders, mergePositionParamsMock)
                .test();
        }

        @Test
        public void callToBasicTaskIsCorrect() {
            verify(basicTaskMock).mergeOrders(mergeOrderLabel, toMergeOrders);
        }

        @Test
        public void returnedObservableIsCorrectComposed() {
            testObserver.assertComplete();
            testObserver.assertValue(mergeRejectEvent);
        }
    }
}
