package com.jforex.programming.order.task.params.basic.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Set;
import java.util.function.Consumer;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.google.common.collect.Sets;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.task.params.TaskParamsType;
import com.jforex.programming.order.task.params.basic.MergeParams;
import com.jforex.programming.order.task.params.test.CommonParamsForTest;

public class MergeParamsTest extends CommonParamsForTest {

    private MergeParams mergeParams;

    @Mock
    public Consumer<OrderEvent> mergeConsumerMock;
    @Mock
    public Consumer<OrderEvent> mergeCloseConsumerMock;
    @Mock
    public Consumer<OrderEvent> rejectConsumerMock;
    private final Set<IOrder> toMergeOrders = Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD);

    @Before
    public void setUp() {
        mergeParams = MergeParams
            .mergeWith(mergeOrderLabel, toMergeOrders)
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
    public void mergeOrderLabelIsCorrect() {
        assertThat(mergeParams.mergeOrderLabel(), equalTo(mergeOrderLabel));
    }

    @Test
    public void toMergeOrdersAreCorrect() {
        assertThat(mergeParams.toMergeOrders(), equalTo(toMergeOrders));
    }

    @Test
    public void typeIsMERGE() {
        assertThat(mergeParams.type(), equalTo(TaskParamsType.MERGE));
    }

    @Test
    public void handlersAreCorrect() {
        consumerForEvent = mergeParams
            .composeData()
            .consumerByEventType();

        assertThat(consumerForEvent.size(), equalTo(3));
        assertEventConsumer(OrderEventType.MERGE_OK, mergeConsumerMock);
        assertEventConsumer(OrderEventType.MERGE_CLOSE_OK, mergeCloseConsumerMock);
        assertEventConsumer(OrderEventType.MERGE_REJECTED, rejectConsumerMock);
    }
}
