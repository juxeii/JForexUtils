package com.jforex.programming.order.task.test;

//@RunWith(HierarchicalContextRunner.class)
//public class BatchChangeTaskTest extends InstrumentUtilForTest {
//
//    private BatchChangeTask batchChangeTask;
//
//    @Mock
//    private BasicTaskObservable basicTaskMock;
//    @Mock
//    private TaskParamsUtil taskParamsUtilMock;
//    @Mock
//    private CloseParams closeParamsMock;
//    @Mock
//    private CancelSLParams cancelSLParamsMock;
//    @Mock
//    private CancelTPParams cancelTPParamsMock;
//    private final List<IOrder> ordersForBatch = Lists.newArrayList(buyOrderEURUSD, sellOrderEURUSD);
//
//    @Before
//    public void setUp() throws Exception {
//        batchChangeTask = new BatchChangeTask(basicTaskMock, taskParamsUtilMock);
//    }
//
//    public class CloseBatch {
//
//        private final Function<IOrder, CloseParams> closeParamsFactory =
//                order -> closeParamsMock;
//
//        @Before
//        public void setUp() {
//            when(basicTaskMock.close(closeParamsMock))
//                .thenReturn(neverObservable())
//                .thenReturn(eventObservable(closeEvent));
//
//            when(taskParamsUtilMock.composeEventHandling(any(), eq(closeParamsMock)))
//                .thenReturn(neverObservable())
//                .thenReturn(eventObservable(closeEvent));
//        }
//
//        @Test
//        public void isNotConcatenated() {
//            batchChangeTask
//                .close(ordersForBatch, closeParamsFactory)
//                .test()
//                .assertNotComplete()
//                .assertValue(closeEvent);
//        }
//    }
//
//    public class CancelSLBatch {
//
//        private final Function<IOrder, CancelSLParams> cancelSLParamsFactory =
//                order -> cancelSLParamsMock;
//
//        @Before
//        public void setUp() {
//            when(basicTaskMock.setStopLossPrice(any()))
//                .thenReturn(neverObservable());
//            when(basicTaskMock.setStopLossPrice(any()))
//                .thenReturn(eventObservable(changedSLEvent));
//
//            when(taskParamsUtilMock.composeEventHandling(any(), eq(cancelSLParamsMock)))
//                .thenReturn(neverObservable())
//                .thenReturn(eventObservable(changedSLEvent));
//        }
//
//        @Test
//        public void forMergeIsNotConcatenated() {
//            batchChangeTask
//                .cancelSL(ordersForBatch,
//                          cancelSLParamsFactory,
//                          BatchMode.MERGE)
//                .test()
//                .assertNotComplete()
//                .assertValue(changedSLEvent);
//        }
//
//        @Test
//        public void forConcatIsNotMerged() {
//            batchChangeTask
//                .cancelSL(ordersForBatch,
//                          cancelSLParamsFactory,
//                          BatchMode.CONCAT)
//                .test()
//                .assertNotComplete()
//                .assertNoValues();
//        }
//    }
//
//    public class CancelTPBatch {
//
//        private final Function<IOrder, CancelTPParams> cancelTPParamsFactory =
//                order -> cancelTPParamsMock;
//
//        @Before
//        public void setUp() {
//            when(basicTaskMock.setTakeProfitPrice(any()))
//                .thenReturn(neverObservable());
//            when(basicTaskMock.setTakeProfitPrice(any()))
//                .thenReturn(eventObservable(changedTPEvent));
//
//            when(taskParamsUtilMock.composeEventHandling(any(), eq(cancelTPParamsMock)))
//                .thenReturn(neverObservable())
//                .thenReturn(eventObservable(changedTPEvent));
//        }
//
//        @Test
//        public void forMergeIsNotConcatenated() {
//            batchChangeTask
//                .cancelTP(ordersForBatch,
//                          cancelTPParamsFactory,
//                          BatchMode.MERGE)
//                .test()
//                .assertNotComplete()
//                .assertValue(changedTPEvent);
//        }
//
//        @Test
//        public void forConcatIsNotMerged() {
//            batchChangeTask
//                .cancelTP(ordersForBatch,
//                          cancelTPParamsFactory,
//                          BatchMode.CONCAT)
//                .test()
//                .assertNotComplete()
//                .assertNoValues();
//        }
//    }
//}
