package com.jforex.programming.order.task.params.position.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.function.Consumer;
import java.util.function.Function;

import org.junit.Test;
import org.mockito.Mock;

import com.dukascopy.api.Instrument;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.task.params.position.SimpleMergePositionParams;
import com.jforex.programming.order.task.params.test.CommonParamsForTest;

public class SimpleMergePositionParamsTest extends CommonParamsForTest {

    private SimpleMergePositionParams simpleMergePositionParams;

    @Mock
    private Function<Instrument, String> mergeOrderLabelSupplier;
    @Mock
    private Consumer<OrderEvent> mergeConsumerMock;
    @Mock
    private Consumer<OrderEvent> mergeCloseConsumerMock;
    @Mock
    private Consumer<OrderEvent> rejectConsumerMock;

    @Test
    public void mergeOrderLabelSupplierIsCorrect() {
        simpleMergePositionParams = SimpleMergePositionParams
            .mergeWithLabel(mergeOrderLabelSupplier)
            .build();

        simpleMergePositionParams.mergeOrderLabel(instrumentEURUSD);

        verify(mergeOrderLabelSupplier).apply(instrumentEURUSD);
    }

    @Test
    public void handlersAreCorrect() {
        simpleMergePositionParams = SimpleMergePositionParams
            .mergeWithLabel(mergeOrderLabelSupplier)
            .doOnMerge(mergeConsumerMock)
            .doOnMergeClose(mergeCloseConsumerMock)
            .doOnReject(rejectConsumerMock)
            .build();

        consumerForEvent = simpleMergePositionParams.consumerForEvent();

        assertThat(consumerForEvent.size(), equalTo(3));
        assertEventConsumer(OrderEventType.MERGE_OK, mergeConsumerMock);
        assertEventConsumer(OrderEventType.MERGE_CLOSE_OK, mergeCloseConsumerMock);
        assertEventConsumer(OrderEventType.MERGE_REJECTED, rejectConsumerMock);
    }
}
