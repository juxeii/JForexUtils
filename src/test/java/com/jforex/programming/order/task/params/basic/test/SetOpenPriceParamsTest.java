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
import com.jforex.programming.order.task.params.basic.SetOpenPriceParams;
import com.jforex.programming.order.task.params.test.CommonParamsForTest;

public class SetOpenPriceParamsTest extends CommonParamsForTest {

    private SetOpenPriceParams setOpenPriceParams;

    @Mock
    public Consumer<OrderEvent> changedOpenPriceConsumerMock;
    @Mock
    public Consumer<OrderEvent> changeRejectConsumerMock;
    private static final double newOpenPrice = 1.12345;

    @Before
    public void setUp() {
        setOpenPriceParams = SetOpenPriceParams
            .setOpenPriceWith(buyOrderEURUSD, newOpenPrice)
            .doOnChangedOpenPrice(changedOpenPriceConsumerMock)
            .doOnReject(changeRejectConsumerMock)
            .build();

        consumerForEvent = setOpenPriceParams
            .composeData()
            .consumerByEventType();
    }

    @Test
    public void handlersAreCorrect() {
        assertThat(setOpenPriceParams.type(), equalTo(TaskParamsType.SETOPENPRICE));
        assertThat(setOpenPriceParams.order(), equalTo(buyOrderEURUSD));
        assertThat(setOpenPriceParams.newOpenPrice(), equalTo(newOpenPrice));

        assertThat(consumerForEvent.size(), equalTo(2));
        assertEventConsumer(OrderEventType.CHANGED_PRICE, changedOpenPriceConsumerMock);
        assertEventConsumer(OrderEventType.CHANGE_PRICE_REJECTED, changeRejectConsumerMock);
    }
}
