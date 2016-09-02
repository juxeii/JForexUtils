package com.jforex.programming.order;

import java.util.Collection;
import java.util.Set;
import java.util.function.Function;

import com.dukascopy.api.IOrder;
import com.jforex.programming.misc.IEngineUtil;
import com.jforex.programming.order.command.CloseCommand;
import com.jforex.programming.order.command.MergeCommand;
import com.jforex.programming.order.command.OrderUtilCommand;
import com.jforex.programming.order.command.SetAmountCommand;
import com.jforex.programming.order.command.SetGTTCommand;
import com.jforex.programming.order.command.SetLabelCommand;
import com.jforex.programming.order.command.SetOpenPriceCommand;
import com.jforex.programming.order.command.SetSLCommand;
import com.jforex.programming.order.command.SetTPCommand;
import com.jforex.programming.order.command.SubmitCommand;

public class OrderUtil {

    private final OrderUtilHandler orderUtilHandler;
    private final PositionUtil positionUtil;
    private final IEngineUtil engineUtil;

    public OrderUtil(final OrderUtilHandler orderUtilHandler,
                     final PositionUtil positionUtil,
                     final IEngineUtil engineUtil) {
        this.orderUtilHandler = orderUtilHandler;
        this.positionUtil = positionUtil;
        this.engineUtil = engineUtil;
    }

    public final void startBatchCommand(final Set<IOrder> orders,
                                        final Function<IOrder, OrderUtilCommand> batchCommand) {
        orders.forEach(order -> batchCommand.apply(order).start());
    }

    public final SubmitCommand.Option submitBuilder(final OrderParams orderParams) {
        return SubmitCommand.create(orderParams,
                                    orderUtilHandler,
                                    engineUtil,
                                    positionUtil);
    }

    public final MergeCommand.Option mergeBuilder(final String mergeOrderLabel,
                                                  final Collection<IOrder> toMergeOrders) {
        return MergeCommand.create(mergeOrderLabel,
                                   toMergeOrders,
                                   orderUtilHandler,
                                   engineUtil,
                                   positionUtil);
    }

    public final CloseCommand.Option closeBuilder(final IOrder orderToClose) {
        return CloseCommand.create(orderToClose,
                                   orderUtilHandler);
    }

    public final SetLabelCommand.Option setLabelBuilder(final IOrder order,
                                                        final String newLabel) {
        return SetLabelCommand.create(order,
                                      newLabel,
                                      orderUtilHandler);
    }

    public final SetGTTCommand.Option setGTTBuilder(final IOrder order,
                                                    final long newGTT) {
        return SetGTTCommand.create(order,
                                    newGTT,
                                    orderUtilHandler);
    }

    public final SetAmountCommand.Option setAmountBuilder(final IOrder order,
                                                          final double newAmount) {
        return SetAmountCommand.create(order,
                                       newAmount,
                                       orderUtilHandler);
    }

    public final SetOpenPriceCommand.Option setOpenPriceBuilder(final IOrder order,
                                                                final double newPrice) {
        return SetOpenPriceCommand.create(order,
                                          newPrice,
                                          orderUtilHandler);
    }

    public final SetSLCommand.Option setSLBuilder(final IOrder order,
                                                  final double newSL) {
        return SetSLCommand.create(order,
                                   newSL,
                                   orderUtilHandler);
    }

    public final SetTPCommand.Option setTPBuilder(final IOrder order,
                                                  final double newTP) {
        return SetTPCommand.create(order,
                                   newTP,
                                   orderUtilHandler);
    }
}
