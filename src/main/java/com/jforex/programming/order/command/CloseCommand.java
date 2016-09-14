package com.jforex.programming.order.command;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.jforex.programming.order.event.OrderEventType.CLOSE_OK;
import static com.jforex.programming.order.event.OrderEventType.CLOSE_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.NOTIFICATION;
import static com.jforex.programming.order.event.OrderEventType.PARTIAL_CLOSE_OK;

import java.util.EnumSet;
import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.OrderStaticUtil;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.command.option.CloseOption;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.event.OrderEventTypeData;

public class CloseCommand extends Command {

    private CloseCommand(final Builder builder) {
        super(builder);
    }

    public static final CloseOption create(final IOrder orderToClose) {
        return new Builder(checkNotNull(orderToClose));
    }

    public static class Builder extends CommonBuilder<CloseOption>
            implements CloseOption {

        private Builder(final IOrder order) {
            this.callable = OrderStaticUtil.runnableToCallable(() -> order.close(), order);
            this.callReason = OrderCallReason.CLOSE;
            this.orderEventTypeData = new OrderEventTypeData(EnumSet.of(CLOSE_OK),
                                                             EnumSet.of(CLOSE_REJECTED),
                                                             EnumSet.of(NOTIFICATION, PARTIAL_CLOSE_OK));
        }

        @Override
        public CloseOption doOnCloseReject(final Consumer<IOrder> rejectAction) {
            return registerTypeHandler(OrderEventType.CLOSE_REJECTED, rejectAction);
        }

        @Override
        public CloseOption doOnPartialClose(final Consumer<IOrder> partialCloseAction) {
            return registerTypeHandler(OrderEventType.PARTIAL_CLOSE_OK, partialCloseAction);
        }

        @Override
        public CloseOption doOnClose(final Consumer<IOrder> doneAction) {
            return registerTypeHandler(OrderEventType.CLOSE_OK, doneAction);
        }

        @Override
        public CloseCommand build() {
            return new CloseCommand(this);
        }
    }
}
