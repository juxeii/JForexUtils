package com.jforex.programming.order.command;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.jforex.programming.order.OrderStaticUtil.instrumentFromOrders;

import java.util.Collection;

import com.dukascopy.api.IOrder;
import com.jforex.programming.misc.IEngineUtil;
import com.jforex.programming.order.OrderUtilHandler;
import com.jforex.programming.order.PositionUtil;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.process.option.MergeOption;

import rx.Observable;

public class MergeCommand extends CommonCommand {

    private final String mergeOrderLabel;
    private final Collection<IOrder> toMergeOrders;

    public interface Option extends MergeOption<Option> {

        public MergeCommand build();
    }

    private MergeCommand(final Builder builder,
                         final OrderUtilHandler orderUtilHandler,
                         final PositionUtil positionUtil) {
        super(builder);
        mergeOrderLabel = builder.mergeOrderLabel;
        toMergeOrders = builder.toMergeOrders;

        this.observable = toMergeOrders.size() < 2
                ? Observable.empty()
                : Observable
                    .just(toMergeOrders)
                    .doOnSubscribe(() -> positionUtil.position(toMergeOrders).markOrdersActive(toMergeOrders))
                    .flatMap(toMergeOrders -> orderUtilHandler.callObservable(this))
                    .doOnNext(positionUtil::addOrderToPosition)
                    .doOnTerminate(() -> positionUtil.position(toMergeOrders).markOrdersIdle(toMergeOrders))
                    .doOnSubscribe(() -> logger.info("Starting to merge with label " + mergeOrderLabel
                            + " for position " + instrumentFromOrders(toMergeOrders) + "."))
                    .doOnCompleted(() -> logger.info("Merging with label " + mergeOrderLabel
                            + " for position " + instrumentFromOrders(toMergeOrders) + " was successful."))
                    .doOnError(e -> logger.error("Merging with label " + mergeOrderLabel + " for position "
                            + instrumentFromOrders(toMergeOrders) + " failed! Exception: " + e.getMessage()));
    }

    public final String mergeOrderLabel() {
        return mergeOrderLabel;
    }

    public final Collection<IOrder> toMergeOrders() {
        return toMergeOrders;
    }

    public static final Option create(final String mergeOrderLabel,
                                      final Collection<IOrder> toMergeOrders,
                                      final OrderUtilHandler orderUtilHandler,
                                      final IEngineUtil engineUtil,
                                      final PositionUtil positionUtil) {
        return new Builder(checkNotNull(mergeOrderLabel),
                           checkNotNull(toMergeOrders),
                           orderUtilHandler,
                           engineUtil,
                           positionUtil);
    }

    private static class Builder extends CommonBuilder<Option>
                                 implements Option {

        private final String mergeOrderLabel;
        private final Collection<IOrder> toMergeOrders;

        private Builder(final String mergeOrderLabel,
                        final Collection<IOrder> toMergeOrders,
                        final OrderUtilHandler orderUtilHandler,
                        final IEngineUtil engineUtil,
                        final PositionUtil positionUtil) {
            this.mergeOrderLabel = mergeOrderLabel;
            this.toMergeOrders = toMergeOrders;
            this.orderUtilHandler = orderUtilHandler;
            this.positionUtil = positionUtil;
            this.callable = engineUtil.mergeCallable(mergeOrderLabel, toMergeOrders);
            this.callReason = OrderCallReason.MERGE;
        }

        @Override
        public MergeCommand build() {
            return new MergeCommand(this, orderUtilHandler, positionUtil);
        }
    }
}
