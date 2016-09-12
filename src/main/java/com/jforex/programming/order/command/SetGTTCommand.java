package com.jforex.programming.order.command;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.jforex.programming.order.event.OrderEventType.CHANGED_GTT;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_GTT_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.NOTIFICATION;

import java.util.EnumSet;
import java.util.function.Consumer;
import java.util.function.Function;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.OrderStaticUtil;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.command.option.SetGTTOption;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.event.OrderEventTypeData;

import io.reactivex.Completable;

public class SetGTTCommand extends CommonCommand {

    private final IOrder order;
    private final long newGTT;

    private SetGTTCommand(final Builder builder) {
        super(builder);
        order = builder.order;
        newGTT = builder.newGTT;
    }

    public IOrder order() {
        return order;
    }

    public long newGTT() {
        return newGTT;
    }

    public static final SetGTTOption create(final IOrder order,
                                            final long newGTT,
                                            final Function<SetGTTCommand, Completable> startFunction) {
        return new Builder(checkNotNull(order),
                           newGTT,
                           startFunction);
    }

    private static class Builder extends CommonBuilder<SetGTTOption>
                                 implements SetGTTOption {

        private final IOrder order;
        private final long newGTT;

        private Builder(final IOrder order,
                        final long newGTT,
                        final Function<SetGTTCommand, Completable> startFunction) {
            this.order = order;
            this.newGTT = newGTT;
            this.callable = OrderStaticUtil.runnableToCallable(() -> order.setGoodTillTime(newGTT), order);
            this.callReason = OrderCallReason.CHANGE_GTT;
            this.orderEventTypeData = new OrderEventTypeData(EnumSet.of(CHANGED_GTT),
                                                             EnumSet.of(CHANGE_GTT_REJECTED),
                                                             EnumSet.of(NOTIFICATION));
            this.startFunction = startFunction;
        }

        @Override
        public SetGTTOption doOnSetGTTReject(final Consumer<IOrder> rejectAction) {
            eventHandlerForType.put(OrderEventType.CHANGE_GTT_REJECTED, checkNotNull(rejectAction));
            return this;
        }

        @Override
        public SetGTTOption doOnSetGTT(final Consumer<IOrder> doneAction) {
            eventHandlerForType.put(OrderEventType.CHANGED_GTT, checkNotNull(doneAction));
            return this;
        }

        @Override
        public SetGTTCommand build() {
            return new SetGTTCommand(this);
        }
    }
}
