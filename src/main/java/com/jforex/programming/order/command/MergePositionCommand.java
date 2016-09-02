package com.jforex.programming.order.command;

import static com.google.common.base.Preconditions.checkNotNull;

import com.dukascopy.api.Instrument;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.process.option.MergeOption;

import rx.Observable;

public class MergePositionCommand extends CommonCommand {

    private final String mergeOrderLabel;
    private final Instrument instrument;

    public interface Option extends MergeOption<Option> {

        public MergePositionCommand build();
    }

    private MergePositionCommand(final Builder builder) {
        super(builder);
        mergeOrderLabel = builder.mergeOrderLabel;
        instrument = builder.instrument;
    }

    public final String mergeOrderLabel() {
        return mergeOrderLabel;
    }

    public final Instrument instrument() {
        return instrument;
    }

    public static final Option create(final String mergeOrderLabel,
                                      final Instrument instrument,
                                      final Observable<OrderEvent> observable) {
        return new Builder(checkNotNull(mergeOrderLabel),
                           checkNotNull(instrument),
                           observable);
    }

    private static class Builder extends CommonBuilder<Option>
                                 implements Option {

        private final String mergeOrderLabel;
        private final Instrument instrument;

        private Builder(final String mergeOrderLabel,
                        final Instrument instrument,
                        final Observable<OrderEvent> observable) {
            this.mergeOrderLabel = mergeOrderLabel;
            this.instrument = instrument;
            this.observable = observable
                .doOnSubscribe(() -> logger.info("Starting position merge for " +
                        instrument + " with label " + mergeOrderLabel))
                .doOnError(e -> logger.error("Position merge for " + instrument
                        + "  with label " + mergeOrderLabel + " failed!" + "Exception: " + e.getMessage()))
                .doOnCompleted(() -> logger.info("Position merge for " + instrument
                        + "  with label " + mergeOrderLabel + " was successful."));
        }

        @Override
        public MergePositionCommand build() {
            return new MergePositionCommand(this);
        }
    }
}
