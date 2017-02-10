package com.jforex.programming.order.task.params.position.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.task.BatchMode;
import com.jforex.programming.order.task.CloseExecutionMode;
import com.jforex.programming.order.task.params.ComposeData;
import com.jforex.programming.order.task.params.TaskParamsType;
import com.jforex.programming.order.task.params.basic.CloseParams;
import com.jforex.programming.order.task.params.position.ClosePositionParams;
import com.jforex.programming.order.task.params.position.MergePositionParams;
import com.jforex.programming.order.task.params.test.CommonParamsForTest;

import io.reactivex.functions.Action;

public class ClosePositionParamsTest extends CommonParamsForTest {

    private ClosePositionParams closePositionParams;

    @Mock
    private MergePositionParams mergePositionParamsMock;
    @Mock
    private Function<IOrder, Action> actionConsumerMock;
    @Mock
    private CloseParams closeParamsMock;
    @Mock
    private Function<IOrder, CloseParams> closeParamsFactoryMock;

    @Before
    public void setUp() {
        when(actionConsumerMock.apply(orderForTest)).thenReturn(actionMock);
        when(mergePositionParamsMock.instrument()).thenReturn(instrumentEURUSD);
        when(mergePositionParamsMock.mergeOrderLabel()).thenReturn(mergeOrderLabel);
        when(closeParamsFactoryMock.apply(orderForTest)).thenReturn(closeParamsMock);

        closePositionParams = ClosePositionParams
            .newBuilder(mergePositionParamsMock, closeParamsFactoryMock)
            .withCloseExecutionMode(CloseExecutionMode.CloseFilled)
            .doOnStart(actionMock)
            .doOnComplete(actionMock)
            .doOnError(errorConsumerMock)
            .retryOnReject(retryParams)
            .build();
    }

    private void assertComposeParams(final ComposeData composeData) {
        assertActions(composeData);
        assertErrorConsumer(composeData.errorConsumer());
        assertRetries(composeData.retryParams());
    }

    @Test
    public void defaultValuesAreCorrect() {
        closePositionParams = ClosePositionParams
            .newBuilder(mergePositionParamsMock, closeParamsFactoryMock)
            .build();

        assertThat(closePositionParams.type(), equalTo(TaskParamsType.CLOSEPOSITION));
        assertThat(closePositionParams.instrument(), equalTo(instrumentEURUSD));
        assertThat(closePositionParams.closeExecutionMode(), equalTo(CloseExecutionMode.CloseAll));
        assertThat(closePositionParams.closeBatchMode(), equalTo(BatchMode.MERGE));
        assertThat(closePositionParams.mergePositionParams(), equalTo(mergePositionParamsMock));
    }

    @Test
    public void assertSpecifiedValues() {
        assertThat(closePositionParams.instrument(), equalTo(instrumentEURUSD));
        assertThat(closePositionParams.closeExecutionMode(), equalTo(CloseExecutionMode.CloseFilled));
    }

    @Test
    public void assertComposeValues() throws Exception {
        final ComposeData composeData = closePositionParams.composeData();
        assertComposeParams(composeData);
        assertTrue(closePositionParams.consumerForEvent().isEmpty());
    }

    @Test
    public void closeParamsFactoryIsCalled() {
        final CloseParams returnedCloseParams = closePositionParams.createCloseParams(orderForTest);
        assertThat(returnedCloseParams, equalTo(closeParamsMock));
    }
}
