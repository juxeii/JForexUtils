package com.jforex.programming.order;

import java.util.Set;

import com.dukascopy.api.IOrder;
import com.jforex.programming.misc.IEngineUtil;
import com.jforex.programming.order.command.CloseCommand;
import com.jforex.programming.order.command.MergeCommand;
import com.jforex.programming.order.command.SetAmountCommand;
import com.jforex.programming.order.command.SetGTTCommand;
import com.jforex.programming.order.command.SetLabelCommand;
import com.jforex.programming.order.command.SetOpenPriceCommand;
import com.jforex.programming.order.command.SetSLCommand;
import com.jforex.programming.order.command.SetTPCommand;
import com.jforex.programming.order.command.SubmitCommand;
import com.jforex.programming.order.process.option.CloseOption;
import com.jforex.programming.order.process.option.MergeOption;
import com.jforex.programming.order.process.option.SetAmountOption;
import com.jforex.programming.order.process.option.SetGTTOption;
import com.jforex.programming.order.process.option.SetLabelOption;
import com.jforex.programming.order.process.option.SetOpenPriceOption;
import com.jforex.programming.order.process.option.SetSLOption;
import com.jforex.programming.order.process.option.SetTPOption;
import com.jforex.programming.order.process.option.SubmitOption;

public class OrderUtilBuilder {

    private final OrderUtilCompletable orderUtilCompletable;
    private final IEngineUtil engineUtil;

    public OrderUtilBuilder(final IEngineUtil engineUtil,
                            final OrderUtilCompletable orderUtilCompletable) {
        this.engineUtil = engineUtil;
        this.orderUtilCompletable = orderUtilCompletable;
    }

    public SubmitOption submitBuilder(final OrderParams orderParams) {
        return SubmitCommand.create(orderParams,
                                    engineUtil,
                                    orderUtilCompletable::submitOrder);
    }

    public MergeOption mergeBuilder(final String mergeOrderLabel,
                                    final Set<IOrder> toMergeOrders) {
        return MergeCommand.create(mergeOrderLabel,
                                   toMergeOrders,
                                   engineUtil,
                                   orderUtilCompletable::mergeOrders);
    }

    public CloseOption closeBuilder(final IOrder orderToClose) {
        return CloseCommand.create(orderToClose, orderUtilCompletable::close);
    }

    public SetLabelOption setLabelBuilder(final IOrder order,
                                          final String newLabel) {
        return SetLabelCommand.create(order,
                                      newLabel,
                                      orderUtilCompletable::setLabel);
    }

    public SetGTTOption setGTTBuilder(final IOrder order,
                                      final long newGTT) {
        return SetGTTCommand.create(order,
                                    newGTT,
                                    orderUtilCompletable::setGTT);
    }

    public SetAmountOption setAmountBuilder(final IOrder order,
                                            final double newAmount) {
        return SetAmountCommand.create(order,
                                       newAmount,
                                       orderUtilCompletable::setAmount);
    }

    public SetOpenPriceOption setOpenPriceBuilder(final IOrder order,
                                                  final double newPrice) {
        return SetOpenPriceCommand.create(order,
                                          newPrice,
                                          orderUtilCompletable::setOpenPrice);
    }

    public SetSLOption setSLBuilder(final IOrder order,
                                    final double newSL) {
        return SetSLCommand.create(order,
                                   newSL,
                                   orderUtilCompletable::setSL);
    }

    public SetTPOption setTPBuilder(final IOrder order,
                                    final double newTP) {
        return SetTPCommand.create(order,
                                   newTP,
                                   orderUtilCompletable::setTP);
    }
}
