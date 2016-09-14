package com.jforex.programming.order.command.test;

import static com.jforex.programming.order.event.OrderEventType.CLOSE_OK;
import static com.jforex.programming.order.event.OrderEventType.CLOSE_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.NOTIFICATION;
import static com.jforex.programming.order.event.OrderEventType.PARTIAL_CLOSE_OK;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.EnumSet;
import java.util.function.Consumer;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.command.CloseCommand;
import com.jforex.programming.order.event.OrderEventType;

public class CloseCommandTest extends CommandTester {

    private CloseCommand closeCommand;

    @Mock
    private Consumer<IOrder> closeRejectActionMock;
    @Mock
    private Consumer<IOrder> partialCloseActionMock;
    @Mock
    private Consumer<IOrder> closeActionMock;

    @Before
    public void setUp() {
        closeCommand = CloseCommand
            .create(buyOrderEURUSD)
            .doOnError(errorActionMock)
            .doOnComplete(completedActionMock)
            .doOnCloseReject(closeRejectActionMock)
            .doOnPartialClose(partialCloseActionMock)
            .doOnClose(closeActionMock)
            .retry(noOfRetries, retryDelay)
            .build();

        eventHandlerForType = closeCommand.data().eventHandlerForType();
    }

    @Test
    public void emptyCommandHasNoRetryParameters() throws Exception {
        final CloseCommand emptyCommand = CloseCommand
            .create(buyOrderEURUSD)
            .build();

        assertNoRetryParams(emptyCommand);
        assertActionsNotNull(emptyCommand);
    }

    @Test
    public void commandValuesAreCorrect() throws Exception {
        assertThat(closeCommand.order(), equalTo(buyOrderEURUSD));
        assertThat(closeCommand.data().callReason(), equalTo(OrderCallReason.CLOSE));
        assertRetryParams(closeCommand);

        closeCommand.data().callable().call();
        verify(buyOrderEURUSD).close();
    }

    @Test
    public void orderEventTypeDataIsCorrect() {
        assertEventTypesForCommand(EnumSet.of(CLOSE_OK,
                                              CLOSE_REJECTED,
                                              PARTIAL_CLOSE_OK,
                                              NOTIFICATION),
                                   closeCommand);
        assertFinishEventTypesForCommand(EnumSet.of(CLOSE_OK,
                                                    CLOSE_REJECTED),
                                         closeCommand);
        assertRejectEventTypesForCommand(EnumSet.of(CLOSE_REJECTED),
                                         closeCommand);
    }

    @Test
    public void actionsAreCorrectMapped() {
        assertThat(eventHandlerForType.size(), equalTo(3));
        eventHandlerForType.get(OrderEventType.CLOSE_OK).accept(buyOrderEURUSD);
        eventHandlerForType.get(OrderEventType.PARTIAL_CLOSE_OK).accept(buyOrderEURUSD);
        eventHandlerForType.get(OrderEventType.CLOSE_REJECTED).accept(buyOrderEURUSD);

        assertThat(closeCommand.data().completedAction(), equalTo(completedActionMock));
        assertThat(closeCommand.data().errorAction(), equalTo(errorActionMock));

        verify(closeRejectActionMock).accept(buyOrderEURUSD);
        verify(closeActionMock).accept(buyOrderEURUSD);
        verify(partialCloseActionMock).accept(buyOrderEURUSD);
    }
}
