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
import com.jforex.programming.order.task.CloseExecutionMode;
import com.jforex.programming.order.task.params.ComposeParams;
import com.jforex.programming.order.task.params.ComposeParamsForOrder;
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

            .doOnclosePositionStart(actionMock)
            .doOnclosePositionComplete(actionMock)
            .doOnclosePositionError(errorConsumerMock)
            .retryOnclosePositionReject(noOfRetries, delayInMillis)

            .doOnCloseStart(actionConsumerMock)
            .doOnCloseComplete(actionConsumerMock)
            .doOnCloseError(biErrorConsumerMock)
            .retryOnCloseReject(noOfRetries, delayInMillis)

            .doOnClose(eventConsumerMock)
            .doOnCloseReject(eventConsumerMock)
            .build();
    }

    private void assertComposeParams(final ComposeParams composeParams) {
        assertActions(composeParams);
        assertErrorConsumer(composeParams.errorConsumer());
        assertRetries(composeParams.retryParams());
    }

    private void assertComposeParams(final ComposeParamsForOrder composeParams) throws Exception {
        assertActions(composeParams);
        assertErrorConsumer(composeParams);
        assertRetries(composeParams.retryParams());
    }

    private void assertActions(final ComposeParams composeParams) {
        assertThat(composeParams.startAction(), equalTo(actionMock));
        assertThat(composeParams.completeAction(), equalTo(actionMock));
    }

    private void assertActions(final ComposeParamsForOrder composeParams) throws Exception {
        composeParams.startAction(orderForTest).run();
        composeParams.completeAction(orderForTest).run();

        verify(actionConsumerMock, times(2)).apply(buyOrderEURUSD);
    }

    private void assertErrorConsumer(final Consumer<Throwable> errorConsumer) {
        assertThat(errorConsumer, equalTo(errorConsumerMock));
    }

    private void assertErrorConsumer(final ComposeParamsForOrder composeParams) {
        composeParams.errorConsumer(orderForTest).accept(jfException);
        verify(biErrorConsumerMock).accept(jfException, orderForTest);
    }

    private void assertRetries(final RetryParams retryParams) {
        assertThat(retryParams.noOfRetries(), equalTo(noOfRetries));
        assertThat(retryParams.delayInMillis(), equalTo(delayInMillis));
    }

    private void assertEventHandler(final OrderEventType type) {
        assertThat(closePositionParams.consumerForEvent().get(type), equalTo(eventConsumerMock));
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
        assertComposeParams(closePositionParams.closeComposeParams());
    }

    @Test
    public void orderEventHandlersAreCorrect() {
        assertThat(closePositionParams.consumerForEvent().size(), equalTo(4));
        assertEventHandler(OrderEventType.CLOSE_OK);
        assertEventHandler(OrderEventType.CLOSE_REJECTED);
        assertEventHandler(OrderEventType.MERGE_OK);
        assertEventHandler(OrderEventType.MERGE_REJECTED);
    }
}
