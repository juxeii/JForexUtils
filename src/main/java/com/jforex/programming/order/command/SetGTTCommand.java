package com.jforex.programming.order.command;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Function;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.OrderStaticUtil;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.process.option.GTTOption;

import rx.Completable;

public class SetGTTCommand extends CommonCommand {

    private final IOrder order;
    private final long newGTT;

    public interface Option extends GTTOption<Option> {

        public SetGTTCommand build();
    }

    private SetGTTCommand(final Builder builder) {
        super(builder);
        order = builder.order;
        newGTT = builder.newGTT;
    }

    public final IOrder order() {
        return order;
    }

    public final long newGTT() {
        return newGTT;
    }

    public static final Option create(final IOrder order,
                                      final long newGTT,
                                      final Function<SetGTTCommand, Completable> startFunction) {
        return new Builder(checkNotNull(order),
                           newGTT,
                           startFunction);
    }

    private static class Builder extends CommonBuilder<Option>
                                 implements Option {

        private final IOrder order;
        private final long newGTT;

        private Builder(final IOrder order,
                        final long newGTT,
                        final Function<SetGTTCommand, Completable> startFunction) {
            this.order = order;
            this.newGTT = newGTT;
            this.callable = OrderStaticUtil.runnableToCallable(() -> order.setGoodTillTime(newGTT), order);
            this.callReason = OrderCallReason.CHANGE_GTT;
            this.startFunction = startFunction;
        }

        @Override
        public SetGTTCommand build() {
            return new SetGTTCommand(this);
        }
    }
}