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
import com.jforex.programming.order.task.params.TaskParamsUtil;
import com.jforex.programming.order.task.params.position.MergePositionParams;
import com.jforex.programming.order.task.params.position.MergePositionParamsHandler;
import com.jforex.programming.order.task.params.position.SimpleMergePositionParams;
import com.jforex.programming.test.common.InstrumentUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.observers.TestObserver;

@RunWith(HierarchicalContextRunner.class)
public class MergeParamsHandlerTest extends InstrumentUtilForTest {

    private MergePositionParamsHandler paramsHandler;

    @Mock
    private CancelSLTPTask orderCancelSLAndTPMock;
    @Mock
    private BasicTaskObservable basicTaskMock;
    @Mock
    private TaskParamsUtil taskParamsUtilMock;
    @Mock
    private MergePositionParams mergePositionParamsMock;
    @Mock
    private SimpleMergePositionParams simpleMergePositionParamsMock;
    private TestObserver<OrderEvent> testObserver;
    private final Set<IOrder> toMergeOrders = Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD);
    private final String mergeOrderLabel = "mergeOrderLabel";
    private final OrderEvent testEvent = closeEvent;
    private final OrderEvent composerEvent = changedLabelEvent;

    @Before
    public void setUp() {
        setUpMocks();

        paramsHandler = new MergePositionParamsHandler(orderCancelSLAndTPMock,
                                                       basicTaskMock,
                                                       taskParamsUtilMock);
    }

    private void setUpMocks() {
        when(mergePositionParamsMock.simpleMergePositionParams()).thenReturn(simpleMergePositionParamsMock);

        when(simpleMergePositionParamsMock.mergeOrderLabel(instrumentEURUSD)).thenReturn(mergeOrderLabel);

        when(orderCancelSLAndTPMock.observe(toMergeOrders, mergePositionParamsMock))
            .thenReturn(eventObservable(testEvent));
    }

    @Test
    public void observeCancelSLTPDelegatesToCancelSLTPMock() {
        testObserver = paramsHandler
            .observeCancelSLTP(toMergeOrders, mergePositionParamsMock)
            .test();

        testObserver.assertComplete();
        testObserver.assertValue(testEvent);
    }

    public class ObserveMerge {

        @Before
        public void setUp() {
            when(basicTaskMock.mergeOrders(mergeOrderLabel, toMergeOrders))
                .thenReturn(eventObservable(testEvent));

            testObserver = paramsHandler
                .observeMerge(toMergeOrders, simpleMergePositionParamsMock)
                .test();
        }

        @Test
        public void observeMergeCallsBasicTaskMockCorrect() {
            verify(basicTaskMock).mergeOrders(mergeOrderLabel, toMergeOrders);
        }

        @Test
        public void returnedObservableIsCorrectComposed() {
            testObserver.assertComplete();
            testObserver.assertValue(composerEvent);
        }
    }
}
