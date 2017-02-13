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
import com.jforex.programming.order.task.CloseExecutionMode;
import com.jforex.programming.order.task.params.TaskParamsType;
import com.jforex.programming.order.task.params.basic.CloseParams;
import com.jforex.programming.order.task.params.position.ClosePositionParams;
import com.jforex.programming.order.task.params.position.MergePositionParams;
import com.jforex.programming.order.task.params.test.CommonParamsForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;

@RunWith(HierarchicalContextRunner.class)
public class ClosePositionParamsTest extends CommonParamsForTest {

    private ClosePositionParams closePositionParams;

    @Mock
    private MergePositionParams mergePositionParamsMock;
    @Mock
    private CloseParams closeParamsMock;
    @Mock
    private Function<IOrder, CloseParams> closeParamsFactoryMock;

    @Before
    public void setUp() {
        when(mergePositionParamsMock.instrument()).thenReturn(instrumentEURUSD);
    }

    public class DefaultTests {

        @Before
        public void setUp() {
            closePositionParams = ClosePositionParams
                .newBuilder(mergePositionParamsMock, closeParamsFactoryMock)
                .build();
        }

        @Test
        public void mergePositionParamsIsCorrect() {
            assertThat(closePositionParams.mergePositionParams(), equalTo(mergePositionParamsMock));
        }

        @Test
        public void closeParamsFactoryIsCorrect() {
            assertThat(closePositionParams.closeParamsFactory(), equalTo(closeParamsFactoryMock));
        }

        @Test
        public void instrumentIsCorrect() {
            assertThat(closePositionParams.instrument(), equalTo(instrumentEURUSD));
        }

        @Test
        public void closeExecutionModeIsCorrect() {
            assertThat(closePositionParams.closeExecutionMode(), equalTo(CloseExecutionMode.CloseAll));
        }

        @Test
        public void closeBatchModeIsCorrect() {
            assertThat(closePositionParams.closeBatchMode(), equalTo(BatchMode.MERGE));
        }

        @Test
        public void typeIsCLOSEPOSITION() {
            assertThat(closePositionParams.type(), equalTo(TaskParamsType.CLOSEPOSITION));
        }

        @Test
        public void noConsumersForEvents() {
            assertTrue(closePositionParams
                .composeData()
                .consumerByEventType()
                .isEmpty());
        }
    }

    public class FullConfigureTests {

        @Before
        public void setUp() {
            closePositionParams = ClosePositionParams
                .newBuilder(mergePositionParamsMock, closeParamsFactoryMock)
                .withCloseExecutionMode(CloseExecutionMode.CloseFilled)
                .withCloseBatchMode(BatchMode.CONCAT)
                .doOnStart(actionMock)
                .doOnComplete(actionMock)
                .doOnError(errorConsumerMock)
                .retryOnReject(retryParams)
                .build();
        }

        @Test
        public void closeExecutionModeIsCorrect() {
            assertThat(closePositionParams.closeExecutionMode(), equalTo(CloseExecutionMode.CloseFilled));
        }

        @Test
        public void closeBatchModeIsCorrect() {
            assertThat(closePositionParams.closeBatchMode(), equalTo(BatchMode.CONCAT));
        }

        @Test
        public void assertComposeDataAreCorrect() {
            assertComposeData(closePositionParams.composeData());
        }
    }
}
