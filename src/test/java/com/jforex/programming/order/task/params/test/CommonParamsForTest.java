package com.jforex.programming.order.task.params.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.task.params.ComposeData;
import com.jforex.programming.order.task.params.RetryParams;
import com.jforex.programming.test.common.InstrumentUtilForTest;

import io.reactivex.functions.Action;

public class CommonParamsForTest extends InstrumentUtilForTest {

    @Mock
    protected Action actionMock;
    @Mock
    protected Consumer<Throwable> errorConsumerMock;
    @Mock
    protected Consumer<OrderEvent> eventConsumerMock;
    protected final IOrder orderForTest = buyOrderEURUSD;
    protected Map<OrderEventType, Consumer<OrderEvent>> consumerForEvent = new HashMap<>();

    protected static final int noOfRetries = retryParams.noOfRetries();
    protected static final String mergeOrderLabel = "mergeOrderLabel";

    protected void assertEventConsumer(final OrderEventType type,
                                       final Consumer<OrderEvent> consumer) {
        assertThat(consumerForEvent.get(type), equalTo(consumer));
    }

    protected void assertComposeData(final ComposeData composeData) {
        assertActions(composeData);
        assertErrorConsumer(composeData.errorConsumer());
        assertRetries(composeData.retryParams());
    }

    protected void assertActions(final ComposeData composeData) {
        assertThat(composeData.startAction(), equalTo(actionMock));
        assertThat(composeData.completeAction(), equalTo(actionMock));
    }

    protected void assertErrorConsumer(final Consumer<Throwable> errorConsumer) {
        assertThat(errorConsumer, equalTo(errorConsumerMock));
    }

    protected void assertRetries(final RetryParams retryParams) {
        assertThat(retryParams.noOfRetries(), equalTo(noOfRetries));
    }
}
