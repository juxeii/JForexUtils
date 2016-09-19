package com.jforex.programming.position.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.position.CancelSLPositionCommand;
import com.jforex.programming.test.common.InstrumentUtilForTest;

import io.reactivex.Observable;
import io.reactivex.functions.BiFunction;

public class CancelSLPositionCommandTest extends InstrumentUtilForTest {

    private CancelSLPositionCommand command;

    private final BiFunction<Observable<OrderEvent>, IOrder, Observable<OrderEvent>> cancelSLComposer =
            (obs, o) -> obs;
    private final OrderEvent testEvent = new OrderEvent(buyOrderEURUSD,
                                                        OrderEventType.CHANGED_SL,
                                                        true);
    private final Observable<OrderEvent> observable = Observable.just(testEvent);

    @Test
    public void defaultCommandValuesAreCorrect() throws Exception {
        command = CancelSLPositionCommand
            .with(instrumentEURUSD)
            .build();

        assertThat(command.instrument(), equalTo(instrumentEURUSD));
        assertNotNull(command.cancelSLComposer(buyOrderEURUSD).apply(observable));
    }

    @Test
    public void cancelSLComposerIsCorrect() {
        command = CancelSLPositionCommand
            .with(instrumentEURUSD)
            .withcancelSLCompose(cancelSLComposer)
            .build();

        final Observable<OrderEvent> cancelSLComposeObservable =
                observable.compose(command.cancelSLComposer(buyOrderEURUSD));

        assertThat(observable, equalTo(cancelSLComposeObservable));
    }
}
