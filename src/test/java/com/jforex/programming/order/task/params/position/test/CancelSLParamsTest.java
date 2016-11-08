package com.jforex.programming.order.task.params.position.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.function.Consumer;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.task.params.position.CancelSLParams;
import com.jforex.programming.order.task.params.test.CommonParamsForTest;

public class CancelSLParamsTest extends CommonParamsForTest {

    private CancelSLParams cancelSLParams;

    @Mock
    public Consumer<OrderEvent> cancelledSLConsumerMock;
    @Mock
    public Consumer<OrderEvent> cancelSLRejectConsumerMock;

    @Before
    public void setUp() {
        cancelSLParams = CancelSLParams
            .newBuilder()
            .doOnCancelSL(cancelledSLConsumerMock)
            .doOnReject(cancelSLRejectConsumerMock)
            .build();

        consumerForEvent = cancelSLParams.consumerForEvent();
    }

    @Test
    public void handlersAreCorrect() {
        assertThat(consumerForEvent.size(), equalTo(2));
        assertEventConsumer(OrderEventType.CHANGED_SL, cancelledSLConsumerMock);
        assertEventConsumer(OrderEventType.CHANGE_SL_REJECTED, cancelSLRejectConsumerMock);
    }
}
