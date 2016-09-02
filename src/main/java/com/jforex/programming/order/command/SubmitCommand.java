package com.jforex.programming.order.command;

import static com.google.common.base.Preconditions.checkNotNull;

import com.dukascopy.api.Instrument;
import com.jforex.programming.order.OrderParams;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.process.option.SubmitOption;

import rx.Observable;

public class SubmitCommand extends CommonCommand {

    private final OrderParams orderParams;

    public interface Option extends SubmitOption<Option> {

        public SubmitCommand build();
    }

    private SubmitCommand(final Builder builder) {
        super(builder);
        orderParams = builder.orderParams;
    }

    public final OrderParams orderParams() {
        return orderParams;
    }

    public static final Option create(final OrderParams orderParams,
                                      final Observable<OrderEvent> observable) {
        return new Builder(checkNotNull(orderParams), observable);
    }

    private static class Builder extends CommonBuilder<Option>
                                 implements Option {

        private final OrderParams orderParams;

        private Builder(final OrderParams orderParams,
                        final Observable<OrderEvent> observable) {
            this.orderParams = orderParams;
            final Instrument instrument = orderParams.instrument();
            final String orderLabel = orderParams.label();
            this.observable = observable
                .doOnSubscribe(() -> logger.info("Start submit task with label " + orderLabel + " for " + instrument))
                .doOnError(e -> logger.error("Submit task with label " + orderLabel
                        + " for " + instrument + " failed!Exception: " + e.getMessage()))
                .doOnCompleted(() -> logger.info("Submit task with label " + orderLabel
                        + " for " + instrument + " was successful."));
        }

        @Override
        public SubmitCommand build() {
            return new SubmitCommand(this);
        }
    }
}
