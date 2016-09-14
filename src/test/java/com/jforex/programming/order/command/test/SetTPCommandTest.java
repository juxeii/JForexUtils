package com.jforex.programming.order.command.test;

import static com.jforex.programming.order.event.OrderEventType.CHANGED_TP;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_TP_REJECTED;
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
import com.jforex.programming.order.command.SetTPCommand;
import com.jforex.programming.order.event.OrderEventType;

public class SetTPCommandTest extends CommandTester {

    private SetTPCommand setTPCommand;

    @Mock
    private Consumer<IOrder> setTPRejectActionMock;
    @Mock
    private Consumer<IOrder> setTPActionMock;
    private final double newTP = 1.234;

    @Before
    public void setUp() {
        setTPCommand = SetTPCommand
            .create(buyOrderEURUSD, newTP)
            .doOnError(errorActionMock)
            .doOnComplete(completedActionMock)
            .doOnSetTPReject(setTPRejectActionMock)
            .doOnSetTP(setTPActionMock)
            .retry(noOfRetries, retryDelay)
            .build();

        eventHandlerForType = setTPCommand.data().eventHandlerForType();
    }

    @Test
    public void emptyCommandHasNoRetryParameters() throws Exception {
        final SetTPCommand emptyCommand = SetTPCommand
            .create(buyOrderEURUSD, newTP)
            .build();

        assertNoRetryParams(emptyCommand);
        assertActionsNotNull(emptyCommand);
    }

    @Test
    public void commandValuesAreCorrect() throws Exception {
        assertThat(setTPCommand.order(), equalTo(buyOrderEURUSD));
        assertThat(setTPCommand.newTP(), equalTo(newTP));
        assertThat(setTPCommand.data().callReason(), equalTo(OrderCallReason.CHANGE_TP));
        assertRetryParams(setTPCommand);

        setTPCommand.data().callable().call();
        verify(buyOrderEURUSD).setTakeProfitPrice(newTP);
    }

    @Test
    public void orderEventTypeDataIsCorrect() {
        assertEventTypesForCommand(EnumSet.of(CHANGED_TP,
                                              CHANGE_TP_REJECTED,
                                              NOTIFICATION),
                                   setTPCommand);
        assertFinishEventTypesForCommand(EnumSet.of(CHANGED_TP,
                                                    CHANGE_TP_REJECTED),
                                         setTPCommand);
        assertRejectEventTypesForCommand(EnumSet.of(CHANGE_TP_REJECTED),
                                         setTPCommand);
    }

    @Test
    public void actionsAreCorrectMapped() {
        assertThat(eventHandlerForType.size(), equalTo(2));
        eventHandlerForType.get(OrderEventType.CHANGED_TP).accept(buyOrderEURUSD);
        eventHandlerForType.get(OrderEventType.CHANGE_TP_REJECTED).accept(buyOrderEURUSD);

        assertThat(setTPCommand.data().completedAction(), equalTo(completedActionMock));
        assertThat(setTPCommand.data().errorAction(), equalTo(errorActionMock));

        verify(setTPRejectActionMock).accept(buyOrderEURUSD);
        verify(setTPActionMock).accept(buyOrderEURUSD);
    }
}
