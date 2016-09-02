package com.jforex.programming.order.command;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.jforex.programming.order.OrderStaticUtil.isClosed;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.OrderStaticUtil;
import com.jforex.programming.order.OrderUtilHandler;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.process.option.CloseOption;

import rx.Observable;

public class CloseCommand extends CommonCommand {

    private final IOrder order;

    public interface Option extends CloseOption<Option> {

        public CloseCommand build();
    }

    private CloseCommand(final Builder builder,
                         final OrderUtilHandler orderUtilHandler) {
        super(builder);
        order = builder.order;

        final String commonLog = "state from " + order.getState() + " to " + IOrder.State.CLOSED;
        this.observable = Observable
            .just(order)
            .filter(order -> !isClosed.test(order))
            .flatMap(order -> changeObservable(orderUtilHandler.callObservable(this),
                                               order,
                                               commonLog));
    }

    public final IOrder order() {
        return order;
    }

    public static final Option create(final IOrder orderToClose,
                                      final OrderUtilHandler orderUtilHandler) {
        return new Builder(checkNotNull(orderToClose), orderUtilHandler);
    }

    public static class Builder extends CommonBuilder<Option>
                                implements Option {

        private final IOrder order;

        private Builder(final IOrder order,
                        final OrderUtilHandler orderUtilHandler) {
            this.order = order;
            this.orderUtilHandler = orderUtilHandler;
            this.callable = OrderStaticUtil.runnableToCallable(() -> order.close(), order);
            this.callReason = OrderCallReason.CLOSE;
        }

        @Override
        public CloseCommand build() {
            return new CloseCommand(this, orderUtilHandler);
        }
    }
}
