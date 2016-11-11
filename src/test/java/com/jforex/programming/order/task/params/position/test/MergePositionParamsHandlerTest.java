//package com.jforex.programming.order.task.params.position.test;
//
//import java.util.Set;
//
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.Mock;
//
//import com.dukascopy.api.IOrder;
//import com.google.common.collect.Sets;
//import com.jforex.programming.order.event.OrderEvent;
//import com.jforex.programming.order.task.BasicTaskObservable;
//import com.jforex.programming.order.task.CancelSLTPTask;
//import com.jforex.programming.order.task.params.TaskParamsUtil;
//import com.jforex.programming.order.task.params.position.MergePositionParams;
//import com.jforex.programming.order.task.params.position.MergePositionParamsHandler;
//import com.jforex.programming.test.common.InstrumentUtilForTest;
//
//import de.bechte.junit.runners.context.HierarchicalContextRunner;
//import io.reactivex.Observable;
//import io.reactivex.observers.TestObserver;
//
//@RunWith(HierarchicalContextRunner.class)
//public class MergePositionParamsHandlerTest extends InstrumentUtilForTest {
//
//    private MergePositionParamsHandler mergePositionParamsHandler;
//
//    @Mock
//    private CancelSLTPTask cancelSLTPTaskMock;
//    @Mock
//    private BasicTaskObservable basicTaskMock;
//    @Mock
//    private TaskParamsUtil taskParamsUtilMock;
//    @Mock
//    private MergePositionParams mergePositionParamsMock;
//    private TestObserver<OrderEvent> testObserver;
//    private final String mergeOrderLabel = "mergeOrderLabel";
//    private final Set<IOrder> toMergeOrders = Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD);
//    private final OrderEvent testEvent = mergeEvent;
//
//    @Before
//    public void setUp() {
//        setUpMocks();
//
//        mergePositionParamsHandler = new MergePositionParamsHandler(cancelSLTPTaskMock,
//                                                                    basicTaskMock,
//                                                                    taskParamsUtilMock);
//    }
//
//    private void setUpMocks() {
//        when(cancelSLTPTaskMock.observe(toMergeOrders, mergePositionParamsMock))
//            .thenReturn(eventObservable(testEvent));
//    }
//
//    @Test
//    public void observeCancelSLTPDelegatesToCancelSLTPMock() {
//        testObserver = mergePositionParamsHandler
//            .observeCancelSLTP(toMergeOrders, mergePositionParamsMock)
//            .test();
//
//        testObserver.assertComplete();
//        testObserver.assertValue(testEvent);
//    }
//
//    public class ObserveMerge {
//
//        private Observable<OrderEvent> returnedObservable;
//
//        @Before
//        public void setUp() {
//            returnedObservable = eventObservable(testEvent);
//
//            when(basicTaskMock.mergeOrders(mergeOrderLabel, toMergeOrders))
//                .thenReturn(eventObservable(testEvent));
//
//            testObserver = mergePositionParamsHandler
//                .observeMerge(toMergeOrders, mergePositionParamsMock)
//                .test();
//        }
//
//        @Test
//        public void observeMergeCallsBasicTaskMockCorrect() {
//            verify(basicTaskMock).mergeOrders(mergeOrderLabel, toMergeOrders);
//        }
//
//        @Test
//        public void returnedObservableIsCorrectComposed() {
//            testObserver.assertComplete();
//            testObserver.assertValue(testEvent);
//        }
//    }
//}
