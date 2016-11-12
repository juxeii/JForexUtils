// package com.jforex.programming.order.task.test;
//
// import java.util.HashMap;
// import java.util.List;
// import java.util.Map;
// import java.util.function.Consumer;
//
// import org.junit.Before;
// import org.junit.Test;
// import org.junit.runner.RunWith;
// import org.mockito.Mock;
//
// import com.dukascopy.api.IOrder;
// import com.google.common.collect.Lists;
// import com.jforex.programming.order.event.OrderEvent;
// import com.jforex.programming.order.event.OrderEventType;
// import com.jforex.programming.order.task.BatchChangeTask;
// import com.jforex.programming.order.task.BatchComposer;
// import com.jforex.programming.order.task.BatchMode;
// import com.jforex.programming.order.task.params.ComposeParams;
// import com.jforex.programming.order.task.params.position.ClosePositionParams;
// import com.jforex.programming.order.task.params.position.MergePositionParams;
// import com.jforex.programming.test.common.InstrumentUtilForTest;
//
// import de.bechte.junit.runners.context.HierarchicalContextRunner;
//
// @RunWith(HierarchicalContextRunner.class)
// public class BatchChangeTaskTest extends InstrumentUtilForTest {
//
// private BatchChangeTask batchChangeTask;
//
// @Mock
// private BatchComposer batchComposerMock;
// @Mock
// private ClosePositionParams closePositionParamsMock;
// @Mock
// private MergePositionParams mergePositionParamsMock;
// private final List<IOrder> ordersForBatch =
// Lists.newArrayList(buyOrderEURUSD, sellOrderEURUSD);
// private final Map<OrderEventType, Consumer<OrderEvent>> consumersForParams =
// new HashMap<>();
// private final ComposeParams composeParams = new ComposeParams();
//
// @Before
// public void setUp() throws Exception {
// when(closePositionParamsMock.consumerForEvent()).thenReturn(consumersForParams);
// when(mergePositionParamsMock.consumerForEvent()).thenReturn(consumersForParams);
//
// when(closePositionParamsMock.closeComposeParams(any())).thenReturn(composeParams);
// when(mergePositionParamsMock.batchCancelTPMode()).thenReturn(BatchMode.MERGE);
//
// batchChangeTask = new BatchChangeTask(batchComposerMock);
// }
//
// public class CloseBatch {
//
// @Before
// public void setUp() {
// composedObservable = eventObservable(closeEvent);
// }
//
// @Test
// public void isMerged() {
// batchChangeTask
// .close(ordersForBatch, closePositionParamsMock)
// .test()
// .assertNotComplete()
// .assertValue(closeEvent);
// }
// }
//
// public class CancelSLBatch {
//
// @Before
// public void setUp() {
// composedObservable = eventObservable(changedSLEvent);
//
// when(mergePositionParamsMock.cancelSLComposeParams(any()))
// .thenReturn(composeParams);
// }
//
// @Test
// public void forMergeIsNotConcatenated() {
// when(mergePositionParamsMock.batchCancelSLMode()).thenReturn(BatchMode.MERGE);
//
// batchChangeTask
// .cancelSL(ordersForBatch, mergePositionParamsMock)
// .test()
// .assertNotComplete()
// .assertValue(changedSLEvent);
// }
//
// @Test
// public void forConcatIsNotMerged() {
// when(mergePositionParamsMock.batchCancelSLMode()).thenReturn(BatchMode.CONCAT);
//
// batchChangeTask
// .cancelSL(ordersForBatch, mergePositionParamsMock)
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
// composedObservable = eventObservable(changedTPEvent);
//
// when(mergePositionParamsMock.cancelTPComposeParams(any()))
// .thenReturn(composeParams);
// }
//
// @Test
// public void forMergeIsNotConcatenated() {
// when(mergePositionParamsMock.batchCancelTPMode()).thenReturn(BatchMode.MERGE);
//
// batchChangeTask
// .cancelTP(ordersForBatch, mergePositionParamsMock)
// .test()
// .assertNotComplete()
// .assertValue(changedTPEvent);
// }
//
// @Test
// public void forConcatIsNotMerged() {
// when(mergePositionParamsMock.batchCancelTPMode()).thenReturn(BatchMode.CONCAT);
//
// batchChangeTask
// .cancelTP(ordersForBatch, mergePositionParamsMock)
// .test()
// .assertNotComplete()
// .assertNoValues();
// }
// }
// }
