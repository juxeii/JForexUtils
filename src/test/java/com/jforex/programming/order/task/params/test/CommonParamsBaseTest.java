package com.jforex.programming.order.task.params.test;

import static org.hamcrest.Matchers.equalTo;
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
    private static final int noOfRetries = 3;
    private static final long delayInMillis = 1500L;

    @Test
    public void commonParamsAreCorrect() {
        closeParams = CloseParams
            .closeOrder(buyOrderEURUSD)
            .doOnClose(consumer)
            .retryOnReject(noOfRetries, delayInMillis)
            .build();

        assertThat(closeParams.consumerForEvent().size(), equalTo(1));
        assertThat(closeParams.consumerForEvent().get(OrderEventType.CLOSE_OK), equalTo(consumer));
        assertThat(closeParams.noOfRetries(), equalTo(noOfRetries));
        assertThat(closeParams.delayInMillis(), equalTo(delayInMillis));
    }
}
