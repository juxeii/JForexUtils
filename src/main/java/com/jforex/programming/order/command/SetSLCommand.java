package com.jforex.programming.order.command;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.jforex.programming.order.event.OrderEventType.CHANGED_SL;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_SL_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.NOTIFICATION;

import java.util.EnumSet;
import java.util.function.Consumer;
import java.util.function.Function;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.OrderStaticUtil;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.command.option.SetSLOption;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.event.OrderEventTypeData;

import io.reactivex.Completable;

public class SetSLCommand extends CommonCommand {

    private final IOrder order;
    private final double newSL;

    private SetSLCommand(final Builder builder) {
        super(builder);
        order = builder.order;
        newSL = builder.newSL;
    }

    public IOrder order() {
        return order;
    }

    public double newSL() {
        return newSL;
    }

    public static final SetSLOption create(final IOrder order,
                                           final double newSL,
                                           final Function<SetSLCommand, Completable> startFunction) {
        return new Builder(checkNotNull(order),
                           newSL,
                           startFunction);
    }

    private static class Builder extends CommonBuilder<SetSLOption>
                                 implements SetSLOption {

        private final IOrder order;
        private final double newSL;

        private Builder(final IOrder order,
                        final double newSL,
                        final Function<SetSLCommand, Completable> startFunction) {
            this.order = order;
            this.newSL = newSL;
            this.callable = OrderStaticUtil.runnableToCallable(() -> order.setStopLossPrice(newSL), order);
            this.callReason = OrderCallReason.CHANGE_SL;
            this.orderEventTypeData = new OrderEventTypeData(EnumSet.of(CHANGED_SL),
                                                             EnumSet.of(CHANGE_SL_REJECTED),
                                                             EnumSet.of(NOTIFICATION));
            this.startFunction = startFunction;
        }

        @Override
        public SetSLOption doOnSetSLReject(final Consumer<IOrder> rejectAction) {
            return registerTypeHandler(OrderEventType.CHANGE_SL_REJECTED, rejectAction);
        }

        @Override
        public SetSLOption doOnSetSL(final Consumer<IOrder> doneAction) {
            return registerTypeHandler(OrderEventType.CHANGED_SL, doneAction);
        }

        @Override
        public SetSLCommand build() {
            return new SetSLCommand(this);
        }
    }
}
