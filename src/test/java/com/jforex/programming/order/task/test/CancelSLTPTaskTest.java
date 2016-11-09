package com.jforex.programming.order.task.test;

// @RunWith(HierarchicalContextRunner.class)
// public class CancelSLTPTaskTest extends InstrumentUtilForTest {
//
// private CancelSLTPTask cancelSLTPTask;
//
// @Mock
// private BatchChangeTask batchChangeTaskMock;
// @Mock
// private TaskParamsUtil taskParamsUtilMock;
// @Mock
// private MergePositionParams mergeCommandMock;
// private TestObserver<OrderEvent> testObserver;
// private final Set<IOrder> toCancelSLTPOrders =
// Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD);
// private final OrderEvent testEvent = mergeEvent;
// private final OrderEvent composerEvent = closeEvent;
//
// @Before
// public void setUp() {
// cancelSLTPTask = new CancelSLTPTask(batchChangeTaskMock, taskParamsUtilMock);
// }
//
// private void setUpCommandObservables(final Observable<OrderEvent>
// cancelSLObservable,
// final Observable<OrderEvent> cancelTPObservable) {
// when(batchChangeTaskMock.cancelSL(eq(toCancelSLTPOrders), any(), any()))
// .thenReturn(cancelSLObservable);
// when(batchChangeTaskMock.cancelTP(eq(toCancelSLTPOrders), any(), any()))
// .thenReturn(cancelTPObservable);
// }
//
// private void subscribeWithOrders(final Set<IOrder> orders) {
// testObserver = cancelSLTPTask
// .observe(orders, mergeCommandMock)
// .test();
// }
//
// @Test
// public void batchTaskIsDeferred() {
// verifyZeroInteractions(batchChangeTaskMock);
// }
//
// @Test
// public void observeTaskIsEmptyForNoOrders() {
// subscribeWithOrders(Sets.newHashSet());
//
// testObserver.assertComplete();
// testObserver.assertNoValues();
// }
//
// @Test
// public void observeTaskIsEmptyForOnerders() {
// subscribeWithOrders(Sets.newHashSet(buyOrderEURUSD));
//
// testObserver.assertComplete();
// testObserver.assertNoValues();
// }
//
// public class ObserveTaskSetup {
//
// @Before
// public void setUp() {
// when(mergeCommandMock.cancelSLTPComposer())
// .thenReturn(testComposer);
// }
//
// private void setExecutionMode(final MergeExecutionMode mode) {
// when(mergeCommandMock.executionMode()).thenReturn(mode);
// }
//
// @Test
// public void cancelSLAndCancelTPAreConcatenated() {
// setExecutionMode(MergeExecutionMode.ConcatCancelSLAndTP);
// setUpCommandObservables(neverObservable(), eventObservable(testEvent));
//
// subscribeWithOrders(toCancelSLTPOrders);
//
// testObserver.assertNotComplete();
// testObserver.assertNoValues();
// }
//
// @Test
// public void cancelTPAndCancelSLAreConcatenated() {
// setExecutionMode(MergeExecutionMode.ConcatCancelTPAndSL);
// setUpCommandObservables(eventObservable(testEvent), neverObservable());
//
// subscribeWithOrders(toCancelSLTPOrders);
//
// testObserver.assertNotComplete();
// testObserver.assertNoValues();
// }
//
// @Test
// public void cancelSLAndTPAreMerged() {
// setExecutionMode(MergeExecutionMode.MergeCancelSLAndTP);
// setUpCommandObservables(neverObservable(), eventObservable(testEvent));
//
// subscribeWithOrders(toCancelSLTPOrders);
//
// testObserver.assertNotComplete();
// testObserver.assertValue(composerEvent);
// }
// }
// }
