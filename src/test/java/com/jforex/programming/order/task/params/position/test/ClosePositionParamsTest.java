package com.jforex.programming.order.task.params.position.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.task.BatchMode;
import com.jforex.programming.order.task.CloseExecutionMode;
import com.jforex.programming.order.task.params.ComposeData;
import com.jforex.programming.order.task.params.RetryParams;
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
    private Action actionMock;
    @Mock
    private Function<IOrder, Action> actionConsumerMock;
    @Mock
    private Consumer<Throwable> errorConsumerMock;
    @Mock
    private BiConsumer<Throwable, IOrder> biErrorConsumerMock;
    @Mock
    private Consumer<OrderEvent> eventConsumerMock;
    @Mock
    private Function<IOrder, CloseParams> closeParamsFactoryMock;
    private final IOrder orderForTest = buyOrderEURUSD;
    private static final String mergeOrderLabel = "mergeOrderLabel";
    private static final int noOfRetries = retryParams.noOfRetries();
    private CloseParams closeParams;

    @Before
    public void setUp() {
        when(actionConsumerMock.apply(orderForTest)).thenReturn(actionMock);

        when(mergePositionParamsMock.instrument()).thenReturn(instrumentEURUSD);
        when(mergePositionParamsMock.mergeOrderLabel()).thenReturn(mergeOrderLabel);

        closeParams = CloseParams
            .withOrder(orderForTest)
            .retryOnReject(retryParams)
            .doOnClose(eventConsumerMock)
            .doOnPartialClose(eventConsumerMock)
            .doOnReject(eventConsumerMock)
            .build();
        when(closeParamsFactoryMock.apply(orderForTest)).thenReturn(closeParams);

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

    private void assertActions(final ComposeData composeData) {
        assertThat(composeData.startAction(), equalTo(actionMock));
        assertThat(composeData.completeAction(), equalTo(actionMock));
    }

    private void assertErrorConsumer(final Consumer<Throwable> errorConsumer) {
        assertThat(errorConsumer, equalTo(errorConsumerMock));
    }

    private void assertRetries(final RetryParams retryParams) {
        assertThat(retryParams.noOfRetries(), equalTo(noOfRetries));
    }

    @Test
    public void defaultValuesAreCorrect() {
        closePositionParams = ClosePositionParams
            .newBuilder(mergePositionParamsMock, closeParamsFactoryMock)
            .build();

        assertThat(closePositionParams.type(), equalTo(TaskParamsType.CLOSEPOSITION));
        assertThat(closePositionParams.instrument(), equalTo(instrumentEURUSD));
        assertThat(closePositionParams.mergeOrderLabel(), equalTo(mergeOrderLabel));
        assertThat(closePositionParams.closeExecutionMode(), equalTo(CloseExecutionMode.CloseAll));
        assertThat(closePositionParams.closeBatchMode(), equalTo(BatchMode.MERGE));
        assertThat(closePositionParams.mergePositionParams(), equalTo(mergePositionParamsMock));
    }

    @Test
    public void assertSpecifiedValues() {
        assertThat(closePositionParams.instrument(), equalTo(instrumentEURUSD));
        assertThat(closePositionParams.mergeOrderLabel(), equalTo(mergeOrderLabel));
        assertThat(closePositionParams.closeExecutionMode(), equalTo(CloseExecutionMode.CloseFilled));
    }

    @Test
    public void assertComposeValues() throws Exception {
        final ComposeData composeData = closePositionParams.composeData();
        assertComposeParams(composeData);
        assertTrue(closePositionParams.consumerForEvent().isEmpty());
    }
}
