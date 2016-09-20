package com.jforex.programming.position.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.position.ClosePositionCommand;
import com.jforex.programming.test.common.InstrumentUtilForTest;

import io.reactivex.Observable;
import io.reactivex.functions.BiFunction;

public class ClosePositionCommandTest extends InstrumentUtilForTest {

    private ClosePositionCommand command;

    private final String mergeOrderLabel = "mergeOrderLabel";
    private final BiFunction<Observable<OrderEvent>, IOrder, Observable<OrderEvent>> closeOpenedComposer =
            (obs, o) -> obs;
    private final OrderEvent testEvent = new OrderEvent(buyOrderEURUSD,
                                                        OrderEventType.CLOSE_OK,
                                                        true);
    private final Observable<OrderEvent> observable = Observable.just(testEvent);

    @Test
    public void defaultCommandValuesAreCorrect() throws Exception {
        command = ClosePositionCommand
            .with(mergeOrderLabel)
            .closeOpened(closeOpenedComposer)
            .build();

        assertThat(command.mergeOrderLabel(), equalTo(mergeOrderLabel));
    }
}
