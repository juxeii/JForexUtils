package com.jforex.programming.order.task.params.position.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.function.Consumer;

import org.junit.Test;
import org.mockito.Mock;

import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.task.params.position.SimpleMergePositionParams;
import com.jforex.programming.order.task.params.test.CommonParamsForTest;

public class SimpleMergePositionParamsTest extends CommonParamsForTest {

    private SimpleMergePositionParams simpleMergePositionParams;

    @Mock
    private Consumer<OrderEvent> mergeConsumerMock;
    @Mock
    private Consumer<OrderEvent> mergeCloseConsumerMock;
    @Mock
    private Consumer<OrderEvent> rejectConsumerMock;
    private static final String mergeOrderLabel = " mergeOrderLabel";

    @Test
    public void valuesAreCorrect() {
        simpleMergePositionParams = SimpleMergePositionParams
            .mergeWithLabel(mergeOrderLabel)
            .doOnMerge(mergeConsumerMock)
            .doOnMergeClose(mergeCloseConsumerMock)
            .doOnReject(rejectConsumerMock)
            .build();

        consumerForEvent = simpleMergePositionParams.consumerForEvent();

        assertThat(simpleMergePositionParams.mergeOrderLabel(), equalTo(mergeOrderLabel));
        assertThat(consumerForEvent.size(), equalTo(3));
        assertEventConsumer(OrderEventType.MERGE_OK, mergeConsumerMock);
        assertEventConsumer(OrderEventType.MERGE_CLOSE_OK, mergeCloseConsumerMock);
        assertEventConsumer(OrderEventType.MERGE_REJECTED, rejectConsumerMock);
    }
}
