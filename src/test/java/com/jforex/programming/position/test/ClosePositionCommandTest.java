package com.jforex.programming.position.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.position.ClosePositionCommand;
import com.jforex.programming.test.common.InstrumentUtilForTest;

import io.reactivex.Observable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Function;

public class ClosePositionCommandTest extends InstrumentUtilForTest {

    private ClosePositionCommand command;

    private final String mergeOrderLabel = "mergeOrderLabel";
    private final Function<Observable<OrderEvent>, Observable<OrderEvent>> mergeComposer =
            obs -> obs;
    private final BiFunction<Observable<OrderEvent>, IOrder, Observable<OrderEvent>> closeComposer =
            (obs, o) -> obs;
    private final OrderEvent testEvent = new OrderEvent(buyOrderEURUSD,
                                                        OrderEventType.SUBMIT_CONDITIONAL_OK,
                                                        true);
    private final Observable<OrderEvent> observable = Observable.just(testEvent);

    @Test
    public void defaultCommandValuesAreCorrect() throws Exception {
        command = ClosePositionCommand
            .with(instrumentEURUSD, mergeOrderLabel)
            .build();

        assertThat(command.instrument(), equalTo(instrumentEURUSD));
        assertThat(command.mergeOrderLabel(), equalTo(mergeOrderLabel));
        assertNotNull(command.mergeComposer().apply(observable));
        assertNotNull(command.closeComposer(buyOrderEURUSD).apply(observable));
    }

    @Test
    public void mergeComposerIsCorrect() {
        command = ClosePositionCommand
            .with(instrumentEURUSD, mergeOrderLabel)
            .withMergeCompose(mergeComposer)
            .build();

        final Observable<OrderEvent> mergeComposeObservable = observable.compose(command.mergeComposer());

        assertThat(command.mergeComposer(), equalTo(mergeComposer));
        assertThat(observable, equalTo(mergeComposeObservable));
    }

    @Test
    public void composersAreCorrect() throws Exception {
        command = ClosePositionCommand
            .with(instrumentEURUSD, mergeOrderLabel)
            .withCloseCompose(closeComposer)
            .build();

        final Observable<OrderEvent> closeComposeObservable = observable.compose(command.closeComposer(buyOrderEURUSD));

        assertThat(observable, equalTo(closeComposeObservable));
    }
}
