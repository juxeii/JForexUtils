package com.jforex.programming.order.command.test;

import static com.jforex.programming.order.event.OrderEventType.CHANGED_LABEL;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_LABEL_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.NOTIFICATION;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.EnumSet;
import java.util.function.Consumer;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.command.SetLabelCommand;
import com.jforex.programming.order.event.OrderEventType;

public class SetLabelCommandTest extends CommandTester {

    private SetLabelCommand setLabelCommand;

    @Mock
    private Consumer<IOrder> setLabelRejectActionMock;
    @Mock
    private Consumer<IOrder> setLabelActionMock;
    private final String newLabel = "newLabel";

    @Before
    public void setUp() {
        setLabelCommand = SetLabelCommand
            .create(buyOrderEURUSD, newLabel)
            .doOnError(errorActionMock)
            .doOnComplete(completedActionMock)
            .doOnSetLabelReject(setLabelRejectActionMock)
            .doOnSetLabel(setLabelActionMock)
            .retry(noOfRetries, retryDelay)
            .build();

        eventHandlerForType = setLabelCommand.eventHandlerForType();
    }

    @Test
    public void emptyCommandHasNoRetryParameters() throws Exception {
        final SetLabelCommand emptyCommand = SetLabelCommand
            .create(buyOrderEURUSD, newLabel)
            .build();

        assertNoRetryParams(emptyCommand);
        assertActionsNotNull(emptyCommand);
    }

    @Test
    public void commandValuesAreCorrect() throws Exception {
        assertThat(setLabelCommand.callReason(), equalTo(OrderCallReason.CHANGE_LABEL));
        assertRetryParams(setLabelCommand);

        setLabelCommand.callable().call();
        verify(buyOrderEURUSD).setLabel(newLabel);
    }

    @Test
    public void orderEventTypeDataIsCorrect() {
        assertEventTypesForCommand(EnumSet.of(CHANGED_LABEL,
                                              CHANGE_LABEL_REJECTED,
                                              NOTIFICATION),
                                   setLabelCommand);
        assertFinishEventTypesForCommand(EnumSet.of(CHANGED_LABEL,
                                                    CHANGE_LABEL_REJECTED),
                                         setLabelCommand);
        assertRejectEventTypesForCommand(EnumSet.of(CHANGE_LABEL_REJECTED),
                                         setLabelCommand);
    }

    @Test
    public void actionsAreCorrectMapped() {
        assertThat(eventHandlerForType.size(), equalTo(2));
        eventHandlerForType.get(OrderEventType.CHANGED_LABEL).accept(buyOrderEURUSD);
        eventHandlerForType.get(OrderEventType.CHANGE_LABEL_REJECTED).accept(buyOrderEURUSD);

        assertThat(setLabelCommand.completedAction(), equalTo(completedActionMock));
        assertThat(setLabelCommand.errorAction(), equalTo(errorActionMock));

        verify(setLabelRejectActionMock).accept(buyOrderEURUSD);
        verify(setLabelActionMock).accept(buyOrderEURUSD);
    }
}
