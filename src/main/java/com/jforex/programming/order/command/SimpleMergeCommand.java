package com.jforex.programming.order.command;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.jforex.programming.order.OrderStaticUtil.instrumentFromOrders;

import java.util.Set;

import com.dukascopy.api.IOrder;
import com.jforex.programming.misc.IEngineUtil;
import com.jforex.programming.order.OrderUtil;
import com.jforex.programming.order.OrderUtilHandler;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.process.option.SimpleMergeOption;

import rx.Observable;

public class SimpleMergeCommand extends CommonCommand {

    private final String mergeOrderLabel;
    private final Set<IOrder> toMergeOrders;

    public interface Option extends SimpleMergeOption<Option> {

        public SimpleMergeCommand build();
    }

    private SimpleMergeCommand(final Builder builder,
                               final OrderUtilHandler orderUtilHandler,
                               final OrderUtil orderUtil) {
        super(builder);
        mergeOrderLabel = builder.mergeOrderLabel;
        toMergeOrders = builder.toMergeOrders;

        this.observable = toMergeOrders.size() < 2
                ? Observable.empty()
                : Observable
                    .just(toMergeOrders)
                    .doOnSubscribe(() -> orderUtil.position(toMergeOrders).markOrdersActive(toMergeOrders))
                    .flatMap(toMergeOrders -> orderUtilHandler.callObservable(this))
                    .doOnNext(orderUtil::addOrderToPosition)
                    .doOnTerminate(() -> orderUtil.position(toMergeOrders).markOrdersIdle(toMergeOrders))
                    .doOnSubscribe(() -> logger.info("Starting to merge with label " + mergeOrderLabel
                            + " for position " + instrumentFromOrders(toMergeOrders) + "."))
                    .doOnCompleted(() -> logger.info("Merging with label " + mergeOrderLabel
                            + " for position " + instrumentFromOrders(toMergeOrders) + " was successful."))
                    .doOnError(e -> logger.error("Merging with label " + mergeOrderLabel + " for position "
                            + instrumentFromOrders(toMergeOrders) + " failed! Exception: " + e.getMessage()));
    }

    public static final Option create(final String mergeOrderLabel,
                                      final Set<IOrder> toMergeOrders,
                                      final OrderUtilHandler orderUtilHandler,
                                      final IEngineUtil engineUtil,
                                      final OrderUtil orderUtil) {
        return new Builder(checkNotNull(mergeOrderLabel),
                           checkNotNull(toMergeOrders),
                           orderUtilHandler,
                           engineUtil,
                           orderUtil);
    }

    private static class Builder extends CommonBuilder<Option>
            implements Option {

        private final String mergeOrderLabel;
        private final Set<IOrder> toMergeOrders;

        private Builder(final String mergeOrderLabel,
                        final Set<IOrder> toMergeOrders,
                        final OrderUtilHandler orderUtilHandler,
                        final IEngineUtil engineUtil,
                        final OrderUtil orderUtil) {
            this.mergeOrderLabel = mergeOrderLabel;
            this.toMergeOrders = toMergeOrders;
            this.orderUtilHandler = orderUtilHandler;
            this.orderUtil = orderUtil;
            this.callable = engineUtil.mergeCallable(mergeOrderLabel, toMergeOrders);
            this.callReason = OrderCallReason.MERGE;
        }

        @Override
        public SimpleMergeCommand build() {
            return new SimpleMergeCommand(this, orderUtilHandler, orderUtil);
        }
    }
}
