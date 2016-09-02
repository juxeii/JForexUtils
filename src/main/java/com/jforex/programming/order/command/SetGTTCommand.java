package com.jforex.programming.order.command;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.jforex.programming.order.OrderStaticUtil.isGTTSetTo;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.OrderStaticUtil;
import com.jforex.programming.order.OrderUtilHandler;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.process.option.GTTOption;

import rx.Observable;

public class SetGTTCommand extends CommonCommand {

    private final IOrder order;
    private final long newGTT;

    public interface Option extends GTTOption<Option> {

        public SetGTTCommand build();
    }

    private SetGTTCommand(final Builder builder,
                          final OrderUtilHandler orderUtilHandler) {
        super(builder);
        order = builder.order;
        newGTT = builder.newGTT;

        final String commonLog = "GTT from " + order.getGoodTillTime() + " to " + newGTT;
        this.observable = Observable
            .just(order)
            .filter(order -> !isGTTSetTo(newGTT).test(order))
            .flatMap(order -> changeObservable(orderUtilHandler.callObservable(this),
                                               order,
                                               commonLog));
    }

    public final IOrder order() {
        return order;
    }

    public final long newGTT() {
        return newGTT;
    }

    public static final Option create(final IOrder order,
                                      final long newGTT,
                                      final OrderUtilHandler orderUtilHandler) {
        return new Builder(checkNotNull(order),
                           newGTT,
                           orderUtilHandler);
    }

    private static class Builder extends CommonBuilder<Option>
                                 implements Option {

        private final IOrder order;
        private final long newGTT;

        private Builder(final IOrder order,
                        final long newGTT,
                        final OrderUtilHandler orderUtilHandler) {
            this.order = order;
            this.newGTT = newGTT;
            this.orderUtilHandler = orderUtilHandler;
            this.callable = OrderStaticUtil.runnableToCallable(() -> order.setGoodTillTime(newGTT), order);
            this.callReason = OrderCallReason.CHANGE_GTT;
        }

        @Override
        public SetGTTCommand build() {
            return new SetGTTCommand(this, orderUtilHandler);
        }
    }
}
