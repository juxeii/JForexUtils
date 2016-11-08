package com.jforex.programming.order.task.params.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.function.Consumer;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.task.params.basic.SetAmountParams;

public class SetAmountParamsTest extends CommonParamsForTest {

    private SetAmountParams setAmountParams;

    @Mock
    public Consumer<OrderEvent> changedAmountConsumerMock;
    @Mock
    public Consumer<OrderEvent> changeRejectConsumerMock;
    private static final double newAmount = 0.12;

    @Before
    public void setUp() {
        setAmountParams = SetAmountParams
            .setAmountWith(buyOrderEURUSD, newAmount)
            .doOnChangedAmount(changedAmountConsumerMock)
            .doOnReject(changeRejectConsumerMock)
            .build();

        consumerForEvent = setAmountParams.consumerForEvent();
    }

    @Test
    public void handlersAreCorrect() {
        assertThat(setAmountParams.order(), equalTo(buyOrderEURUSD));
        assertThat(setAmountParams.newAmount(), equalTo(newAmount));

        assertThat(consumerForEvent.size(), equalTo(2));
        assertEventConsumer(OrderEventType.CHANGED_AMOUNT, changedAmountConsumerMock);
        assertEventConsumer(OrderEventType.CHANGE_AMOUNT_REJECTED, changeRejectConsumerMock);
    }
}
