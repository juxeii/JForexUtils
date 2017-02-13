package com.jforex.programming.order.task.params.basic.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.function.Consumer;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.task.params.basic.CancelSLParams;
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
            .withOrder(buyOrderEURUSD)
            .doOnCancelSL(cancelledSLConsumerMock)
            .doOnReject(cancelSLRejectConsumerMock)
            .build();

        consumerForEvent = cancelSLParams
            .composeData()
            .consumerByEventType();
    }

    @Test
    public void valuesAreCorrect() {
        assertThat(cancelSLParams.order(), equalTo(buyOrderEURUSD));
        assertThat(consumerForEvent.size(), equalTo(2));
        assertEventConsumer(OrderEventType.CHANGED_SL, cancelledSLConsumerMock);
        assertEventConsumer(OrderEventType.CHANGE_SL_REJECTED, cancelSLRejectConsumerMock);
    }
}
