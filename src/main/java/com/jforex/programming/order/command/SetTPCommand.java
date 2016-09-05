package com.jforex.programming.order.command;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Consumer;
import java.util.function.Function;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.OrderStaticUtil;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.process.option.SetTPOption;

import rx.Completable;

public class SetTPCommand extends CommonCommand {

    private final IOrder order;
    private final double newTP;

    private SetTPCommand(final Builder builder) {
        super(builder);
        order = builder.order;
        newTP = builder.newTP;
    }

    public final IOrder order() {
        return order;
    }

    public final double newTP() {
        return newTP;
    }

    public static final SetTPOption create(final IOrder order,
                                        final double newTP,
                                        final Function<SetTPCommand, Completable> startFunction) {
        return new Builder(checkNotNull(order),
                           newTP,
                           startFunction);
    }

    private static class Builder extends CommonBuilder<SetTPOption>
                                 implements SetTPOption {

        private final IOrder order;
        private final double newTP;

        private Builder(final IOrder order,
                        final double newTP,
                        final Function<SetTPCommand, Completable> startFunction) {
            this.order = order;
            this.newTP = newTP;
            this.callable = OrderStaticUtil.runnableToCallable(() -> order.setTakeProfitPrice(newTP), order);
            this.callReason = OrderCallReason.CHANGE_TP;
            this.startFunction = startFunction;
        }

        @Override
        public SetTPOption doOnSetTPReject(final Consumer<IOrder> rejectAction) {
            eventHandlerForType.put(OrderEventType.CHANGE_TP_REJECTED, checkNotNull(rejectAction));
            return this;
        }

        @Override
        public SetTPOption doOnSetTP(final Consumer<IOrder> doneAction) {
            eventHandlerForType.put(OrderEventType.CHANGED_TP, checkNotNull(doneAction));
            return this;
        }

        @Override
        public SetTPCommand build() {
            return new SetTPCommand(this);
        }
    }
}
