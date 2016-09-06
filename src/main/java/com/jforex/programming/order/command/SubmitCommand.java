package com.jforex.programming.order.command;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Consumer;
import java.util.function.Function;

import com.dukascopy.api.IOrder;
import com.jforex.programming.misc.IEngineUtil;
import com.jforex.programming.order.OrderParams;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.process.option.SubmitOption;

import rx.Completable;

public class SubmitCommand extends CommonCommand {

    private final OrderParams orderParams;

    private SubmitCommand(final Builder builder) {
        super(builder);
        orderParams = builder.orderParams;
    }

    public OrderParams orderParams() {
        return orderParams;
    }

    public static final SubmitOption create(final OrderParams orderParams,
                                            final IEngineUtil engineUtil,
                                            final Function<SubmitCommand, Completable> startFunction) {
        return new Builder(checkNotNull(orderParams),
                           engineUtil,
                           startFunction);
    }

    private static class Builder extends CommonBuilder<SubmitOption>
                                 implements SubmitOption {

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
        public SubmitOption doOnSubmit(final Consumer<IOrder> submitAction) {
            eventHandlerForType.put(OrderEventType.SUBMIT_OK, checkNotNull(submitAction));
            eventHandlerForType.put(OrderEventType.SUBMIT_CONDITIONAL_OK, checkNotNull(submitAction));
            return this;
        }

        @Override
        public SubmitOption doOnSubmitReject(final Consumer<IOrder> submitRejectAction) {
            return registerTypeHandler(OrderEventType.SUBMIT_REJECTED, submitRejectAction);
        }

        @Override
        public SubmitOption doOnFillReject(final Consumer<IOrder> fillRejectAction) {
            return registerTypeHandler(OrderEventType.FILL_REJECTED, fillRejectAction);
        }

        @Override
        public SubmitOption doOnPartialFill(final Consumer<IOrder> partialFillAction) {
            return registerTypeHandler(OrderEventType.PARTIAL_FILL_OK, partialFillAction);
        }

        @Override
        public SubmitOption doOnFill(final Consumer<IOrder> fillAction) {
            return registerTypeHandler(OrderEventType.FULLY_FILLED, fillAction);
        }

        @Override
        public SubmitCommand build() {
            return new SubmitCommand(this);
        }
    }
}
