package com.jforex.programming.order.task.params.position.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.function.Consumer;

import org.junit.Test;
import org.mockito.Mock;

import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.task.params.position.SimpleClosePositionParams;
import com.jforex.programming.order.task.params.test.CommonParamsForTest;

public class SimpleClosePositionParamsTest extends CommonParamsForTest {

    private SimpleClosePositionParams simpleClosePositionParams;

    @Mock
    private Consumer<OrderEvent> closeConsumerMock;
    @Mock
    private Consumer<OrderEvent> partialCloseConsumerMock;
    @Mock
    private Consumer<OrderEvent> rejectConsumerMock;

    @Test
    public void handlersAreCorrect() {
        simpleClosePositionParams = SimpleClosePositionParams
            .newBuilder()
            .doOnClose(closeConsumerMock)
            .doOnPartialClose(partialCloseConsumerMock)
            .doOnReject(rejectConsumerMock)
            .build();

        consumerForEvent = simpleClosePositionParams.consumerForEvent();

        assertThat(consumerForEvent.size(), equalTo(3));
        assertEventConsumer(OrderEventType.CLOSE_OK, closeConsumerMock);
        assertEventConsumer(OrderEventType.PARTIAL_CLOSE_OK, partialCloseConsumerMock);
        assertEventConsumer(OrderEventType.CLOSE_REJECTED, rejectConsumerMock);
    }
}
