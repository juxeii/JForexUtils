package com.jforex.programming.order.task.params.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Map;
import java.util.function.Consumer;

import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.test.common.InstrumentUtilForTest;

public class CommonParamsForTest extends InstrumentUtilForTest {

    protected Map<OrderEventType, Consumer<OrderEvent>> consumerForEvent;

    protected void assertEventConsumer(final OrderEventType type,
                                       final Consumer<OrderEvent> consumer) {
        assertThat(consumerForEvent.get(type), equalTo(consumer));
    }
}
