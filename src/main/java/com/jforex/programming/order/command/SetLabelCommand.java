package com.jforex.programming.order.command;

import static com.google.common.base.Preconditions.checkNotNull;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.process.option.LabelOption;

import rx.Observable;

public class SetLabelCommand extends CommonCommand {

    private final IOrder order;
    private final String newLabel;

    public interface Option extends LabelOption<Option> {

        public SetLabelCommand build();
    }

    private SetLabelCommand(final Builder builder) {
        super(builder);
        order = builder.order;
        newLabel = builder.newLabel;
    }

    public final IOrder order() {
        return order;
    }

    public final String newLabel() {
        return newLabel;
    }

    public static final Option create(final IOrder order,
                                      final String newLabel,
                                      final Observable<OrderEvent> observable) {
        return new Builder(checkNotNull(order),
                           checkNotNull(newLabel),
                           observable);
    }

    private static class Builder extends CommonBuilder<Option>
                                 implements Option {

        private final IOrder order;
        private final String newLabel;

        private Builder(final IOrder order,
                        final String newLabel,
                        final Observable<OrderEvent> observable) {
            this.order = order;
            this.newLabel = newLabel;
            final String commonLog = "label from " + order.getLabel() + " to " + newLabel;
            this.observable = changeObservable(observable, order, commonLog);
        }

        @Override
        public SetLabelCommand build() {
            return new SetLabelCommand(this);
        }
    }
}
