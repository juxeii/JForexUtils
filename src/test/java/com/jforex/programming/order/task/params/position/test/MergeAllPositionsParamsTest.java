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
import com.jforex.programming.order.task.params.position.MergeAllPositionsParams;
import com.jforex.programming.order.task.params.position.MergePositionParams;
import com.jforex.programming.order.task.params.test.CommonParamsForTest;

public class MergeAllPositionsParamsTest extends CommonParamsForTest {

    private MergeAllPositionsParams mergeAllPositionsParams;

    @Mock
    public MergePositionParams mergePositionParamsMock;
    @Mock
    public Consumer<OrderEvent> consumerMock;
    private final Map<OrderEventType, Consumer<OrderEvent>> consumersForMergePosition = new HashMap<>();

    @Before
    public void setUp() {
        consumersForMergePosition.put(OrderEventType.MERGE_OK, consumerMock);

        when(mergePositionParamsMock.consumerForEvent()).thenReturn(consumersForMergePosition);
    }

    @Test
    public void defaultValuesAreCorrect() {
        mergeAllPositionsParams = MergeAllPositionsParams
            .newBuilder()
            .build();

        assertNotNull(mergeAllPositionsParams.mergePositionParams());
    }

    @Test
    public void valuesAreCorrect() {
        mergeAllPositionsParams = MergeAllPositionsParams
            .newBuilder()
            .withMergePositionParams(mergePositionParamsMock)
            .build();

        consumerForEvent = mergeAllPositionsParams.consumerForEvent();

        assertThat(mergeAllPositionsParams.mergePositionParams(), equalTo(mergePositionParamsMock));
        assertThat(mergeAllPositionsParams.consumerForEvent().size(), equalTo(1));
        assertEventConsumer(OrderEventType.MERGE_OK, consumerMock);
    }
}
