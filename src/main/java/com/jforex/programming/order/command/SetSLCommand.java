package com.jforex.programming.order.command;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Consumer;
import java.util.function.Function;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.OrderStaticUtil;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.process.option.SLOption;

import rx.Completable;

public class SetSLCommand extends CommonCommand {

    private final IOrder order;
    private final double newSL;

    private SetSLCommand(final Builder builder) {
        super(builder);
        order = builder.order;
        newSL = builder.newSL;
    }

    public final IOrder order() {
        return order;
    }

    public final double newSL() {
        return newSL;
    }

    public static final SLOption create(final IOrder order,
                                        final double newSL,
                                        final Function<SetSLCommand, Completable> startFunction) {
        return new Builder(checkNotNull(order),
                           newSL,
                           startFunction);
    }

    private static class Builder extends CommonBuilder<SLOption>
                                 implements SLOption {

        private final IOrder order;
        private final double newSL;

        private Builder(final IOrder order,
                        final double newSL,
                        final Function<SetSLCommand, Completable> startFunction) {
            this.order = order;
            this.newSL = newSL;
            this.callable = OrderStaticUtil.runnableToCallable(() -> order.setStopLossPrice(newSL), order);
            this.callReason = OrderCallReason.CHANGE_SL;
            this.startFunction = startFunction;
        }

        @Override
        public SLOption doOnSetSLReject(final Consumer<IOrder> rejectAction) {
            eventHandlerForType.put(OrderEventType.CHANGE_SL_REJECTED, checkNotNull(rejectAction));
            return this;
        }

        @Override
        public SLOption doOnSetSL(final Consumer<IOrder> doneAction) {
            eventHandlerForType.put(OrderEventType.CHANGED_SL, checkNotNull(doneAction));
            return this;
        }

        @Override
        public SetSLCommand build() {
            return new SetSLCommand(this);
        }
    }
}
