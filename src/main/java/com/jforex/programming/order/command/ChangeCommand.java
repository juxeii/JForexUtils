package com.jforex.programming.order.command;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.command.option.ChangeOption;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.event.OrderEventTypeData;

public class ChangeCommand extends Command {

    private static final Map<OrderCallReason, OrderEventType> changeRejectEventByReason =
            Maps.immutableEnumMap(ImmutableMap.<OrderCallReason, OrderEventType> builder()
                .put(OrderCallReason.CHANGE_AMOUNT, OrderEventType.CHANGE_AMOUNT_REJECTED)
                .put(OrderCallReason.CHANGE_LABEL, OrderEventType.CHANGE_LABEL_REJECTED)
                .put(OrderCallReason.CHANGE_GTT, OrderEventType.CHANGE_GTT_REJECTED)
                .put(OrderCallReason.CHANGE_PRICE, OrderEventType.CHANGE_PRICE_REJECTED)
                .put(OrderCallReason.CHANGE_SL, OrderEventType.CHANGE_SL_REJECTED)
                .put(OrderCallReason.CHANGE_TP, OrderEventType.CHANGE_TP_REJECTED)
                .build());

    private static final Map<OrderCallReason, OrderEventType> changeDoneByReason =
            Maps.immutableEnumMap(ImmutableMap.<OrderCallReason, OrderEventType> builder()
                .put(OrderCallReason.CHANGE_AMOUNT, OrderEventType.CHANGED_AMOUNT)
                .put(OrderCallReason.CHANGE_LABEL, OrderEventType.CHANGED_LABEL)
                .put(OrderCallReason.CHANGE_GTT, OrderEventType.CHANGED_GTT)
                .put(OrderCallReason.CHANGE_PRICE, OrderEventType.CHANGED_PRICE)
                .put(OrderCallReason.CHANGE_SL, OrderEventType.CHANGED_SL)
                .put(OrderCallReason.CHANGE_TP, OrderEventType.CHANGED_TP)
                .build());

    private ChangeCommand(final Builder builder) {
        super(builder);
    }

    public static final ChangeOption create(final Callable<IOrder> callable,
                                            final OrderCallReason callReason,
                                            final OrderEventTypeData orderEventTypeData) {
        return new Builder(callable,
                           callReason,
                           orderEventTypeData);
    }

    private static class Builder extends CommonBuilder<ChangeOption>
            implements ChangeOption {

        private Builder(final Callable<IOrder> callable,
                        final OrderCallReason callReason,
                        final OrderEventTypeData orderEventTypeData) {
            this.callable = callable;
            this.callReason = callReason;
            this.orderEventTypeData = orderEventTypeData;
        }

        @Override
        public ChangeOption doOnReject(final Consumer<IOrder> rejectConsumer) {
            eventHandlerForType.put(changeRejectEventByReason.get(callReason),
                                    checkNotNull(rejectConsumer));
            return this;
        }

        @Override
        public ChangeOption doOnChange(final Consumer<IOrder> changeConsumer) {
            eventHandlerForType.put(changeDoneByReason.get(callReason),
                                    checkNotNull(changeConsumer));
            return this;
        }

        @Override
        public ChangeCommand build() {
            return new ChangeCommand(this);
        }
    }
}
