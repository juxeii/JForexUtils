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
import com.jforex.programming.order.task.params.basic.MergeParamsForPosition;
import com.jforex.programming.order.task.params.test.CommonParamsForTest;

public class MergeParamsForPositionTest extends CommonParamsForTest {

    private MergeParamsForPosition mergeParamsForPosition;

    @Mock
    public Consumer<OrderEvent> mergeConsumerMock;
    @Mock
    public Consumer<OrderEvent> mergeCloseConsumerMock;
    @Mock
    public Consumer<OrderEvent> rejectConsumerMock;

    @Before
    public void setUp() {
        mergeParamsForPosition = MergeParamsForPosition
            .newBuilder()
            .doOnStart(actionMock)
            .doOnComplete(actionMock)
            .doOnError(errorConsumerMock)
            .retryOnReject(retryParams)
            .doOnMerge(mergeConsumerMock)
            .doOnMergeClose(mergeCloseConsumerMock)
            .doOnReject(rejectConsumerMock)
            .build();
    }

    @Test
    public void typeIsMERGE() {
        assertThat(mergeParamsForPosition.type(), equalTo(TaskParamsType.MERGE));
    }

    @Test
    public void handlersAreCorrect() {
        consumerForEvent = mergeParamsForPosition
            .composeData()
            .consumerByEventType();

        assertThat(consumerForEvent.size(), equalTo(3));
        assertEventConsumer(OrderEventType.MERGE_OK, mergeConsumerMock);
        assertEventConsumer(OrderEventType.MERGE_CLOSE_OK, mergeCloseConsumerMock);
        assertEventConsumer(OrderEventType.MERGE_REJECTED, rejectConsumerMock);
    }

    @Test
    public void assertComposeDataAreCorrect() {
        assertComposeData(mergeParamsForPosition.composeData());
    }
}
