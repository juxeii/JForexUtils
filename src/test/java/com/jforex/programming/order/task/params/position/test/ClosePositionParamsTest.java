package com.jforex.programming.order.task.params.position.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.task.BatchMode;
import com.jforex.programming.order.task.CloseExecutionMode;
import com.jforex.programming.order.task.params.ComposeData;
import com.jforex.programming.order.task.params.RetryParams;
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
    private final Map<OrderEventType, Consumer<OrderEvent>> consumersForMergeParams = new HashMap<>();
    private final IOrder orderForTest = buyOrderEURUSD;
    private static final String mergeOrderLabel = "mergeOrderLabel";
    private static final int noOfRetries = 3;
    private static final long delayInMillis = 1500L;

    @Before
    public void setUp() {
        consumersForMergeParams.put(OrderEventType.MERGE_OK, eventConsumerMock);
        consumersForMergeParams.put(OrderEventType.MERGE_REJECTED, eventConsumerMock);

        when(actionConsumerMock.apply(orderForTest)).thenReturn(actionMock);
        when(mergePositionParamsMock.consumerForEvent()).thenReturn(consumersForMergeParams);

        closePositionParams = ClosePositionParams
            .newBuilder(instrumentEURUSD, mergeOrderLabel)

            .withCloseExecutionMode(CloseExecutionMode.CloseFilled)
            .withMergePositionParams(mergePositionParamsMock)

            .doOnClosePositionStart(actionMock)
            .doOnClosePositionComplete(actionMock)
            .doOnClosePositionError(errorConsumerMock)
            .retryOnClosePositionReject(noOfRetries, delayInMillis)

            .doOnCloseStart(actionConsumerMock)
            .doOnCloseComplete(actionConsumerMock)
            .doOnCloseError(biErrorConsumerMock)
            .retryOnCloseReject(noOfRetries, delayInMillis)

            .doOnClose(eventConsumerMock)
            .doOnPartialClose(eventConsumerMock)
            .doOnCloseReject(eventConsumerMock)
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
        assertThat(retryParams.delayInMillis(), equalTo(delayInMillis));
    }

    private void assertEventHandler(final OrderEventType type) {
        assertThat(closePositionParams.consumerForEvent().get(type), equalTo(eventConsumerMock));
    }

    private void assertComposeDataWithOrder(final ComposeData composeData) throws Exception {
        composeData.startAction().run();
        verify(actionMock).run();

        composeData.completeAction().run();
        verify(actionMock, times(2)).run();

        composeData.errorConsumer().accept(jfException);
        verify(biErrorConsumerMock).accept(jfException, orderForTest);
    }

    @Test
    public void defaultValuesAreCorrect() {
        closePositionParams = ClosePositionParams
            .newBuilder(instrumentEURUSD, mergeOrderLabel)
            .withMergePositionParams(mergePositionParamsMock)
            .build();

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
    public void assertClosePositionValues() {
        assertComposeParams(closePositionParams.closePositionComposeParams());
    }

    @Test
    public void assertCloseValues() throws Exception {
        final ComposeData composeData = closePositionParams.closeComposeParams(orderForTest);
        assertComposeDataWithOrder(composeData);
    }

    @Test
    public void orderEventHandlersAreCorrect() {
        assertThat(closePositionParams.consumerForEvent().size(), equalTo(5));
        assertEventHandler(OrderEventType.CLOSE_OK);
        assertEventHandler(OrderEventType.PARTIAL_CLOSE_OK);
        assertEventHandler(OrderEventType.CLOSE_REJECTED);
        assertEventHandler(OrderEventType.MERGE_OK);
        assertEventHandler(OrderEventType.MERGE_REJECTED);
    }
}
