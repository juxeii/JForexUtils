package com.jforex.programming.order.command.test;

import static com.jforex.programming.order.event.OrderEventType.CHANGED_AMOUNT;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_AMOUNT_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.NOTIFICATION;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.EnumSet;
import java.util.function.Consumer;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.command.SetAmountCommand;
import com.jforex.programming.order.event.OrderEventType;

import rx.Completable;

public class SetAmountCommandTest extends CommandTester {

    private SetAmountCommand setAmountCommand;

    @Mock
    private Consumer<IOrder> setAmountRejectActionMock;
    @Mock
    private Consumer<IOrder> setAmountActionMock;
    private final Function<SetAmountCommand, Completable> startFunction = command -> Completable.complete();
    private final double newAmount = 0.12;

    @Before
    public void setUp() {
        setAmountCommand = SetAmountCommand
            .create(buyOrderEURUSD,
                    newAmount,
                    startFunction)
            .doOnError(errorActionMock)
            .doOnCompleted(completedActionMock)
            .doOnSetAmountReject(setAmountRejectActionMock)
            .doOnSetAmount(setAmountActionMock)
            .retry(noOfRetries, retryDelay)
            .build();

        eventHandlerForType = setAmountCommand.eventHandlerForType();
    }

    @Test
    public void emptyCommandHasNoRetryParameters() {
        final SetAmountCommand emptyCommand = SetAmountCommand
            .create(buyOrderEURUSD,
                    newAmount,
                    startFunction)
            .build();

        assertNoRetryParams(emptyCommand);
    }

    @Test
    public void commandValuesAreCorrect() throws Exception {
        assertThat(setAmountCommand.order(), equalTo(buyOrderEURUSD));
        assertThat(setAmountCommand.newAmount(), equalTo(newAmount));
        assertThat(setAmountCommand.callReason(), equalTo(OrderCallReason.CHANGE_AMOUNT));
        assertRetryParams(setAmountCommand);

        setAmountCommand.callable().call();
        verify(buyOrderEURUSD).setRequestedAmount(newAmount);
    }

    @Test
    public void orderEventTypeDataIsCorrect() {
        assertEventTypesForCommand(EnumSet.of(CHANGED_AMOUNT,
                                              CHANGE_AMOUNT_REJECTED,
                                              NOTIFICATION),
                                   setAmountCommand);
        assertFinishEventTypesForCommand(EnumSet.of(CHANGED_AMOUNT,
                                                    CHANGE_AMOUNT_REJECTED),
                                         setAmountCommand);
        assertRejectEventTypesForCommand(EnumSet.of(CHANGE_AMOUNT_REJECTED),
                                         setAmountCommand);
    }

    @Test
    public void actionsAreCorrectMapped() {
        assertThat(eventHandlerForType.size(), equalTo(2));
        eventHandlerForType.get(OrderEventType.CHANGED_AMOUNT).accept(buyOrderEURUSD);
        eventHandlerForType.get(OrderEventType.CHANGE_AMOUNT_REJECTED).accept(buyOrderEURUSD);

        assertThat(setAmountCommand.completedAction(), equalTo(completedActionMock));
        assertThat(setAmountCommand.errorAction(), equalTo(errorActionMock));

        verify(setAmountRejectActionMock).accept(buyOrderEURUSD);
        verify(setAmountActionMock).accept(buyOrderEURUSD);
    }
}
