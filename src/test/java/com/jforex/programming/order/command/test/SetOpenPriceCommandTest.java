package com.jforex.programming.order.command.test;

import static com.jforex.programming.order.event.OrderEventType.CHANGED_PRICE;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_PRICE_REJECTED;
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
import com.jforex.programming.order.command.SetOpenPriceCommand;
import com.jforex.programming.order.event.OrderEventType;

import io.reactivex.Completable;

public class SetOpenPriceCommandTest extends CommandTester {

    private SetOpenPriceCommand setOpenPriceCommand;

    @Mock
    private Consumer<IOrder> setOpenPriceRejectActionMock;
    @Mock
    private Consumer<IOrder> setOpenPriceActionMock;
    @Mock
    private Function<SetOpenPriceCommand, Completable> startFunctionMock;
    private final double newOpenPrice = 1.234;

    @Before
    public void setUp() {
        setOpenPriceCommand = SetOpenPriceCommand
            .create(buyOrderEURUSD,
                    newOpenPrice,
                    startFunctionMock)
            .doOnError(errorActionMock)
            .doOnCompleted(completedActionMock)
            .doOnSetOpenPriceReject(setOpenPriceRejectActionMock)
            .doOnSetOpenPrice(setOpenPriceActionMock)
            .retry(noOfRetries, retryDelay)
            .build();

        eventHandlerForType = setOpenPriceCommand.eventHandlerForType();
    }

    @Test
    public void emptyCommandHasNoRetryParameters() throws Exception {
        final SetOpenPriceCommand emptyCommand = SetOpenPriceCommand
            .create(buyOrderEURUSD,
                    newOpenPrice,
                    startFunctionMock)
            .build();

        assertNoRetryParams(emptyCommand);
        assertActionsNotNull(emptyCommand);
    }

    @Test
    public void commandValuesAreCorrect() throws Exception {
        assertThat(setOpenPriceCommand.order(), equalTo(buyOrderEURUSD));
        assertThat(setOpenPriceCommand.newOpenPrice(), equalTo(newOpenPrice));
        assertThat(setOpenPriceCommand.callReason(), equalTo(OrderCallReason.CHANGE_PRICE));
        assertRetryParams(setOpenPriceCommand);

        setOpenPriceCommand.callable().call();
        verify(buyOrderEURUSD).setOpenPrice(newOpenPrice);
    }

    @Test
    public void orderEventTypeDataIsCorrect() {
        assertEventTypesForCommand(EnumSet.of(CHANGED_PRICE,
                                              CHANGE_PRICE_REJECTED,
                                              NOTIFICATION),
                                   setOpenPriceCommand);
        assertFinishEventTypesForCommand(EnumSet.of(CHANGED_PRICE,
                                                    CHANGE_PRICE_REJECTED),
                                         setOpenPriceCommand);
        assertRejectEventTypesForCommand(EnumSet.of(CHANGE_PRICE_REJECTED),
                                         setOpenPriceCommand);
    }

    @Test
    public void actionsAreCorrectMapped() {
        assertThat(eventHandlerForType.size(), equalTo(2));
        eventHandlerForType.get(OrderEventType.CHANGED_PRICE).accept(buyOrderEURUSD);
        eventHandlerForType.get(OrderEventType.CHANGE_PRICE_REJECTED).accept(buyOrderEURUSD);

        assertThat(setOpenPriceCommand.completedAction(), equalTo(completedActionMock));
        assertThat(setOpenPriceCommand.errorAction(), equalTo(errorActionMock));

        verify(setOpenPriceRejectActionMock).accept(buyOrderEURUSD);
        verify(setOpenPriceActionMock).accept(buyOrderEURUSD);
    }
}
