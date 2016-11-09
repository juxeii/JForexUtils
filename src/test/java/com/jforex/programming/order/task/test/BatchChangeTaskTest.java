package com.jforex.programming.order.task.test;

// @RunWith(HierarchicalContextRunner.class)
// public class BatchChangeTaskTest extends InstrumentUtilForTest {
//
// private BatchChangeTask batchChangeTask;
//
// @Mock
// private BasicTaskObservable basicTaskMock;
// @Mock
// private TaskParamsUtil taskParamsUtilMock;
// @Mock
// private SimpleClosePositionParams simpleClosePositionParams;
// @Mock
// private CancelSLParams cancelSLParamsMock;
// @Mock
// private CancelTPParams cancelTPParamsMock;
//
// private final CloseParams closeParams = CloseParams
// .closeOrder(buyOrderEURUSD)
// .build();
// private final List<IOrder> ordersForBatch =
// Lists.newArrayList(buyOrderEURUSD, sellOrderEURUSD);
// private final OrderEvent testEvent = submitEvent;
// private final OrderEvent composerEvent = closeEvent;
//
// @Before
// public void setUp() throws Exception {
// batchChangeTask = new BatchChangeTask(basicTaskMock, taskParamsUtilMock);
// }
//
// public class CloseBatch {
//
// @Before
// public void setUp() {
// when(basicTaskMock.close(closeParams))
// .thenReturn(neverObservable())
// .thenReturn(eventObservable(testEvent));
// }
//
// @Test
// public void forMergeIsNotConcatenated() {
// batchChangeTask
// .close(instrumentEURUSD,
// ordersForBatch,
// simpleClosePositionParams)
// .test()
// .assertNotComplete()
// .assertValue(composerEvent);
// }
//
// @Test
// public void forConcatIsNotMerged() {
// batchChangeTask
// .close(instrumentEURUSD,
// ordersForBatch,
// simpleClosePositionParams)
// .test()
// .assertNotComplete()
// .assertNoValues();
// }
// }
//
// public class CancelSLBatch {
//
// @Before
// public void setUp() {
// when(basicTaskMock.setStopLossPrice(any()))
// .thenReturn(neverObservable());
// when(basicTaskMock.setStopLossPrice(any()))
// .thenReturn(eventObservable(testEvent));
// }
//
// @Test
// public void forMergeIsNotConcatenated() {
// batchChangeTask
// .cancelSL(ordersForBatch,
// cancelSLParamsMock,
// BatchMode.MERGE)
// .test()
// .assertNotComplete()
// .assertValue(composerEvent);
// }
//
// @Test
// public void forConcatIsNotMerged() {
// batchChangeTask
// .cancelSL(ordersForBatch,
// cancelSLParamsMock,
// BatchMode.CONCAT)
// .test()
// .assertNotComplete()
// .assertNoValues();
// }
// }
//
// public class CancelTPBatch {
//
// @Before
// public void setUp() {
// when(basicTaskMock.setTakeProfitPrice(any()))
// .thenReturn(neverObservable());
// when(basicTaskMock.setTakeProfitPrice(any()))
// .thenReturn(eventObservable(testEvent));
// }
//
// @Test
// public void forMergeIsNotConcatenated() {
// batchChangeTask
// .cancelSL(ordersForBatch,
// cancelSLParamsMock,
// BatchMode.MERGE)
// .test()
// .assertNotComplete()
// .assertValue(composerEvent);
// }
//
// @Test
// public void forConcatIsNotMerged() {
// batchChangeTask
// .cancelSL(ordersForBatch,
// cancelSLParamsMock,
// BatchMode.CONCAT)
// .test()
// .assertNotComplete()
// .assertNoValues();
// }
// }
// }
