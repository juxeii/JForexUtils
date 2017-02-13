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
import com.jforex.programming.order.task.params.basic.SetLabelParams;
import com.jforex.programming.order.task.params.test.CommonParamsForTest;

public class SetLabelParamsTest extends CommonParamsForTest {

    private SetLabelParams setLabelParams;

    @Mock
    public Consumer<OrderEvent> changedLabelConsumerMock;
    @Mock
    public Consumer<OrderEvent> changeRejectConsumerMock;
    private static final String newLabel = "newLabel";

    @Before
    public void setUp() {
        setLabelParams = SetLabelParams
            .setLabelWith(buyOrderEURUSD, newLabel)
            .doOnChangedLabel(changedLabelConsumerMock)
            .doOnReject(changeRejectConsumerMock)
            .build();

        consumerForEvent = setLabelParams
            .composeData()
            .consumerByEventType();
    }

    @Test
    public void handlersAreCorrect() {
        assertThat(setLabelParams.type(), equalTo(TaskParamsType.SETLABEL));
        assertThat(setLabelParams.order(), equalTo(buyOrderEURUSD));
        assertThat(setLabelParams.newLabel(), equalTo(newLabel));

        assertThat(consumerForEvent.size(), equalTo(2));
        assertEventConsumer(OrderEventType.CHANGED_LABEL, changedLabelConsumerMock);
        assertEventConsumer(OrderEventType.CHANGE_LABEL_REJECTED, changeRejectConsumerMock);
    }
}
