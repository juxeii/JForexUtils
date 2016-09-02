package com.jforex.programming.order.command;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.jforex.programming.order.OrderStaticUtil.instrumentFromOrders;

import java.util.Collection;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.process.option.MergeOption;

import rx.Observable;

public class MergeCommand extends CommonCommand {

    private final String mergeOrderLabel;
    private final Collection<IOrder> toMergeOrders;

    public interface Option extends MergeOption<Option> {

        public MergeCommand build();
    }

    private MergeCommand(final Builder builder) {
        super(builder);
        mergeOrderLabel = builder.mergeOrderLabel;
        toMergeOrders = builder.toMergeOrders;
    }

    public final String mergeOrderLabel() {
        return mergeOrderLabel;
    }

    public final Collection<IOrder> toMergeOrders() {
        return toMergeOrders;
    }

    public static final Option create(final String mergeOrderLabel,
                                      final Collection<IOrder> toMergeOrders,
                                      final Observable<OrderEvent> observable) {
        return new Builder(checkNotNull(mergeOrderLabel),
                           checkNotNull(toMergeOrders),
                           observable);
    }

    private static class Builder extends CommonBuilder<Option>
                                 implements Option {

        private final String mergeOrderLabel;
        private final Collection<IOrder> toMergeOrders;

        private Builder(final String mergeOrderLabel,
                        final Collection<IOrder> toMergeOrders,
                        final Observable<OrderEvent> observable) {
            this.mergeOrderLabel = mergeOrderLabel;
            this.toMergeOrders = toMergeOrders;
            this.observable = observable
                .doOnSubscribe(() -> logger.info("Starting to merge with label " + mergeOrderLabel
                        + " for position " + instrumentFromOrders(toMergeOrders) + "."))
                .doOnCompleted(() -> logger.info("Merging with label " + mergeOrderLabel
                        + " for position " + instrumentFromOrders(toMergeOrders) + " was successful."))
                .doOnError(e -> logger.error("Merging with label " + mergeOrderLabel + " for position "
                        + instrumentFromOrders(toMergeOrders) + " failed! Exception: " + e.getMessage()));
        }

        @Override
        public MergeCommand build() {
            return new MergeCommand(this);
        }
    }
}
