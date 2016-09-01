package com.jforex.programming.order.process.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.function.Consumer;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.process.ClosePositionProcess;
import com.jforex.programming.test.common.InstrumentUtilForTest;

public class ClosePositionProcessTest extends InstrumentUtilForTest {

    private ClosePositionProcess process;

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
        process = ClosePositionProcess
            .forInstrument(instrumentEURUSD)
            .onError(errorActionMock)
            .onClose(closedActionMock)
            .onCloseReject(closeRejectActionMock)
            .onPartialClose(partialClosedActionMock)
            .doRetries(3, 1500L)
            .build();

        eventHandlerForType = process.eventHandlerForType();
    }

    @Test
    public void emptyProcessHasNoRetriesAndActions() {
        final ClosePositionProcess emptyProcess = ClosePositionProcess
            .forInstrument(instrumentEURUSD)
            .build();

        final Map<OrderEventType, Consumer<IOrder>> eventHandlerForType = emptyProcess.eventHandlerForType();

        assertThat(emptyProcess.noOfRetries(), equalTo(0));
        assertThat(emptyProcess.delayInMillis(), equalTo(0L));
        assertTrue(eventHandlerForType.isEmpty());
    }

    @Test
    public void processValuesAreCorrect() {
        assertThat(process.errorAction(), equalTo(errorActionMock));
        assertThat(process.instrument(), equalTo(instrumentEURUSD));
        assertThat(process.noOfRetries(), equalTo(3));
        assertThat(process.delayInMillis(), equalTo(1500L));
        assertThat(eventHandlerForType.size(), equalTo(3));
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
