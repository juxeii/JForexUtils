package com.jforex.programming.order.task.params.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.util.function.Consumer;

import org.junit.Test;
import org.mockito.Mock;

import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.task.params.basic.CloseParams;
import com.jforex.programming.test.common.InstrumentUtilForTest;

public class CommonParamsBaseTest extends InstrumentUtilForTest {

    private CloseParams closeParams;

    @Mock
    private Consumer<OrderEvent> consumer;

    @Test
    public void commonParamsAreCorrect() {
        closeParams = CloseParams
            .withOrder(buyOrderEURUSD)
            .doOnClose(consumer)
            .retryOnReject(retryParams)
            .build();

        assertThat(closeParams
            .composeData()
            .consumerByEventType()
            .size(), equalTo(1));
        assertThat(closeParams
            .composeData()
            .consumerByEventType()
            .get(OrderEventType.CLOSE_OK), equalTo(consumer));
        assertNotNull(closeParams.composeData());
    }
}
