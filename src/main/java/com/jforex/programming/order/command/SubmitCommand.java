package com.jforex.programming.order.command;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Function;

import com.jforex.programming.misc.IEngineUtil;
import com.jforex.programming.order.OrderParams;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.process.option.SubmitOption;

import rx.Completable;

public class SubmitCommand extends CommonCommand {

    private final OrderParams orderParams;

    public interface Option extends SubmitOption<Option> {

        public SubmitCommand build();
    }

    private SubmitCommand(final Builder builder) {
        super(builder);
        orderParams = builder.orderParams;
    }

    public OrderParams orderParams() {
        return orderParams;
    }

    public static final Option create(final OrderParams orderParams,
                                      final IEngineUtil engineUtil,
                                      final Function<SubmitCommand, Completable> startFunction) {
        return new Builder(checkNotNull(orderParams),
                           engineUtil,
                           startFunction);
    }

    private static class Builder extends CommonBuilder<Option>
            implements Option {

        private final OrderParams orderParams;

        private Builder(final OrderParams orderParams,
                        final IEngineUtil engineUtil,
                        final Function<SubmitCommand, Completable> startFunction) {
            this.orderParams = orderParams;
            this.callable = engineUtil.submitCallable(orderParams);
            this.callReason = OrderCallReason.SUBMIT;
            this.startFunction = startFunction;
        }

        @Override
        public SubmitCommand build() {
            return new SubmitCommand(this);
        }
    }
}
