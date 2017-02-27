package com.jforex.programming.order.task.params.position.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.task.BatchMode;
import com.jforex.programming.order.task.CancelSLTPMode;
import com.jforex.programming.order.task.params.TaskParamsBase;
import com.jforex.programming.order.task.params.TaskParamsType;
import com.jforex.programming.order.task.params.basic.CancelSLParams;
import com.jforex.programming.order.task.params.basic.CancelTPParams;
import com.jforex.programming.order.task.params.basic.MergeParamsForPosition;
import com.jforex.programming.order.task.params.position.MergePositionParams;
import com.jforex.programming.order.task.params.test.CommonParamsForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;

@RunWith(HierarchicalContextRunner.class)
public class MergePositionParamsTest extends CommonParamsForTest {

    private MergePositionParams mergePositionParams;

    @Mock
    private MergeParamsForPosition mergeParamsForPositionMock;
    @Mock
    private TaskParamsBase cancelSLTPParamsMock;
    @Mock
    private TaskParamsBase batchCancelSLParamsMock;
    @Mock
    private TaskParamsBase batchCancelTPParamsMock;
    @Mock
    private CancelSLParams cancelSLParamsMock;
    @Mock
    private CancelTPParams cancelTPParamsMock;
    @Mock
    private Function<IOrder, CancelSLParams> cancelSLParamsFactoryMock;
    @Mock
    private Function<IOrder, CancelTPParams> cancelTPParamsFactoryMock;

    public class DefaultTests {

        @Before
        public void setUp() {
            mergePositionParams = MergePositionParams
                .newBuilder(instrumentEURUSD, mergeOrderLabel)
                .build();
        }

        @Test
        public void instrumentIsCorrect() {
            assertThat(mergePositionParams.instrument(), equalTo(instrumentEURUSD));
        }

        @Test
        public void mergeOrderLabelIsCorrect() {
            assertThat(mergePositionParams.mergeOrderLabel(), equalTo(mergeOrderLabel));
        }

        @Test
        public void cancelSLTPParamsIsOfTypeEmptyTaskParams() {
            assertEmptyParamsType(mergePositionParams.cancelSLTPParams());
        }

        private void assertEmptyParamsType(final TaskParamsBase taskParams) {
            assertTrue(taskParams instanceof TaskParamsBase);
        }

        @Test
        public void batchCancelSLParamsIsOfTypeEmptyTaskParams() {
            assertEmptyParamsType(mergePositionParams.batchCancelSLParams());
        }

        @Test
        public void batchCancelTPParamsIsOfTypeEmptyTaskParams() {
            assertEmptyParamsType(mergePositionParams.batchCancelTPParams());
        }

        @Test
        public void cancelSLParamsFactoryProducesTypeOfEmptyTaskParams() {
            assertEmptyParamsType(mergePositionParams.cancelSLParamsFactory().apply(orderForTest));
        }

        @Test
        public void cancelTPParamsFactoryProducesTypeOfEmptyTaskParams() {
            assertEmptyParamsType(mergePositionParams.cancelTPParamsFactory().apply(orderForTest));
        }

        @Test
        public void mergeExecutionModeIsOfTypeMergeCancelSLAndTP() {
            assertThat(mergePositionParams.mergeExecutionMode(), equalTo(CancelSLTPMode.MergeCancelSLAndTP));
        }

        @Test
        public void batchCancelSLModeIsOfTypeMerge() {
            assertThat(mergePositionParams.batchCancelSLMode(), equalTo(BatchMode.MERGE));
        }

        @Test
        public void batchCancelTPModeIsOfTypeMerge() {
            assertThat(mergePositionParams.batchCancelTPMode(), equalTo(BatchMode.MERGE));
        }

        @Test
        public void typeIsMERGEPOSITION() {
            assertThat(mergePositionParams.type(), equalTo(TaskParamsType.MERGEPOSITION));
        }

        @Test
        public void noConsumersForEvents() {
            assertTrue(mergePositionParams
                .composeData()
                .consumerByEventType()
                .isEmpty());
        }
    }

    public class FullConfigureTests {

        @Before
        public void setUp() {
            mergePositionParams = MergePositionParams
                .newBuilder(instrumentEURUSD, mergeOrderLabel)
                .withMergeExecutionMode(CancelSLTPMode.ConcatCancelSLAndTP)
                .withBatchCancelSLMode(BatchMode.CONCAT)
                .withBatchCancelTPMode(BatchMode.CONCAT)
                .withCancelSLTPParams(cancelSLTPParamsMock)
                .withBatchCancelSLParams(batchCancelSLParamsMock)
                .withBatchCancelTPParams(batchCancelTPParamsMock)
                .withCancelSLParamsFactory(cancelSLParamsFactoryMock)
                .withCancelTPParamsFactory(cancelTPParamsFactoryMock)
                .withMergeParamsForPosition(mergeParamsForPositionMock)
                .doOnStart(actionMock)
                .doOnComplete(actionMock)
                .doOnError(errorConsumerMock)
                .retryOnReject(retryParams)
                .build();
        }

        @Test
        public void cancelSLTPParamsIsCorrect() {
            assertThat(mergePositionParams.cancelSLTPParams(), equalTo(cancelSLTPParamsMock));
        }

        @Test
        public void batchCancelSLParamsIsCorrect() {
            assertThat(mergePositionParams.batchCancelSLParams(), equalTo(batchCancelSLParamsMock));
        }

        @Test
        public void batchCancelTPParamsIsCorrect() {
            assertThat(mergePositionParams.batchCancelTPParams(), equalTo(batchCancelTPParamsMock));
        }

        @Test
        public void cancelSLParamsFactoryIsCorrect() {
            assertThat(mergePositionParams.cancelSLParamsFactory(), equalTo(cancelSLParamsFactoryMock));
        }

        @Test
        public void cancelTPParamsFactoryIsCorrect() {
            assertThat(mergePositionParams.cancelTPParamsFactory(), equalTo(cancelTPParamsFactoryMock));
        }

        @Test
        public void mergeParamsForPositionIsCorrect() {
            assertThat(mergePositionParams.mergeParamsForPosition(), equalTo(mergeParamsForPositionMock));
        }

        @Test
        public void mergeExecutionModeIsOfTypeConcatCancelSLAndTP() {
            assertThat(mergePositionParams.mergeExecutionMode(), equalTo(CancelSLTPMode.ConcatCancelSLAndTP));
        }

        @Test
        public void batchCancelSLModeIsOfTypeCONCAT() {
            assertThat(mergePositionParams.batchCancelSLMode(), equalTo(BatchMode.CONCAT));
        }

        @Test
        public void batchCancelTPModeIsOfTypeCONCAT() {
            assertThat(mergePositionParams.batchCancelTPMode(), equalTo(BatchMode.CONCAT));
        }

        @Test
        public void assertComposeDataAreCorrect() {
            assertComposeData(mergePositionParams.composeData());
        }
    }
}
