package com.jforex.programming.order.command;

import static com.google.common.base.Preconditions.checkNotNull;

import com.dukascopy.api.Instrument;
import com.jforex.programming.misc.IEngineUtil;
import com.jforex.programming.order.OrderParams;
import com.jforex.programming.order.OrderUtil;
import com.jforex.programming.order.OrderUtilHandler;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.process.option.SubmitOption;

public class SubmitCommand extends CommonCommand {

    private final OrderParams orderParams;

    public interface Option extends SubmitOption<Option> {

        public SubmitCommand build();
    }

    private SubmitCommand(final Builder builder,
                          final OrderUtilHandler orderUtilHandler,
                          final OrderUtil orderUtil) {
        super(builder);
        orderParams = builder.orderParams;

        final Instrument instrument = orderParams.instrument();
        final String orderLabel = orderParams.label();
        this.observable = orderUtilHandler.callObservable(this)
            .doOnSubscribe(() -> logger.info("Start submit task with label " + orderLabel + " for " + instrument))
            .doOnError(e -> logger.error("Submit task with label " + orderLabel
                    + " for " + instrument + " failed!Exception: " + e.getMessage()))
            .doOnCompleted(() -> logger.info("Submit task with label " + orderLabel
                    + " for " + instrument + " was successful."))
            .doOnNext(orderUtil::addOrderToPosition);
    }

    public static final Option create(final OrderParams orderParams,
                                      final OrderUtilHandler orderUtilHandler,
                                      final IEngineUtil engineUtil,
                                      final OrderUtil orderUtil) {
        return new Builder(checkNotNull(orderParams),
                           orderUtilHandler,
                           engineUtil,
                           orderUtil);
    }

    private static class Builder extends CommonBuilder<Option>
            implements Option {

        private final OrderParams orderParams;

        private Builder(final OrderParams orderParams,
                        final OrderUtilHandler orderUtilHandler,
                        final IEngineUtil engineUtil,
                        final OrderUtil orderUtil) {
            this.orderParams = orderParams;
            this.orderUtilHandler = orderUtilHandler;
            this.orderUtil = orderUtil;
            this.callable = engineUtil.submitCallable(orderParams);
            this.callReason = OrderCallReason.SUBMIT;
        }

        @Override
        public SubmitCommand build() {
            return new SubmitCommand(this, orderUtilHandler, orderUtil);
        }
    }
}
