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
import com.jforex.programming.order.task.BasicTask;
import com.jforex.programming.order.task.CancelSLTPAndMergeTask;
import com.jforex.programming.order.task.CancelSLTPTask;
import com.jforex.programming.order.task.params.ComposeParams;
import com.jforex.programming.order.task.params.TaskParamsUtil;
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
    private TestObserver<OrderEvent> testObserver;
    private final Map<OrderEventType, Consumer<OrderEvent>> consumerForEvent = new HashMap<>();
    private final Set<IOrder> toMergeOrders = Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD);
    private final ComposeParams composeParams = new ComposeParams();
    private static final String mergeOrderLabel = "mergeOrderLabel";

    @Before
    public void setUp() {
        setUpMocks();

        cancelSLTPAndMergeTask = new CancelSLTPAndMergeTask(cancelSLTPTaskMock,
                                                            basicTaskMock,
                                                            taskParamsUtilMock);
    }

    private void setUpMocks() {
        when(mergePositionParamsMock.mergeOrderLabel())
            .thenReturn(mergeOrderLabel);
        when(mergePositionParamsMock.cancelSLTPComposeParams())
            .thenReturn(composeParams);
        when(mergePositionParamsMock.composeData())
            .thenReturn(composeParams);
        when(mergePositionParamsMock.consumerForEvent())
            .thenReturn(consumerForEvent);
    }

    private void subscribeWithEvents(final Observable<OrderEvent> cancelSLTPObservable,
                                     final Observable<OrderEvent> mergeObservable) {
        when(cancelSLTPTaskMock.observe(toMergeOrders, mergePositionParamsMock))
            .thenReturn(cancelSLTPObservable);
        when(taskParamsUtilMock.composeParams(cancelSLTPObservable,
                                              composeParams))
                                                  .thenReturn(cancelSLTPObservable);

        when(basicTaskMock.mergeOrders(mergeOrderLabel, toMergeOrders))
            .thenReturn(mergeObservable);
        when(taskParamsUtilMock.composeParamsWithEvents(mergeObservable,
                                                        composeParams,
                                                        mergePositionParamsMock.consumerForEvent()))
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
