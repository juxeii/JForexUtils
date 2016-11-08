package com.jforex.programming.order.task.params.position.test;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.google.common.collect.Sets;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventTransformer;
import com.jforex.programming.order.task.BasicTaskObservable;
import com.jforex.programming.order.task.CancelSLTPTask;
import com.jforex.programming.order.task.params.position.MergePositionParams;
import com.jforex.programming.order.task.params.position.MergePositionParamsHandler;
import com.jforex.programming.test.common.InstrumentUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;

@RunWith(HierarchicalContextRunner.class)
public class MergeParamsHandlerTest extends InstrumentUtilForTest {

    private MergePositionParamsHandler paramsHandler;

    @Mock
    private CancelSLTPTask orderCancelSLAndTPMock;
    @Mock
    private BasicTaskObservable orderBasicTaskMock;
    @Mock
    private MergePositionParams mergeParamsMock;
    private TestObserver<OrderEvent> testObserver;
    private final Set<IOrder> toMergeOrders = Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD);
    private final String mergeOrderLabel = "mergeOrderLabel";
    private final OrderEvent testEvent = closeEvent;
    private final OrderEvent composerEvent = changedLabelEvent;
    private final OrderEventTransformer testComposer =
            upstream -> upstream.flatMap(orderEvent -> Observable.just(composerEvent));

    @Before
    public void setUp() {
        setUpMocks();

        paramsHandler = new MergePositionParamsHandler(orderCancelSLAndTPMock, orderBasicTaskMock);
    }

    private void setUpMocks() {
        when(mergeParamsMock.mergeOrderLabel()).thenReturn(mergeOrderLabel);
        when(mergeParamsMock.mergeComposer()).thenReturn(testComposer);

        when(orderCancelSLAndTPMock.observe(toMergeOrders, mergeParamsMock))
            .thenReturn(eventObservable(testEvent));
    }

    @Test
    public void observeCancelSLTPDelegatesToCancelSLTPMock() {
        testObserver = paramsHandler
            .observeCancelSLTP(toMergeOrders, mergeParamsMock)
            .test();

        testObserver.assertComplete();
        testObserver.assertValue(testEvent);
    }

    public class ObserveMerge {

        @Before
        public void setUp() {
            when(orderBasicTaskMock.mergeOrders(mergeOrderLabel, toMergeOrders))
                .thenReturn(eventObservable(testEvent));

            testObserver = paramsHandler
                .observeMerge(toMergeOrders, mergeParamsMock)
                .test();
        }

        @Test
        public void observeMergeCallsBasicTaskMockCorrect() {
            verify(orderBasicTaskMock).mergeOrders(mergeOrderLabel, toMergeOrders);
        }

        @Test
        public void returnedObservableIsCorrectComposed() {
            testObserver.assertComplete();
            testObserver.assertValue(composerEvent);
        }
    }
}
