package com.jforex.programming.order.command;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.jforex.programming.order.event.OrderEventType.MERGE_CLOSE_OK;
import static com.jforex.programming.order.event.OrderEventType.MERGE_OK;
import static com.jforex.programming.order.event.OrderEventType.MERGE_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.NOTIFICATION;

import java.util.Collection;
import java.util.EnumSet;
import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.misc.IEngineUtil;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.command.option.MergeOption;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.event.OrderEventTypeData;

public class MergeCommand implements Command {

    private final String mergeOrderLabel;
    private final Collection<IOrder> toMergeOrders;
    private final CommandData commandData;

    private MergeCommand(final Builder builder) {
        mergeOrderLabel = builder.mergeOrderLabel;
        toMergeOrders = builder.toMergeOrders;
        commandData = builder.commandData;
    }

    public String mergeOrderLabel() {
        return mergeOrderLabel;
    }

    public Collection<IOrder> toMergeOrders() {
        return toMergeOrders;
    }

    @Override
    public CommandData data() {
        return commandData;
    }

    public static final MergeOption create(final String mergeOrderLabel,
                                           final Collection<IOrder> toMergeOrders,
                                           final IEngineUtil engineUtil) {
        return new Builder(checkNotNull(mergeOrderLabel),
                           checkNotNull(toMergeOrders),
                           engineUtil);
    }

    private static class Builder extends CommonBuilder<MergeOption>
                                 implements MergeOption {

        private final String mergeOrderLabel;
        private final Collection<IOrder> toMergeOrders;

        private Builder(final String mergeOrderLabel,
                        final Collection<IOrder> toMergeOrders,
                        final IEngineUtil engineUtil) {
            this.mergeOrderLabel = mergeOrderLabel;
            this.toMergeOrders = toMergeOrders;
            this.callable = engineUtil.mergeCallable(mergeOrderLabel, toMergeOrders);
            this.callReason = OrderCallReason.MERGE;
            this.orderEventTypeData = new OrderEventTypeData(EnumSet.of(MERGE_OK, MERGE_CLOSE_OK),
                                                             EnumSet.of(MERGE_REJECTED),
                                                             EnumSet.of(NOTIFICATION));
        }

        @Override
        public MergeOption doOnMergeReject(final Consumer<IOrder> rejectAction) {
            return registerTypeHandler(OrderEventType.MERGE_REJECTED, rejectAction);
        }

        @Override
        public MergeOption doOnMergeClose(final Consumer<IOrder> mergeCloseAction) {
            return registerTypeHandler(OrderEventType.MERGE_CLOSE_OK, mergeCloseAction);
        }

        @Override
        public MergeOption doOnMerge(final Consumer<IOrder> doneAction) {
            return registerTypeHandler(OrderEventType.MERGE_OK, doneAction);
        }

        @Override
        public MergeCommand build() {
            this.commandData = new CommandData(this);
            return new MergeCommand(this);
        }
    }
}
