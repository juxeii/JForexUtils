package com.jforex.programming.order.command;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.jforex.programming.order.event.OrderEventType.CHANGED_LABEL;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_LABEL_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.NOTIFICATION;

import java.util.EnumSet;
import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.OrderStaticUtil;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.command.option.SetLabelOption;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.event.OrderEventTypeData;

public class SetLabelCommand extends CommonCommand {

    private final IOrder order;
    private final String newLabel;

    private SetLabelCommand(final Builder builder) {
        super(builder);
        order = builder.order;
        newLabel = builder.newLabel;
    }

    public IOrder order() {
        return order;
    }

    public String newLabel() {
        return newLabel;
    }

    public static final SetLabelOption create(final IOrder order,
                                              final String newLabel) {
        return new Builder(checkNotNull(order),
                           checkNotNull(newLabel));
    }

    private static class Builder extends CommonBuilder<SetLabelOption>
                                 implements SetLabelOption {

        private final IOrder order;
        private final String newLabel;

        private Builder(final IOrder order,
                        final String newLabel) {
            this.order = order;
            this.newLabel = newLabel;
            this.callable = OrderStaticUtil.runnableToCallable(() -> order.setLabel(newLabel), order);
            this.callReason = OrderCallReason.CHANGE_LABEL;
            this.orderEventTypeData = new OrderEventTypeData(EnumSet.of(CHANGED_LABEL),
                                                             EnumSet.of(CHANGE_LABEL_REJECTED),
                                                             EnumSet.of(NOTIFICATION));
        }

        @Override
        public SetLabelOption doOnSetLabelReject(final Consumer<IOrder> rejectAction) {
            return registerTypeHandler(OrderEventType.CHANGE_LABEL_REJECTED, rejectAction);
        }

        @Override
        public SetLabelOption doOnSetLabel(final Consumer<IOrder> doneAction) {
            return registerTypeHandler(OrderEventType.CHANGED_LABEL, doneAction);
        }

        @Override
        public SetLabelCommand build() {
            return new SetLabelCommand(this);
        }
    }
}
