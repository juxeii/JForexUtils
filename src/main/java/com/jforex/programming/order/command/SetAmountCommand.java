package com.jforex.programming.order.command;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.jforex.programming.order.event.OrderEventType.CHANGED_AMOUNT;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_AMOUNT_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.NOTIFICATION;

import java.util.EnumSet;
import java.util.function.Consumer;
import java.util.function.Function;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.OrderStaticUtil;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.command.option.SetAmountOption;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.event.OrderEventTypeData;

import rx.Completable;

public class SetAmountCommand extends CommonCommand {

    private final IOrder order;
    private final double newAmount;

    private SetAmountCommand(final Builder builder) {
        super(builder);
        order = builder.order;
        newAmount = builder.newAmount;
    }

    public IOrder order() {
        return order;
    }

    public double newAmount() {
        return newAmount;
    }

    public static final SetAmountOption create(final IOrder order,
                                               final double newAmount,
                                               final Function<SetAmountCommand, Completable> startFunction) {
        return new Builder(checkNotNull(order),
                           newAmount,
                           startFunction);
    }

    public static class Builder extends CommonBuilder<SetAmountOption>
                                implements SetAmountOption {

        private final IOrder order;
        private final double newAmount;

        private Builder(final IOrder order,
                        final double newAmount,
                        final Function<SetAmountCommand, Completable> startFunction) {
            this.order = order;
            this.newAmount = newAmount;
            this.callable = OrderStaticUtil.runnableToCallable(() -> order.setRequestedAmount(newAmount), order);
            this.callReason = OrderCallReason.CHANGE_AMOUNT;
            this.orderEventTypeData = new OrderEventTypeData(EnumSet.of(CHANGED_AMOUNT),
                                                             EnumSet.of(CHANGE_AMOUNT_REJECTED),
                                                             EnumSet.of(NOTIFICATION));
            this.startFunction = startFunction;
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
