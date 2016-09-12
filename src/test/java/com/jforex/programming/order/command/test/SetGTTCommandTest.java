package com.jforex.programming.order.command.test;

import static com.jforex.programming.order.event.OrderEventType.CHANGED_GTT;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_GTT_REJECTED;
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
import com.jforex.programming.order.command.SetGTTCommand;
import com.jforex.programming.order.event.OrderEventType;

import rx.Completable;

public class SetGTTCommandTest extends CommandTester {

    private SetGTTCommand setGTTCommand;

    @Mock
    private Consumer<IOrder> setGTTRejectActionMock;
    @Mock
    private Consumer<IOrder> setGTTActionMock;
    @Mock
    private Function<SetGTTCommand, Completable> startFunctionMock;
    private final long newGTT = 1L;

    @Before
    public void setUp() {
        setGTTCommand = SetGTTCommand
            .create(buyOrderEURUSD,
                    newGTT,
                    startFunctionMock)
            .doOnError(errorActionMock)
            .doOnCompleted(completedActionMock)
            .doOnSetGTTReject(setGTTRejectActionMock)
            .doOnSetGTT(setGTTActionMock)
            .retry(noOfRetries, retryDelay)
            .build();

        eventHandlerForType = setGTTCommand.eventHandlerForType();
    }

    @Test
    public void emptyCommandHasNoRetryParameters() {
        final SetGTTCommand emptyCommand = SetGTTCommand
            .create(buyOrderEURUSD,
                    newGTT,
                    startFunctionMock)
            .build();

        assertNoRetryParams(emptyCommand);
        assertActionsNotNull(emptyCommand);
    }

    @Test
    public void commandValuesAreCorrect() throws Exception {
        assertThat(setGTTCommand.order(), equalTo(buyOrderEURUSD));
        assertThat(setGTTCommand.newGTT(), equalTo(newGTT));
        assertThat(setGTTCommand.callReason(), equalTo(OrderCallReason.CHANGE_GTT));
        assertRetryParams(setGTTCommand);

        setGTTCommand.callable().call();
        verify(buyOrderEURUSD).setGoodTillTime(newGTT);
    }

    @Test
    public void orderEventTypeDataIsCorrect() {
        assertEventTypesForCommand(EnumSet.of(CHANGED_GTT,
                                              CHANGE_GTT_REJECTED,
                                              NOTIFICATION),
                                   setGTTCommand);
        assertFinishEventTypesForCommand(EnumSet.of(CHANGED_GTT,
                                                    CHANGE_GTT_REJECTED),
                                         setGTTCommand);
        assertRejectEventTypesForCommand(EnumSet.of(CHANGE_GTT_REJECTED),
                                         setGTTCommand);
    }

    @Test
    public void actionsAreCorrectMapped() {
        assertThat(eventHandlerForType.size(), equalTo(2));
        eventHandlerForType.get(OrderEventType.CHANGED_GTT).accept(buyOrderEURUSD);
        eventHandlerForType.get(OrderEventType.CHANGE_GTT_REJECTED).accept(buyOrderEURUSD);

        assertThat(setGTTCommand.completedAction(), equalTo(completedActionMock));
        assertThat(setGTTCommand.errorAction(), equalTo(errorActionMock));

        verify(setGTTRejectActionMock).accept(buyOrderEURUSD);
        verify(setGTTActionMock).accept(buyOrderEURUSD);
    }
}
