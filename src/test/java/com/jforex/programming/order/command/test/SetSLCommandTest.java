package com.jforex.programming.order.command.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.function.Consumer;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.command.SetSLCommand;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.test.common.CommonUtilForTest;

import rx.Observable;

public class SetSLCommandTest extends CommonUtilForTest {

    private SetSLCommand command;

    @Mock
    private Consumer<Throwable> errorActionMock;
    @Mock
    private Consumer<IOrder> doneActionMock;
    @Mock
    private Consumer<IOrder> rejectedActionMock;
    private final Observable<OrderEvent> observable =
            Observable.just(new OrderEvent(buyOrderEURUSD, OrderEventType.CHANGED_SL));
    private Map<OrderEventType, Consumer<IOrder>> eventHandlerForType;
    private final double newSL = 1.1234;

    @Before
    public void SetSLProcess() {
        command = SetSLCommand
            .create(buyOrderEURUSD, newSL, observable)
            .onError(errorActionMock)
            .onSLChange(doneActionMock)
            .onSLReject(rejectedActionMock)
            .doRetries(3, 1500L)
            .build();

        eventHandlerForType = command.eventHandlerForType();
    }

    @Test
    public void emptyProcessHasNoRetriesAndActions() {
        final SetSLCommand emptyProcess = SetSLCommand
            .create(buyOrderEURUSD, newSL, observable)
            .build();

        final Map<OrderEventType, Consumer<IOrder>> eventHandlerForType = emptyProcess.eventHandlerForType();

        assertThat(emptyProcess.noOfRetries(), equalTo(0));
        assertThat(emptyProcess.delayInMillis(), equalTo(0L));
        assertTrue(eventHandlerForType.isEmpty());
    }

    @Test
    public void processValuesAreCorrect() {
        assertThat(command.errorAction(), equalTo(errorActionMock));
        assertThat(command.order(), equalTo(buyOrderEURUSD));
        assertThat(command.newSL(), equalTo(newSL));
        assertThat(command.noOfRetries(), equalTo(3));
        assertThat(command.delayInMillis(), equalTo(1500L));
        assertThat(eventHandlerForType.size(), equalTo(2));
    }

    @Test
    public void actionsAreCorrectMapped() {
        eventHandlerForType.get(OrderEventType.CHANGE_SL_REJECTED).accept(buyOrderEURUSD);
        eventHandlerForType.get(OrderEventType.CHANGED_SL).accept(buyOrderEURUSD);

        verify(doneActionMock).accept(buyOrderEURUSD);
        verify(rejectedActionMock).accept(buyOrderEURUSD);
    }
}
