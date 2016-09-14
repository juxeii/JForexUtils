package com.jforex.programming.order.command;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.jforex.programming.order.event.OrderEventType.CHANGED_TP;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_TP_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.NOTIFICATION;

import java.util.EnumSet;
import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.OrderStaticUtil;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.command.option.SetTPOption;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.event.OrderEventTypeData;

public class SetTPCommand extends Command {

    private SetTPCommand(final Builder builder) {
        super(builder);
    }

    public static final SetTPOption create(final IOrder order,
                                           final double newTP) {
        return new Builder(checkNotNull(order), newTP);
    }

    private static class Builder extends CommonBuilder<SetTPOption>
            implements SetTPOption {

        private Builder(final IOrder order,
                        final double newTP) {
            this.callable = OrderStaticUtil.runnableToCallable(() -> order.setTakeProfitPrice(newTP), order);
            this.callReason = OrderCallReason.CHANGE_TP;
            this.orderEventTypeData = new OrderEventTypeData(EnumSet.of(CHANGED_TP),
                                                             EnumSet.of(CHANGE_TP_REJECTED),
                                                             EnumSet.of(NOTIFICATION));
        }

        @Override
        public SetTPOption doOnSetTPReject(final Consumer<IOrder> rejectAction) {
            return registerTypeHandler(OrderEventType.CHANGE_TP_REJECTED, rejectAction);
        }

        @Override
        public SetTPOption doOnSetTP(final Consumer<IOrder> doneAction) {
            return registerTypeHandler(OrderEventType.CHANGED_TP, doneAction);
        }

        @Override
        public SetTPCommand build() {
            return new SetTPCommand(this);
        }
    }
}
