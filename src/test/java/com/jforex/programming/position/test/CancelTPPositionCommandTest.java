package com.jforex.programming.position.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.position.CancelTPPositionCommand;
import com.jforex.programming.test.common.InstrumentUtilForTest;

import io.reactivex.Observable;
import io.reactivex.functions.BiFunction;

public class CancelTPPositionCommandTest extends InstrumentUtilForTest {

    private CancelTPPositionCommand command;

    private final BiFunction<Observable<OrderEvent>, IOrder, Observable<OrderEvent>> cancelTPComposer =
            (obs, o) -> obs;
    private final OrderEvent testEvent = new OrderEvent(buyOrderEURUSD,
                                                        OrderEventType.CHANGED_TP,
                                                        true);
    private final Observable<OrderEvent> observable = Observable.just(testEvent);

    @Test
    public void defaultCommandValuesAreCorrect() throws Exception {
        command = CancelTPPositionCommand
            .with(instrumentEURUSD)
            .build();

        assertThat(command.instrument(), equalTo(instrumentEURUSD));
        assertNotNull(command.cancelTPComposer(buyOrderEURUSD).apply(observable));
    }

    @Test
    public void cancelTPComposerIsCorrect() {
        command = CancelTPPositionCommand
            .with(instrumentEURUSD)
            .withcancelTPCompose(cancelTPComposer)
            .build();

        final Observable<OrderEvent> cancelTPComposeObservable =
                observable.compose(command.cancelTPComposer(buyOrderEURUSD));

        assertThat(observable, equalTo(cancelTPComposeObservable));
    }
}
