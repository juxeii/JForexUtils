package com.jforex.programming.order.task.params.position.test;

import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.task.BatchChangeTask;
import com.jforex.programming.order.task.CloseExecutionMode;
import com.jforex.programming.order.task.MergePositionTaskObservable;
import com.jforex.programming.order.task.params.basic.CloseParams;
import com.jforex.programming.order.task.params.position.ClosePositionParams;
import com.jforex.programming.order.task.params.position.ClosePositionParamsHandler;
import com.jforex.programming.order.task.params.position.MergePositionParams;
import com.jforex.programming.position.PositionUtil;
import com.jforex.programming.test.common.InstrumentUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;

@RunWith(HierarchicalContextRunner.class)
public class ClosePositionParamsHandlerTest extends InstrumentUtilForTest {

    private ClosePositionParamsHandler closePositionParamsHandler;

    @Mock
    private MergePositionTaskObservable mergePositionTaskObservableMock;
    @Mock
    private BatchChangeTask batchChangeTaskMock;
    @Mock
    private PositionUtil positionUtilMock;
    @Mock
    private ClosePositionParams closePositionParamsMock;
    @Mock
    private CloseParams closeParamsMock;
    @Mock
    private MergePositionParams mergePositionParamsMock;
    private TestObserver<OrderEvent> testObserver;
    private final OrderEvent testEvent = closeEvent;
    private final Function<IOrder, CloseParams> closeParamsFactory =
            order -> closeParamsMock;

    @Before
    public void setUp() {
        setUpMocks();

        closePositionParamsHandler = new ClosePositionParamsHandler(mergePositionTaskObservableMock,
                                                                    batchChangeTaskMock,
                                                                    positionUtilMock);
    }

    private void setUpMocks() {
        when(closePositionParamsMock.closeParamsFactory())
            .thenReturn(closeParamsFactory);
        when(closePositionParamsMock.mergePositionParams())
            .thenReturn(mergePositionParamsMock);
    }

    @Test
    public void observeMergeDelegatesToMergePositionTaskMock() {
        when(mergePositionTaskObservableMock.merge(anyCollection(), eq(mergePositionParamsMock)))
            .thenReturn(eventObservable(testEvent));

        testObserver = closePositionParamsHandler
            .observeMerge(closePositionParamsMock)
            .test();

        testObserver.assertComplete();
        testObserver.assertValue(testEvent);
    }

    @Test
    public void emptyMergeObservableIsReturnedWhenClosingOnlyOpenedOrders() {
        when(closePositionParamsMock.closeExecutionMode())
            .thenReturn(CloseExecutionMode.CloseOpened);

        testObserver = closePositionParamsHandler
            .observeMerge(closePositionParamsMock)
            .test();

        testObserver.assertComplete();
        testObserver.assertNoValues();
    }

    public class ObserveClose {

        private Observable<OrderEvent> returnedObservable;

        @Before
        public void setUp() {
            returnedObservable = eventObservable(testEvent);

            when(batchChangeTaskMock.close(anyCollection(), any()))
                .thenReturn(returnedObservable);

            testObserver = closePositionParamsHandler
                .observeClose(closePositionParamsMock)
                .test();
        }

        @Test
        public void observeCloseCallsBatchTaskMockCorrect() {
            verify(batchChangeTaskMock).close(anyCollection(), any());
        }

        @Test
        public void returnedObservableIsCorrectComposed() {
            testObserver.assertComplete();
            testObserver.assertValue(testEvent);
        }
    }
}
