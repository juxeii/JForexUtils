package com.jforex.programming.order.command;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.jforex.programming.order.event.OrderEventType.CHANGED_AMOUNT;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_AMOUNT_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.NOTIFICATION;

import java.util.EnumSet;
import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.OrderStaticUtil;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.command.option.SetAmountOption;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.event.OrderEventTypeData;

public class SetAmountCommand extends Command {

    private SetAmountCommand(final Builder builder) {
        super(builder);
    }

    public static final SetAmountOption create(final IOrder order,
                                               final double newAmount) {
        return new Builder(checkNotNull(order), newAmount);
    }

    public static class Builder extends CommonBuilder<SetAmountOption>
            implements SetAmountOption {

        private Builder(final IOrder order,
                        final double newAmount) {
            this.callable = OrderStaticUtil.runnableToCallable(() -> order.setRequestedAmount(newAmount), order);
            this.callReason = OrderCallReason.CHANGE_AMOUNT;
            this.orderEventTypeData = new OrderEventTypeData(EnumSet.of(CHANGED_AMOUNT),
                                                             EnumSet.of(CHANGE_AMOUNT_REJECTED),
                                                             EnumSet.of(NOTIFICATION));
        }

        @Override
        public SetAmountOption doOnSetAmountReject(final Consumer<IOrder> rejectAction) {
            return registerTypeHandler(OrderEventType.CHANGE_AMOUNT_REJECTED, rejectAction);
        }

        @Override
        public SetAmountOption doOnSetAmount(final Consumer<IOrder> doneAction) {
            return registerTypeHandler(OrderEventType.CHANGED_AMOUNT, doneAction);
        }

        public SetAmountCommand build() {
            return new SetAmountCommand(this);
        }
    }
}
