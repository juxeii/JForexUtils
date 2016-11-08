package com.jforex.programming.order.task.params.position.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.task.MergeExecutionMode;
import com.jforex.programming.order.task.params.position.BatchCancelSLParams;
import com.jforex.programming.order.task.params.position.BatchCancelSLTPParams;
import com.jforex.programming.order.task.params.position.BatchCancelTPParams;
import com.jforex.programming.order.task.params.test.CommonParamsForTest;

public class BatchCancelSLTPParamsTest extends CommonParamsForTest {

    private BatchCancelSLTPParams batchCancelSLTPParams;

    @Mock
    public BatchCancelSLParams batchCancelSLParamsMock;
    @Mock
    public BatchCancelTPParams batchCancelTPParamsMock;
    @Mock
    public Consumer<OrderEvent> consumerMock;
    private final Map<OrderEventType, Consumer<OrderEvent>> consumersForCancelSL = new HashMap<>();
    private final Map<OrderEventType, Consumer<OrderEvent>> consumersForCancelTP = new HashMap<>();

    @Before
    public void setUp() {
        consumersForCancelSL.put(OrderEventType.CHANGE_SL_REJECTED, consumerMock);
        consumersForCancelTP.put(OrderEventType.CHANGED_TP, consumerMock);
        consumersForCancelTP.put(OrderEventType.CHANGE_TP_REJECTED, consumerMock);

        when(batchCancelSLParamsMock.consumerForEvent()).thenReturn(consumersForCancelSL);
        when(batchCancelTPParamsMock.consumerForEvent()).thenReturn(consumersForCancelTP);
    }

    @Test
    public void defaultValuesAreCorrect() {
        batchCancelSLTPParams = BatchCancelSLTPParams
            .newBuilder()
            .build();

        assertThat(batchCancelSLTPParams.mergeExecutionMode(), equalTo(MergeExecutionMode.MergeCancelSLAndTP));
        assertNotNull(batchCancelSLTPParams.consumerForEvent());
        assertNotNull(batchCancelSLTPParams.batchCancelSLParams());
        assertNotNull(batchCancelSLTPParams.batchCancelTPParams());
    }

    @Test
    public void valuesAreCorrect() {
        batchCancelSLTPParams = BatchCancelSLTPParams
            .newBuilder()
            .withMergeExecutionMode(MergeExecutionMode.ConcatCancelSLAndTP)
            .withBatchCancelSLParams(batchCancelSLParamsMock)
            .withBatchCancelTPParams(batchCancelTPParamsMock)
            .build();

        consumerForEvent = batchCancelSLTPParams.consumerForEvent();

        assertThat(batchCancelSLTPParams.mergeExecutionMode(), equalTo(MergeExecutionMode.ConcatCancelSLAndTP));
        assertThat(batchCancelSLTPParams.consumerForEvent().size(), equalTo(3));
        assertEventConsumer(OrderEventType.CHANGE_SL_REJECTED, consumerMock);
        assertEventConsumer(OrderEventType.CHANGED_TP, consumerMock);
        assertEventConsumer(OrderEventType.CHANGE_TP_REJECTED, consumerMock);
    }
}
