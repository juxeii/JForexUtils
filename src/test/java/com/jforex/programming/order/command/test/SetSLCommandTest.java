package com.jforex.programming.order.command.test;

import static com.jforex.programming.order.event.OrderEventType.CHANGED_SL;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_SL_REJECTED;
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
import com.jforex.programming.order.command.SetSLCommand;
import com.jforex.programming.order.event.OrderEventType;

import rx.Completable;

public class SetSLCommandTest extends CommandTester {

    private SetSLCommand setSLCommand;

    @Mock
    private Consumer<IOrder> setSLRejectActionMock;
    @Mock
    private Consumer<IOrder> setSLActionMock;
    @Mock
    private Function<SetSLCommand, Completable> startFunctionMock;
    private final double newSL = 1.234;

    @Before
    public void setUp() {
        setSLCommand = SetSLCommand
            .create(buyOrderEURUSD,
                    newSL,
                    startFunctionMock)
            .doOnError(errorActionMock)
            .doOnCompleted(completedActionMock)
            .doOnSetSLReject(setSLRejectActionMock)
            .doOnSetSL(setSLActionMock)
            .retry(noOfRetries, retryDelay)
            .build();

        eventHandlerForType = setSLCommand.eventHandlerForType();
    }

    @Test
    public void emptyCommandHasNoRetryParameters() {
        final SetSLCommand emptyCommand = SetSLCommand
            .create(buyOrderEURUSD,
                    newSL,
                    startFunctionMock)
            .build();

        assertNoRetryParams(emptyCommand);
        assertActionsNotNull(emptyCommand);
    }

    @Test
    public void commandValuesAreCorrect() throws Exception {
        assertThat(setSLCommand.order(), equalTo(buyOrderEURUSD));
        assertThat(setSLCommand.newSL(), equalTo(newSL));
        assertThat(setSLCommand.callReason(), equalTo(OrderCallReason.CHANGE_SL));
        assertRetryParams(setSLCommand);

        setSLCommand.callable().call();
        verify(buyOrderEURUSD).setStopLossPrice(newSL);
    }

    @Test
    public void orderEventTypeDataIsCorrect() {
        assertEventTypesForCommand(EnumSet.of(CHANGED_SL,
                                              CHANGE_SL_REJECTED,
                                              NOTIFICATION),
                                   setSLCommand);
        assertFinishEventTypesForCommand(EnumSet.of(CHANGED_SL,
                                                    CHANGE_SL_REJECTED),
                                         setSLCommand);
        assertRejectEventTypesForCommand(EnumSet.of(CHANGE_SL_REJECTED),
                                         setSLCommand);
    }

    @Test
    public void actionsAreCorrectMapped() {
        assertThat(eventHandlerForType.size(), equalTo(2));
        eventHandlerForType.get(OrderEventType.CHANGED_SL).accept(buyOrderEURUSD);
        eventHandlerForType.get(OrderEventType.CHANGE_SL_REJECTED).accept(buyOrderEURUSD);

        assertThat(setSLCommand.completedAction(), equalTo(completedActionMock));
        assertThat(setSLCommand.errorAction(), equalTo(errorActionMock));

        verify(setSLRejectActionMock).accept(buyOrderEURUSD);
        verify(setSLActionMock).accept(buyOrderEURUSD);
    }
}
