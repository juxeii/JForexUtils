package com.jforex.programming.order.command;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.jforex.programming.order.OrderStaticUtil.isLabelSetTo;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.OrderStaticUtil;
import com.jforex.programming.order.OrderUtilHandler;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.process.option.LabelOption;

import rx.Observable;

public class SetLabelCommand extends CommonCommand {

    private final IOrder order;
    private final String newLabel;

    public interface Option extends LabelOption<Option> {

        public SetLabelCommand build();
    }

    private SetLabelCommand(final Builder builder,
                            final OrderUtilHandler orderUtilHandler) {
        super(builder);
        order = builder.order;
        newLabel = builder.newLabel;

        final String commonLog = "label from " + order.getLabel() + " to " + newLabel;
        this.observable = Observable
            .just(order)
            .filter(order -> !isLabelSetTo(newLabel).test(order))
            .flatMap(order -> changeObservable(orderUtilHandler.callObservable(this),
                                               order,
                                               commonLog));
    }

    public static final Option create(final IOrder order,
                                      final String newLabel,
                                      final OrderUtilHandler orderUtilHandler) {
        return new Builder(checkNotNull(order),
                           checkNotNull(newLabel),
                           orderUtilHandler);
    }

    private static class Builder extends CommonBuilder<Option>
                                 implements Option {

        private final IOrder order;
        private final String newLabel;

        private Builder(final IOrder order,
                        final String newLabel,
                        final OrderUtilHandler orderUtilHandler) {
            this.order = order;
            this.newLabel = newLabel;
            this.orderUtilHandler = orderUtilHandler;
            this.callable = OrderStaticUtil.runnableToCallable(() -> order.setLabel(newLabel), order);
            this.callReason = OrderCallReason.CHANGE_LABEL;
        }

        @Override
        public SetLabelCommand build() {
            return new SetLabelCommand(this, orderUtilHandler);
        }
    }
}
