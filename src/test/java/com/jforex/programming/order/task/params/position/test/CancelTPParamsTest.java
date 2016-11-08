package com.jforex.programming.order.task.params.position.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.function.Consumer;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.task.params.position.CancelTPParams;
import com.jforex.programming.order.task.params.test.CommonParamsForTest;

public class CancelTPParamsTest extends CommonParamsForTest {

    private CancelTPParams cancelTPParams;

    @Mock
    public Consumer<OrderEvent> cancelledTPConsumerMock;
    @Mock
    public Consumer<OrderEvent> cancelTPRejectConsumerMock;

    @Before
    public void setUp() {
        cancelTPParams = CancelTPParams
            .newBuilder()
            .doOnCancelTP(cancelledTPConsumerMock)
            .doOnReject(cancelTPRejectConsumerMock)
            .build();

        consumerForEvent = cancelTPParams.consumerForEvent();
    }

    @Test
    public void handlersAreCorrect() {
        assertThat(consumerForEvent.size(), equalTo(2));
        assertEventConsumer(OrderEventType.CHANGED_TP, cancelledTPConsumerMock);
        assertEventConsumer(OrderEventType.CHANGE_TP_REJECTED, cancelTPRejectConsumerMock);
    }
}
