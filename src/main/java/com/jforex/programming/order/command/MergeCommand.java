package com.jforex.programming.order.command;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import com.dukascopy.api.IOrder;
import com.jforex.programming.misc.IEngineUtil;
import com.jforex.programming.order.OrderUtil;
import com.jforex.programming.order.OrderUtilHandler;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.process.option.MergeOption;

public class MergeCommand extends CommonCommand {

    private final String mergeOrderLabel;
    private final Set<IOrder> toMergeOrders;

    public interface Option extends MergeOption<Option> {

        public MergeCommand build();
    }

    private MergeCommand(final Builder builder,
                         final OrderUtilHandler orderUtilHandler,
                         final OrderUtil orderUtil) {
        super(builder);
        mergeOrderLabel = builder.mergeOrderLabel;
        toMergeOrders = builder.toMergeOrders;

        final Function<IOrder, SetSLCommand> setSLCommandCreator =
                order -> orderUtil
                    .setSLBuilder(order, 0.0)
                    .build();
        final List<SetSLCommand> setSLCommands =
                orderUtil.createBatchCommands(toMergeOrders, setSLCommandCreator);

        final Function<IOrder, SetTPCommand> setTPCommandCreator =
                order -> orderUtil
                    .setTPBuilder(order, 0.0)
                    .build();
        final List<SetTPCommand> setTPCommands =
                orderUtil.createBatchCommands(toMergeOrders, setTPCommandCreator);
        final MergeCommand mergeCommand =
                orderUtil
                    .mergeBuilder(mergeOrderLabel, toMergeOrders)
                    .build();
        final List<OrderUtilCommand> allCommands = new ArrayList<>(setSLCommands);
        allCommands.addAll(setTPCommands);
        allCommands.add(mergeCommand);

        orderUtil.startCommandsInOrder(allCommands);
    }

    public static final Option create(final String mergeOrderLabel,
                                      final Set<IOrder> toMergeOrders,
                                      final OrderUtilHandler orderUtilHandler,
                                      final IEngineUtil engineUtil,
                                      final OrderUtil orderUtil) {
        return new Builder(checkNotNull(mergeOrderLabel),
                           checkNotNull(toMergeOrders),
                           orderUtilHandler,
                           engineUtil,
                           orderUtil);
    }

    private static class Builder extends CommonBuilder<Option>
            implements Option {

        private final String mergeOrderLabel;
        private final Set<IOrder> toMergeOrders;

        private Builder(final String mergeOrderLabel,
                        final Set<IOrder> toMergeOrders,
                        final OrderUtilHandler orderUtilHandler,
                        final IEngineUtil engineUtil,
                        final OrderUtil orderUtil) {
            this.mergeOrderLabel = mergeOrderLabel;
            this.toMergeOrders = toMergeOrders;
            this.orderUtilHandler = orderUtilHandler;
            this.orderUtil = orderUtil;
            this.callable = engineUtil.mergeCallable(mergeOrderLabel, toMergeOrders);
            this.callReason = OrderCallReason.MERGE;
        }

        @Override
        public MergeCommand build() {
            return new MergeCommand(this, orderUtilHandler, orderUtil);
        }
    }
}
