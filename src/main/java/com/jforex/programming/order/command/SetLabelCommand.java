package com.jforex.programming.order.command;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Consumer;
import java.util.function.Function;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.OrderStaticUtil;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.process.option.LabelOption;

import rx.Completable;

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
                                      final Function<SetLabelCommand, Completable> startFunction) {
        return new Builder(checkNotNull(order),
                           checkNotNull(newLabel),
                           startFunction);
    }

    private static class Builder extends CommonBuilder<Option>
                                 implements Option {

        private final IOrder order;
        private final String newLabel;

        private Builder(final IOrder order,
                        final String newLabel,
                        final Function<SetLabelCommand, Completable> startFunction) {
            this.order = order;
            this.newLabel = newLabel;
            this.callable = OrderStaticUtil.runnableToCallable(() -> order.setLabel(newLabel), order);
            this.callReason = OrderCallReason.CHANGE_LABEL;
            this.startFunction = startFunction;
        }

        @Override
        public Option onLabelReject(final Consumer<IOrder> rejectAction) {
            eventHandlerForType.put(OrderEventType.CHANGE_LABEL_REJECTED, checkNotNull(rejectAction));
            return this;
        }

        @Override
        public Option onLabelChange(final Consumer<IOrder> doneAction) {
            eventHandlerForType.put(OrderEventType.CHANGED_LABEL, checkNotNull(doneAction));
            return this;
        }

        @Override
        public SetLabelCommand build() {
            return new SetLabelCommand(this);
        }
    }
}
