package com.jforex.programming.order.task.params.basic.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.function.Consumer;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.task.params.basic.CancelTPParams;
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
            .withOrder(buyOrderEURUSD)
            .doOnCancelTP(cancelledTPConsumerMock)
            .doOnReject(cancelTPRejectConsumerMock)
            .build();

        consumerForEvent = cancelTPParams
            .composeData()
            .consumerByEventType();
    }

    @Test
    public void handlersAreCorrect() {
        assertThat(cancelTPParams.order(), equalTo(buyOrderEURUSD));
        assertThat(consumerForEvent.size(), equalTo(2));
        assertEventConsumer(OrderEventType.CHANGED_TP, cancelledTPConsumerMock);
        assertEventConsumer(OrderEventType.CHANGE_TP_REJECTED, cancelTPRejectConsumerMock);
    }
}
