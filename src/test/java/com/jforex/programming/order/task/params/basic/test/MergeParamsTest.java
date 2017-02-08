package com.jforex.programming.order.task.params.basic.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Set;
import java.util.function.Consumer;

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
    private final String mergeOrderLabel = "mergeOrderLabel";
    private final Set<IOrder> toMergeOrders = Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD);

    @Test
    public void defaultValuesAreCorrect() {
        mergeParams = MergeParams
            .mergeWith(mergeOrderLabel, toMergeOrders)
            .build();

        assertThat(mergeParams.type(), equalTo(TaskParamsType.MERGE));
        assertThat(mergeParams.mergeOrderLabel(), equalTo(mergeOrderLabel));
        assertThat(mergeParams.toMergeOrders(), equalTo(toMergeOrders));
    }

    @Test
    public void handlersAreCorrect() {
        mergeParams = MergeParams
            .mergeWith(mergeOrderLabel, toMergeOrders)
            .doOnMerge(mergeConsumerMock)
            .doOnMergeClose(mergeCloseConsumerMock)
            .doOnReject(rejectConsumerMock)
            .build();

        consumerForEvent = mergeParams.consumerForEvent();

        assertThat(consumerForEvent.size(), equalTo(3));
        assertEventConsumer(OrderEventType.MERGE_OK, mergeConsumerMock);
        assertEventConsumer(OrderEventType.MERGE_CLOSE_OK, mergeCloseConsumerMock);
        assertEventConsumer(OrderEventType.MERGE_REJECTED, rejectConsumerMock);
    }
}
