package com.jforex.programming.order.task.params.basic.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import java.util.function.Consumer;

import org.junit.Test;
import org.mockito.Mock;

import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.task.params.TaskParamsType;
import com.jforex.programming.order.task.params.basic.CloseParams;
import com.jforex.programming.order.task.params.test.CommonParamsForTest;

public class CloseParamsTest extends CommonParamsForTest {

    private CloseParams closeParams;

    @Mock
    public Consumer<OrderEvent> closeConsumerMock;
    @Mock
    public Consumer<OrderEvent> partialCloseConsumerMock;
    @Mock
    public Consumer<OrderEvent> rejectConsumerMock;

    @Test
    public void handlersAreCorrect() {
        closeParams = CloseParams
            .withOrder(buyOrderEURUSD)
            .doOnClose(closeConsumerMock)
            .doOnPartialClose(partialCloseConsumerMock)
            .doOnReject(rejectConsumerMock)
            .build();

        consumerForEvent = closeParams
            .composeData()
            .consumerByEventType();

        assertThat(consumerForEvent.size(), equalTo(3));
        assertEventConsumer(OrderEventType.CLOSE_OK, closeConsumerMock);
        assertEventConsumer(OrderEventType.PARTIAL_CLOSE_OK, partialCloseConsumerMock);
        assertEventConsumer(OrderEventType.CLOSE_REJECTED, rejectConsumerMock);
    }

    @Test
    public void defaultValuesAreCorrect() {
        closeParams = CloseParams
            .withOrder(buyOrderEURUSD)
            .build();

        assertThat(closeParams.type(), equalTo(TaskParamsType.CLOSE));
        assertThat(closeParams.order(), equalTo(buyOrderEURUSD));
        assertThat(closeParams.partialCloseAmount(), equalTo(0.0));
        assertFalse(closeParams.maybePrice().isPresent());
        assertThat(closeParams.slippage(), equalTo(Double.NaN));
    }

    @Test
    public void partialCloseAmountIsCorrect() {
        final double partialCloseAmount = 0.13;

        closeParams = CloseParams
            .withOrder(buyOrderEURUSD)
            .closePartial(partialCloseAmount)
            .build();

        assertThat(closeParams.partialCloseAmount(), equalTo(partialCloseAmount));
    }

    @Test
    public void definedPriceWithNegativeSlippageGivesDefaultSlippage() {
        final double price = 1.1234;

        closeParams = CloseParams
            .withOrder(buyOrderEURUSD)
            .atPrice(price, -1.0)
            .build();

        assertThat(closeParams.maybePrice().get(), equalTo(price));
        assertThat(closeParams.slippage(), equalTo(platformSettings.defaultCloseSlippage()));
    }

    @Test
    public void definedPriceWithPositiveSlippageGivesSlippage() {
        final double slippage = 3.2;

        closeParams = CloseParams
            .withOrder(buyOrderEURUSD)
            .atPrice(1.1234, slippage)
            .build();

        assertThat(closeParams.slippage(), equalTo(slippage));
    }
}
