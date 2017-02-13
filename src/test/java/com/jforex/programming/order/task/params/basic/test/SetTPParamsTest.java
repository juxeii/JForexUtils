package com.jforex.programming.order.task.params.basic.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.function.Consumer;

import org.junit.Test;
import org.mockito.Mock;

import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.task.params.SetSLTPMode;
import com.jforex.programming.order.task.params.TaskParamsType;
import com.jforex.programming.order.task.params.basic.SetTPParams;
import com.jforex.programming.order.task.params.test.CommonParamsForTest;

public class SetTPParamsTest extends CommonParamsForTest {

    private SetTPParams setTPParams;

    @Mock
    public Consumer<OrderEvent> changedTPConsumerMock;
    @Mock
    public Consumer<OrderEvent> changeRejectConsumerMock;

    @Test
    public void valuesWithPriceAreCorrect() {
        final double newTP = 1.1234;

        setTPParams = SetTPParams
            .setTPAtPrice(buyOrderEURUSD, newTP)
            .build();

        assertThat(setTPParams.order(), equalTo(buyOrderEURUSD));
        assertThat(setTPParams.priceOrPips(), equalTo(newTP));
        assertThat(setTPParams.setSLTPMode(), equalTo(SetSLTPMode.PRICE));
    }

    @Test
    public void valuesWithPipsAreCorrect() {
        final double pipsToTP = 12.3;

        setTPParams = SetTPParams
            .setTPWithPips(buyOrderEURUSD, pipsToTP)
            .build();

        assertThat(setTPParams.order(), equalTo(buyOrderEURUSD));
        assertThat(setTPParams.priceOrPips(), equalTo(pipsToTP));
        assertThat(setTPParams.setSLTPMode(), equalTo(SetSLTPMode.PIPS));
    }

    @Test
    public void handlersAreCorrect() {
        setTPParams = SetTPParams
            .setTPAtPrice(buyOrderEURUSD, 1.1234)
            .doOnChangedTP(changedTPConsumerMock)
            .doOnReject(changeRejectConsumerMock)
            .build();

        consumerForEvent = setTPParams
            .composeData()
            .consumerByEventType();

        assertThat(setTPParams.type(), equalTo(TaskParamsType.SETTP));
        assertThat(consumerForEvent.size(), equalTo(2));
        assertEventConsumer(OrderEventType.CHANGED_TP, changedTPConsumerMock);
        assertEventConsumer(OrderEventType.CHANGE_TP_REJECTED, changeRejectConsumerMock);
    }
}
