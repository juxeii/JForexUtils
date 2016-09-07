package com.jforex.programming.order.command.test;

import static com.jforex.programming.order.event.OrderEventType.MERGE_CLOSE_OK;
import static com.jforex.programming.order.event.OrderEventType.MERGE_OK;
import static com.jforex.programming.order.event.OrderEventType.MERGE_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.NOTIFICATION;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.google.common.collect.Sets;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.command.MergeCommand;
import com.jforex.programming.order.event.OrderEventType;

import rx.Completable;

public class MergeCommandTest extends CommandTester {

    private MergeCommand mergeCommand;

    @Mock
    private Consumer<IOrder> mergeRejectActionMock;
    @Mock
    private Consumer<IOrder> mergeCloseActionMock;
    @Mock
    private Consumer<IOrder> mergeActionMock;
    @Mock
    private Function<MergeCommand, Completable> startFunctionMock;
    private final Callable<IOrder> callable = () -> buyOrderEURUSD;
    private final String mergeOrderLabel = "mergeOrderLabel";
    private final Set<IOrder> toMergeOrders = Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD);

    @Before
    public void setUp() {
        setUpMocks();

        mergeCommand = MergeCommand
            .create(mergeOrderLabel,
                    toMergeOrders,
                    iengineUtilMock,
                    startFunctionMock)
            .doOnError(errorActionMock)
            .doOnCompleted(completedActionMock)
            .doOnMergeClose(mergeCloseActionMock)
            .doOnMerge(mergeActionMock)
            .doOnMergeReject(mergeRejectActionMock)
            .retry(noOfRetries, retryDelay)
            .build();

        eventHandlerForType = mergeCommand.eventHandlerForType();
    }

    private void setUpMocks() {
        when(iengineUtilMock.mergeCallable(mergeOrderLabel, toMergeOrders))
            .thenReturn(callable);
    }

    @Test
    public void emptyCommandHasNoRetryParameters() {
        final MergeCommand emptyCommand = MergeCommand
            .create(mergeOrderLabel,
                    toMergeOrders,
                    iengineUtilMock,
                    startFunctionMock)
            .build();

        assertNoRetryParams(emptyCommand);
    }

    @Test
    public void commandValuesAreCorrect() {
        assertThat(mergeCommand.mergeOrderLabel(), equalTo(mergeOrderLabel));
        assertThat(mergeCommand.toMergeOrders(), equalTo(toMergeOrders));
        assertThat(mergeCommand.callReason(), equalTo(OrderCallReason.MERGE));
        assertThat(mergeCommand.callable(), equalTo(callable));
        assertRetryParams(mergeCommand);
    }

    @Test
    public void orderEventTypeDataIsCorrect() {
        assertEventTypesForCommand(EnumSet.of(MERGE_OK,
                                              MERGE_CLOSE_OK,
                                              MERGE_REJECTED,
                                              NOTIFICATION),
                                   mergeCommand);
        assertFinishEventTypesForCommand(EnumSet.of(MERGE_OK,
                                                    MERGE_CLOSE_OK,
                                                    MERGE_REJECTED),
                                         mergeCommand);
        assertRejectEventTypesForCommand(EnumSet.of(MERGE_REJECTED),
                                         mergeCommand);
    }

    @Test
    public void actionsAreCorrectMapped() {
        assertThat(eventHandlerForType.size(), equalTo(3));
        eventHandlerForType.get(OrderEventType.MERGE_OK).accept(buyOrderEURUSD);
        eventHandlerForType.get(OrderEventType.MERGE_CLOSE_OK).accept(buyOrderEURUSD);
        eventHandlerForType.get(OrderEventType.MERGE_REJECTED).accept(buyOrderEURUSD);

        assertThat(mergeCommand.completedAction(), equalTo(completedActionMock));
        assertThat(mergeCommand.errorAction(), equalTo(errorActionMock));

        verify(mergeRejectActionMock).accept(buyOrderEURUSD);
        verify(mergeActionMock).accept(buyOrderEURUSD);
        verify(mergeCloseActionMock).accept(buyOrderEURUSD);

        assertActionsNotNull(mergeCommand);
    }
}
