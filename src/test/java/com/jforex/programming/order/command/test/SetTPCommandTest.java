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
import com.jforex.programming.order.command.SetTPCommand;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.test.common.CommonUtilForTest;

import rx.Observable;

public class SetTPCommandTest extends CommonUtilForTest {

    private SetTPCommand command;

    @Mock
    private Consumer<Throwable> errorActionMock;
    @Mock
    private Consumer<IOrder> doneActionMock;
    @Mock
    private Consumer<IOrder> rejectedActionMock;
    private final Observable<OrderEvent> observable =
            Observable.just(new OrderEvent(buyOrderEURUSD, OrderEventType.CHANGED_TP));
    private Map<OrderEventType, Consumer<IOrder>> eventHandlerForType;
    private final double newTP = 1.1234;

    @Before
    public void SetTPProcess() {
        command = SetTPCommand
            .create(buyOrderEURUSD, newTP, observable)
            .onError(errorActionMock)
            .onTPChange(doneActionMock)
            .onTPReject(rejectedActionMock)
            .doRetries(3, 1500L)
            .build();

        eventHandlerForType = command.eventHandlerForType();
    }

    @Test
    public void emptyProcessHasNoRetriesAndActions() {
        final SetTPCommand emptyProcess = SetTPCommand
            .create(buyOrderEURUSD, newTP, observable)
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
        assertThat(command.newTP(), equalTo(newTP));
        assertThat(command.noOfRetries(), equalTo(3));
        assertThat(command.delayInMillis(), equalTo(1500L));
        assertThat(eventHandlerForType.size(), equalTo(2));
    }

    @Test
    public void actionsAreCorrectMapped() {
        eventHandlerForType.get(OrderEventType.CHANGE_TP_REJECTED).accept(buyOrderEURUSD);
        eventHandlerForType.get(OrderEventType.CHANGED_TP).accept(buyOrderEURUSD);

        verify(doneActionMock).accept(buyOrderEURUSD);
        verify(rejectedActionMock).accept(buyOrderEURUSD);
    }
}
