package com.jforex.programming.order.task.test;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.google.common.collect.Sets;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.task.BasicTask;
import com.jforex.programming.order.task.CancelSLTPAndMergeTask;
import com.jforex.programming.order.task.CancelSLTPTask;
import com.jforex.programming.order.task.params.ComposeDataImpl;
import com.jforex.programming.order.task.params.TaskParamsBase;
import com.jforex.programming.order.task.params.TaskParamsUtil;
import com.jforex.programming.order.task.params.basic.MergeParamsForPosition;
import com.jforex.programming.order.task.params.position.MergePositionParams;
import com.jforex.programming.test.common.InstrumentUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;

@RunWith(HierarchicalContextRunner.class)
public class CancelSLTPAndMergeTaskTest extends InstrumentUtilForTest {

    private CancelSLTPAndMergeTask cancelSLTPAndMergeTask;

    @Mock
    private CancelSLTPTask cancelSLTPTaskMock;
    @Mock
    private BasicTask basicTaskMock;
    @Mock
    private TaskParamsUtil taskParamsUtilMock;
    @Mock
    private MergePositionParams mergePositionParamsMock;
    @Mock
    private MergeParamsForPosition mergeParamsForPositionMock;
    @Mock
    private TaskParamsBase cancelSLTPParamsMock;
    private TestObserver<OrderEvent> testObserver;
    private final Set<IOrder> toMergeOrders = Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD);
    private final ComposeDataImpl composeData = new ComposeDataImpl();
    private static final String mergeOrderLabel = "mergeOrderLabel";

    @Before
    public void setUp() {
        setUpMocks();

        cancelSLTPAndMergeTask = new CancelSLTPAndMergeTask(cancelSLTPTaskMock,
                                                            basicTaskMock,
                                                            taskParamsUtilMock);
    }

    private void setUpMocks() {
        when(mergePositionParamsMock.mergeParamsForPosition())
            .thenReturn(mergeParamsForPositionMock);
        when(mergePositionParamsMock.mergeOrderLabel())
            .thenReturn(mergeOrderLabel);
        when(mergeParamsForPositionMock.composeData())
            .thenReturn(composeData);

        when(mergePositionParamsMock.cancelSLTPParams())
            .thenReturn(cancelSLTPParamsMock);
        when(cancelSLTPParamsMock.composeData())
            .thenReturn(composeData);
        when(mergePositionParamsMock.composeData())
            .thenReturn(composeData);
    }

    private void subscribeWithEvents(final Observable<OrderEvent> cancelSLTPObservable,
                                     final Observable<OrderEvent> mergeObservable) {
        when(cancelSLTPTaskMock.observe(toMergeOrders, mergePositionParamsMock))
            .thenReturn(cancelSLTPObservable);
        when(taskParamsUtilMock.compose(cancelSLTPObservable, cancelSLTPParamsMock))
            .thenReturn(cancelSLTPObservable);

        when(basicTaskMock.mergeOrders(mergeOrderLabel, toMergeOrders))
            .thenReturn(mergeObservable);
        when(taskParamsUtilMock.compose(mergeObservable, mergeParamsForPositionMock))
            .thenReturn(mergeObservable);

        testObserver = cancelSLTPAndMergeTask
            .observe(toMergeOrders, mergePositionParamsMock)
            .test();
    }

    public class ObservablesAreDoneSetup {

        @Before
        public void setUp() {
            subscribeWithEvents(eventObservable(changedSLEvent), eventObservable(mergeEvent));
        }

        @Test
        public void callToCancelSLTPIsCorrect() {
            verify(cancelSLTPTaskMock).observe(toMergeOrders, mergePositionParamsMock);
        }

        @Test
        public void callToBasicTaskIsCorrect() {
            verify(basicTaskMock).mergeOrders(mergeOrderLabel, toMergeOrders);
        }

        @Test
        public void returnedObservableIsCorrectComposed() {
            testObserver.assertComplete();
            testObserver.assertValues(changedSLEvent, mergeEvent);
        }
    }

    public class CancelSLTPAndMergeAreConcatenated {

        @Before
        public void setUp() {
            subscribeWithEvents(neverObservable(), eventObservable(mergeEvent));
        }

        @Test
        public void callToCancelSLTPIsCorrect() {
            verify(cancelSLTPTaskMock).observe(toMergeOrders, mergePositionParamsMock);
        }

        @Test
        public void callToBasicTaskIsCorrect() {
            verify(basicTaskMock).mergeOrders(mergeOrderLabel, toMergeOrders);
        }

        @Test
        public void observablesAreConcatenated() {
            testObserver.assertNotComplete();
            testObserver.assertNoValues();
        }
    }
}
