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
import com.jforex.programming.order.process.SetAmountProcess;
import com.jforex.programming.test.common.CommonUtilForTest;

public class SetAmountProcessTest extends CommonUtilForTest {

    private SetAmountProcess process;

    @Mock
    private Consumer<Throwable> errorActionMock;
    @Mock
    private Consumer<IOrder> doneActionMock;
    @Mock
    private Consumer<IOrder> rejectedActionMock;
    private Map<OrderEventType, Consumer<IOrder>> eventHandlerForType;
    private final double newAmount = 0.12;

    @Before
    public void SetAmountProcess() {
        process = SetAmountProcess
            .forParams(buyOrderEURUSD, newAmount)
            .onError(errorActionMock)
            .onAmountChange(doneActionMock)
            .onAmountReject(rejectedActionMock)
            .doRetries(3, 1500L)
            .build();

        eventHandlerForType = process.eventHandlerForType();
    }

    @Test
    public void emptyProcessHasNoRetriesAndActions() {
        final SetAmountProcess emptyProcess = SetAmountProcess
            .forParams(buyOrderEURUSD, newAmount)
            .build();

        final Map<OrderEventType, Consumer<IOrder>> eventHandlerForType = emptyProcess.eventHandlerForType();

        assertThat(emptyProcess.noOfRetries(), equalTo(0));
        assertThat(emptyProcess.delayInMillis(), equalTo(0L));
        assertTrue(eventHandlerForType.isEmpty());
    }

    @Test
    public void processValuesAreCorrect() {
        assertThat(process.errorAction(), equalTo(errorActionMock));
        assertThat(process.order(), equalTo(buyOrderEURUSD));
        assertThat(process.newAmount(), equalTo(newAmount));
        assertThat(process.noOfRetries(), equalTo(3));
        assertThat(process.delayInMillis(), equalTo(1500L));
        assertThat(eventHandlerForType.size(), equalTo(2));
    }

    @Test
    public void actionsAreCorrectMapped() {
        eventHandlerForType.get(OrderEventType.CHANGE_AMOUNT_REJECTED).accept(buyOrderEURUSD);
        eventHandlerForType.get(OrderEventType.CHANGED_AMOUNT).accept(buyOrderEURUSD);

        verify(doneActionMock).accept(buyOrderEURUSD);
        verify(rejectedActionMock).accept(buyOrderEURUSD);
    }
}
