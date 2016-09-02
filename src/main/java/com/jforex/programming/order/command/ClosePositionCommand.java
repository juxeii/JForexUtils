package com.jforex.programming.order.command;

import static com.google.common.base.Preconditions.checkNotNull;

import com.dukascopy.api.Instrument;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.process.option.CloseOption;

import rx.Observable;

public class ClosePositionCommand extends CommonCommand {

    private final Instrument instrument;

    public interface Option extends CloseOption<Option> {

        public ClosePositionCommand build();
    }

    private ClosePositionCommand(final Builder builder) {
        super(builder);
        instrument = builder.instrument;
    }

    public final Instrument instrument() {
        return instrument;
    }

    public static final Option create(final Instrument instrument,
                                      final Observable<OrderEvent> observable) {
        return new Builder(checkNotNull(instrument), observable);
    }

    private static class Builder extends CommonBuilder<Option>
                                 implements Option {

        private final Instrument instrument;

        private Builder(final Instrument instrument,
                        final Observable<OrderEvent> observable) {
            this.instrument = instrument;
            this.observable = observable
                .doOnSubscribe(() -> logger.info("Starting position close for " + instrument))
                .doOnCompleted(() -> logger.info("Closing position " + instrument + " was successful."))
                .doOnError(e -> logger.error("Closing position " + instrument
                        + " failed! Exception: " + e.getMessage()));
        }

        @Override
        public ClosePositionCommand build() {
            return new ClosePositionCommand(this);
        }
    }
}
