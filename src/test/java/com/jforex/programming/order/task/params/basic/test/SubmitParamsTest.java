package com.jforex.programming.order.task.params.basic.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.function.Consumer;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.task.params.TaskParamsType;
import com.jforex.programming.order.task.params.basic.SubmitParams;
import com.jforex.programming.order.task.params.test.CommonParamsForTest;

public class SubmitParamsTest extends CommonParamsForTest {

    private SubmitParams submitParams;

    @Mock
    public Consumer<OrderEvent> submitConsumerMock;
    @Mock
    public Consumer<OrderEvent> partialFillConsumerMock;
    @Mock
    public Consumer<OrderEvent> fullFillConsumerMock;
    @Mock
    public Consumer<OrderEvent> submitRejectConsumerMock;
    @Mock
    public Consumer<OrderEvent> fillRejectConsumerMock;

    @Before
    public void setUp() {
        submitParams = SubmitParams
            .withOrderParams(buyParamsEURUSD)
            .doOnSubmit(submitConsumerMock)
            .doOnPartialFill(partialFillConsumerMock)
            .doOnFullFill(fullFillConsumerMock)
            .doOnSubmitReject(submitRejectConsumerMock)
            .doOnFillReject(fillRejectConsumerMock)
            .build();

        consumerForEvent = submitParams
            .composeData()
            .consumerByEventType();
    }

    @Test
    public void assertBuilderValues() {
        assertThat(submitParams.orderParams(), equalTo(buyParamsEURUSD));

        assertThat(submitParams.type(), equalTo(TaskParamsType.SUBMIT));
        assertThat(consumerForEvent.size(), equalTo(5));
        assertEventConsumer(OrderEventType.SUBMIT_OK, submitConsumerMock);
        assertEventConsumer(OrderEventType.PARTIAL_FILL_OK, partialFillConsumerMock);
        assertEventConsumer(OrderEventType.FULLY_FILLED, fullFillConsumerMock);
        assertEventConsumer(OrderEventType.SUBMIT_REJECTED, submitRejectConsumerMock);
        assertEventConsumer(OrderEventType.FILL_REJECTED, fillRejectConsumerMock);
    }
}
