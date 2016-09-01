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
import com.jforex.programming.order.process.SubmitProcess;
import com.jforex.programming.test.common.CommonUtilForTest;

public class SubmitProcessTest extends CommonUtilForTest {

    private SubmitProcess process;

    @Mock
    private Consumer<Throwable> errorActionMock;
    @Mock
    private Consumer<IOrder> submitRejectActionMock;
    @Mock
    private Consumer<IOrder> fillRejectActionMock;
    @Mock
    private Consumer<IOrder> partialFillActionMock;
    @Mock
    private Consumer<IOrder> submittedActionMock;
    @Mock
    private Consumer<IOrder> filledActionMock;
    private Map<OrderEventType, Consumer<IOrder>> eventHandlerForType;

    @Before
    public void setUp() {
        process = SubmitProcess
            .forOrderParams(buyParamsEURUSD)
            .onError(errorActionMock)
            .onSubmitReject(submitRejectActionMock)
            .onFillReject(fillRejectActionMock)
            .onSubmitOK(submittedActionMock)
            .onPartialFill(partialFillActionMock)
            .onFill(filledActionMock)
            .doRetries(3, 1500L)
            .build();

        eventHandlerForType = process.eventHandlerForType();
    }

    @Test
    public void emptyProcessHasNoRetriesAndActions() {
        final SubmitProcess emptyProcess = SubmitProcess
            .forOrderParams(buyParamsEURUSD)
            .build();

        final Map<OrderEventType, Consumer<IOrder>> eventHandlerForType = emptyProcess.eventHandlerForType();

        assertThat(emptyProcess.noOfRetries(), equalTo(0));
        assertThat(emptyProcess.delayInMillis(), equalTo(0L));
        assertTrue(eventHandlerForType.isEmpty());
    }

    @Test
    public void processValuesAreCorrect() {
        assertThat(process.errorAction(), equalTo(errorActionMock));
        assertThat(process.orderParams(), equalTo(buyParamsEURUSD));
        assertThat(process.noOfRetries(), equalTo(3));
        assertThat(process.delayInMillis(), equalTo(1500L));
        assertThat(eventHandlerForType.size(), equalTo(6));
    }

    @Test
    public void actionsAreCorrectMapped() {
        eventHandlerForType.get(OrderEventType.SUBMIT_REJECTED).accept(buyOrderEURUSD);
        eventHandlerForType.get(OrderEventType.FILL_REJECTED).accept(buyOrderEURUSD);
        eventHandlerForType.get(OrderEventType.SUBMIT_OK).accept(buyOrderEURUSD);
        eventHandlerForType.get(OrderEventType.SUBMIT_CONDITIONAL_OK).accept(buyOrderEURUSD);
        eventHandlerForType.get(OrderEventType.PARTIAL_FILL_OK).accept(buyOrderEURUSD);
        eventHandlerForType.get(OrderEventType.FULLY_FILLED).accept(buyOrderEURUSD);

        verify(submitRejectActionMock).accept(buyOrderEURUSD);
        verify(fillRejectActionMock).accept(buyOrderEURUSD);
        verify(submittedActionMock, times(2)).accept(buyOrderEURUSD);
        verify(partialFillActionMock).accept(buyOrderEURUSD);
        verify(filledActionMock).accept(buyOrderEURUSD);
    }
}
