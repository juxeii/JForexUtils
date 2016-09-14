package com.jforex.programming.order.command.test;

import static com.jforex.programming.order.event.OrderEventType.FILL_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.FULLY_FILLED;
import static com.jforex.programming.order.event.OrderEventType.NOTIFICATION;
import static com.jforex.programming.order.event.OrderEventType.PARTIAL_FILL_OK;
import static com.jforex.programming.order.event.OrderEventType.SUBMIT_CONDITIONAL_OK;
import static com.jforex.programming.order.event.OrderEventType.SUBMIT_OK;
import static com.jforex.programming.order.event.OrderEventType.SUBMIT_REJECTED;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.EnumSet;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.command.SubmitCommand;
import com.jforex.programming.order.event.OrderEventType;

public class SubmitCommandTest extends CommandTester {

    private SubmitCommand submitCommand;

    @Mock
    private Consumer<IOrder> submitRejectActionMock;
    @Mock
    private Consumer<IOrder> fillRejectActionMock;
    @Mock
    private Consumer<IOrder> partialFillActionMock;
    @Mock
    private Consumer<IOrder> submittedActionMock;
    @Mock
    private Consumer<IOrder> filledActionMock;
    @Mock
    private Callable<IOrder> callable;

    @Before
    public void setUp() {
        setUpMocks();

        submitCommand = SubmitCommand
            .create(buyParamsEURUSD, iengineUtilMock)
            .doOnError(errorActionMock)
            .doOnComplete(completedActionMock)
            .doOnSubmitReject(submitRejectActionMock)
            .doOnFillReject(fillRejectActionMock)
            .doOnSubmit(submittedActionMock)
            .doOnPartialFill(partialFillActionMock)
            .doOnFill(filledActionMock)
            .retry(noOfRetries, retryDelay)
            .build();

        eventHandlerForType = submitCommand.data().eventHandlerForType();
    }

    private void setUpMocks() {
        when(iengineUtilMock.submitCallable(buyParamsEURUSD))
            .thenReturn(callable);
    }

    @Test
    public void emptyCommandHasNoRetryParameters() throws Exception {
        final SubmitCommand emptyCommand = SubmitCommand
            .create(buyParamsEURUSD, iengineUtilMock)
            .build();

        assertNoRetryParams(emptyCommand);
        assertActionsNotNull(emptyCommand);
    }

    @Test
    public void commandValuesAreCorrect() {
        assertThat(submitCommand.orderParams(), equalTo(buyParamsEURUSD));
        assertThat(submitCommand.data().callReason(), equalTo(OrderCallReason.SUBMIT));
        assertThat(submitCommand.data().callable(), equalTo(callable));
        assertRetryParams(submitCommand);
    }

    @Test
    public void orderEventTypeDataIsCorrect() {
        assertEventTypesForCommand(EnumSet.of(FULLY_FILLED,
                                              SUBMIT_CONDITIONAL_OK,
                                              FILL_REJECTED,
                                              SUBMIT_REJECTED,
                                              NOTIFICATION,
                                              SUBMIT_OK,
                                              PARTIAL_FILL_OK),
                                   submitCommand);
        assertFinishEventTypesForCommand(EnumSet.of(FULLY_FILLED,
                                                    SUBMIT_CONDITIONAL_OK,
                                                    FILL_REJECTED,
                                                    SUBMIT_REJECTED),
                                         submitCommand);
        assertRejectEventTypesForCommand(EnumSet.of(FILL_REJECTED,
                                                    SUBMIT_REJECTED),
                                         submitCommand);
    }

    @Test
    public void actionsAreCorrectMapped() {
        assertThat(eventHandlerForType.size(), equalTo(6));
        eventHandlerForType.get(OrderEventType.SUBMIT_REJECTED).accept(buyOrderEURUSD);
        eventHandlerForType.get(OrderEventType.FILL_REJECTED).accept(buyOrderEURUSD);
        eventHandlerForType.get(OrderEventType.SUBMIT_OK).accept(buyOrderEURUSD);
        eventHandlerForType.get(OrderEventType.SUBMIT_CONDITIONAL_OK).accept(buyOrderEURUSD);
        eventHandlerForType.get(OrderEventType.PARTIAL_FILL_OK).accept(buyOrderEURUSD);
        eventHandlerForType.get(OrderEventType.FULLY_FILLED).accept(buyOrderEURUSD);

        assertThat(submitCommand.data().completedAction(), equalTo(completedActionMock));
        assertThat(submitCommand.data().errorAction(), equalTo(errorActionMock));

        verify(submitRejectActionMock).accept(buyOrderEURUSD);
        verify(fillRejectActionMock).accept(buyOrderEURUSD);
        verify(submittedActionMock, times(2)).accept(buyOrderEURUSD);
        verify(partialFillActionMock).accept(buyOrderEURUSD);
        verify(filledActionMock).accept(buyOrderEURUSD);
    }
}
