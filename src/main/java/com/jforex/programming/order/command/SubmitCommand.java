package com.jforex.programming.order.command;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.jforex.programming.order.event.OrderEventType.FILL_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.FULLY_FILLED;
import static com.jforex.programming.order.event.OrderEventType.NOTIFICATION;
import static com.jforex.programming.order.event.OrderEventType.PARTIAL_FILL_OK;
import static com.jforex.programming.order.event.OrderEventType.SUBMIT_CONDITIONAL_OK;
import static com.jforex.programming.order.event.OrderEventType.SUBMIT_OK;
import static com.jforex.programming.order.event.OrderEventType.SUBMIT_REJECTED;

import java.util.EnumSet;
import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.misc.IEngineUtil;
import com.jforex.programming.order.OrderParams;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.command.option.SubmitOption;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.event.OrderEventTypeData;

public class SubmitCommand extends Command {

    private SubmitCommand(final Builder builder) {
        super(builder);
    }

    public static final SubmitOption create(final OrderParams orderParams,
                                            final IEngineUtil engineUtil) {
        return new Builder(checkNotNull(orderParams), engineUtil);
    }

    private static class Builder extends CommonBuilder<SubmitOption>
            implements SubmitOption {

        private Builder(final OrderParams orderParams,
                        final IEngineUtil engineUtil) {
            this.callable = engineUtil.submitCallable(orderParams);
            this.callReason = OrderCallReason.SUBMIT;
            this.orderEventTypeData = new OrderEventTypeData(EnumSet.of(FULLY_FILLED, SUBMIT_CONDITIONAL_OK),
                                                             EnumSet.of(FILL_REJECTED, SUBMIT_REJECTED),
                                                             EnumSet.of(NOTIFICATION, SUBMIT_OK, PARTIAL_FILL_OK));
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
