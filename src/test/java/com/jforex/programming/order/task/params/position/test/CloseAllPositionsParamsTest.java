package com.jforex.programming.order.task.params.position.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.task.params.position.CloseAllPositionsParams;
import com.jforex.programming.order.task.params.position.ClosePositionParams;
import com.jforex.programming.order.task.params.test.CommonParamsForTest;

public class CloseAllPositionsParamsTest extends CommonParamsForTest {

    private CloseAllPositionsParams closeAllPositionsParams;

    @Mock
    public ClosePositionParams closePositionParamsMock;
    @Mock
    public Consumer<OrderEvent> consumerMock;
    private final Map<OrderEventType, Consumer<OrderEvent>> consumersForClosePosition = new HashMap<>();

    @Before
    public void setUp() {
        consumersForClosePosition.put(OrderEventType.CLOSE_OK, consumerMock);

        when(closePositionParamsMock.consumerForEvent()).thenReturn(consumersForClosePosition);
    }

    @Test
    public void valuesAreCorrect() {
        closeAllPositionsParams = CloseAllPositionsParams
            .newBuilder(closePositionParamsMock)
            .build();

        consumerForEvent = closeAllPositionsParams.consumerForEvent();

        assertThat(closeAllPositionsParams.closePositionParams(), equalTo(closePositionParamsMock));
        assertThat(closeAllPositionsParams.consumerForEvent().size(), equalTo(1));
        assertEventConsumer(OrderEventType.CLOSE_OK, consumerMock);
    }
}
