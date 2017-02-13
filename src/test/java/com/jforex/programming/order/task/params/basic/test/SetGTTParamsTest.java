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
import com.jforex.programming.order.task.params.basic.SetGTTParams;
import com.jforex.programming.order.task.params.test.CommonParamsForTest;

public class SetGTTParamsTest extends CommonParamsForTest {

    private SetGTTParams setGTTParams;

    @Mock
    public Consumer<OrderEvent> changedGTTConsumerMock;
    @Mock
    public Consumer<OrderEvent> changeRejectConsumerMock;
    private static final long newGTT = 1L;

    @Before
    public void setUp() {
        setGTTParams = SetGTTParams
            .setGTTWith(buyOrderEURUSD, newGTT)
            .doOnChangedGTT(changedGTTConsumerMock)
            .doOnReject(changeRejectConsumerMock)
            .build();

        consumerForEvent = setGTTParams
            .composeData()
            .consumerByEventType();
    }

    @Test
    public void handlersAreCorrect() {
        assertThat(setGTTParams.type(), equalTo(TaskParamsType.SETGTT));
        assertThat(setGTTParams.order(), equalTo(buyOrderEURUSD));
        assertThat(setGTTParams.newGTT(), equalTo(newGTT));

        assertThat(consumerForEvent.size(), equalTo(2));
        assertEventConsumer(OrderEventType.CHANGED_GTT, changedGTTConsumerMock);
        assertEventConsumer(OrderEventType.CHANGE_GTT_REJECTED, changeRejectConsumerMock);
    }
}
