package com.jforex.programming.order.process.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Map;
import java.util.function.Consumer;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.process.CloseProcess;
import com.jforex.programming.test.common.CommonUtilForTest;

public class CloseProcessTest extends CommonUtilForTest {

    private CloseProcess process;

    @Mock
    private Consumer<Throwable> errorActionMock;
    @Mock
    private Consumer<IOrder> closeRejectActionMock;
    @Mock
    private Consumer<IOrder> closedActionMock;
    @Mock
    private Consumer<IOrder> partialClosedActionMock;
    private Map<OrderEventType, Consumer<IOrder>> eventHandlerForType;

    @Before
    public void setUp() {
        process = CloseProcess
            .forOrder(buyOrderEURUSD)
            .onError(errorActionMock)
            .onClose(closedActionMock)
            .onCloseReject(closeRejectActionMock)
            .onPartialClose(partialClosedActionMock)
            .doRetries(3, 1500L)
            .build();

        eventHandlerForType = process.eventHandlerForType();
    }

    @Test
    public void processValuesAreCorrect() {
        assertThat(eventHandlerForType.size(), equalTo(3));
        assertThat(process.orderToClose(), equalTo(buyOrderEURUSD));
        assertThat(process.noOfRetries(), equalTo(3));
        assertThat(process.delayInMillis(), equalTo(1500L));
    }

    @Test
    public void actionsAreCorrectMapped() {
        eventHandlerForType.get(OrderEventType.CLOSE_REJECTED).accept(buyOrderEURUSD);
        eventHandlerForType.get(OrderEventType.CLOSE_OK).accept(buyOrderEURUSD);
        eventHandlerForType.get(OrderEventType.PARTIAL_CLOSE_OK).accept(buyOrderEURUSD);

        verify(closeRejectActionMock).accept(buyOrderEURUSD);
        verify(closedActionMock).accept(buyOrderEURUSD);
        verify(partialClosedActionMock).accept(buyOrderEURUSD);
    }
}
