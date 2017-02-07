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
import com.jforex.programming.order.task.params.ComposeParams;
import com.jforex.programming.order.task.params.RetryParams;
import com.jforex.programming.order.task.params.basic.CloseParams;
import com.jforex.programming.order.task.params.position.ClosePositionParams;
import com.jforex.programming.order.task.params.position.MergePositionParams;
import com.jforex.programming.order.task.params.position.PositionParams;
import com.jforex.programming.order.task.params.test.CommonParamsForTest;

import io.reactivex.functions.Action;

public class ClosePositionParamsTest extends CommonParamsForTest {

    private ClosePositionParams closePositionParams;

    @Mock
    private PositionParams closePositionComposeParamsMock;
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
    private final ComposeData composeData = new ComposeParams();
    private final Map<OrderEventType, Consumer<OrderEvent>> consumersForMergeParams = new HashMap<>();
    private final IOrder orderForTest = buyOrderEURUSD;
    private static final String mergeOrderLabel = "mergeOrderLabel";
    private static final int noOfRetries = retryParams.noOfRetries();
    private CloseParams closeParams;

    @Before
    public void setUp() {
        consumersForMergeParams.put(OrderEventType.MERGE_OK, eventConsumerMock);
        consumersForMergeParams.put(OrderEventType.MERGE_REJECTED, eventConsumerMock);

        when(actionConsumerMock.apply(orderForTest)).thenReturn(actionMock);

        when(mergePositionParamsMock.instrument()).thenReturn(instrumentEURUSD);
        when(mergePositionParamsMock.mergeOrderLabel()).thenReturn(mergeOrderLabel);
        when(mergePositionParamsMock.consumerForEvent()).thenReturn(consumersForMergeParams);
        when(closePositionComposeParamsMock.composeData()).thenReturn(composeData);

        closeParams = CloseParams
            .withOrder(orderForTest)
            .doOnStart(actionMock)
            .doOnComplete(actionMock)
            .doOnError(errorConsumerMock)
            .retryOnReject(retryParams)
            .doOnClose(eventConsumerMock)
            .doOnPartialClose(eventConsumerMock)
            .doOnReject(eventConsumerMock)
            .build();
        when(closeParamsFactoryMock.apply(orderForTest)).thenReturn(closeParams);

        closePositionParams = ClosePositionParams
            .newBuilder(mergePositionParamsMock, closeParamsFactoryMock)
            .withCloseExecutionMode(CloseExecutionMode.CloseFilled)
            .withClosePositonParams(closePositionComposeParamsMock)
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
        // assertThat(retryParams.delayFunction().apply(0).delay(),
        // equalTo(delayInMillis));
    }

    private void assertEventHandler(final OrderEventType type) {
        assertThat(closePositionParams.consumerForEvent().get(type), equalTo(eventConsumerMock));
    }

    @Test
    public void defaultValuesAreCorrect() {
        closePositionParams = ClosePositionParams
            .newBuilder(mergePositionParamsMock, closeParamsFactoryMock)
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
        assertThat(closePositionParams.composeData(), equalTo(composeData));
    }

    @Test
    public void assertCloseValues() throws Exception {
        final CloseParams returnedCloseParams = closePositionParams.closeParamsFactory(orderForTest);
        final ComposeData composeData = returnedCloseParams.composeData();
        assertComposeParams(composeData);

        final Map<OrderEventType, Consumer<OrderEvent>> consumerForEvent = returnedCloseParams.consumerForEvent();
        assertThat(consumerForEvent.size(), equalTo(3));
        assertThat(consumerForEvent.get(OrderEventType.CLOSE_OK),
                   equalTo(eventConsumerMock));
        assertThat(consumerForEvent.get(OrderEventType.PARTIAL_CLOSE_OK),
                   equalTo(eventConsumerMock));
        assertThat(consumerForEvent.get(OrderEventType.CLOSE_REJECTED),
                   equalTo(eventConsumerMock));
    }

    @Test
    public void orderEventHandlersAreCorrect() {
        assertThat(closePositionParams.consumerForEvent().size(), equalTo(2));
        assertEventHandler(OrderEventType.MERGE_OK);
        assertEventHandler(OrderEventType.MERGE_REJECTED);
    }
}
