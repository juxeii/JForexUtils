package com.jforex.programming.order;

import java.util.Collection;

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
import com.jforex.programming.order.command.option.CloseOption;
import com.jforex.programming.order.command.option.MergeOption;
import com.jforex.programming.order.command.option.SetAmountOption;
import com.jforex.programming.order.command.option.SetGTTOption;
import com.jforex.programming.order.command.option.SetLabelOption;
import com.jforex.programming.order.command.option.SetOpenPriceOption;
import com.jforex.programming.order.command.option.SetSLOption;
import com.jforex.programming.order.command.option.SetTPOption;
import com.jforex.programming.order.command.option.SubmitOption;

public class OrderUtilBuilder {

    private final IEngineUtil engineUtil;

    public OrderUtilBuilder(final IEngineUtil engineUtil) {
        this.engineUtil = engineUtil;
    }

    public SubmitOption submitBuilder(final OrderParams orderParams) {
        return SubmitCommand.create(orderParams, engineUtil);
    }

    public MergeOption mergeBuilder(final String mergeOrderLabel,
                                    final Collection<IOrder> toMergeOrders) {
        return MergeCommand.create(mergeOrderLabel,
                                   toMergeOrders,
                                   engineUtil);
    }

    public CloseOption closeBuilder(final IOrder orderToClose) {
        return CloseCommand.create(orderToClose);
    }

    public SetLabelOption setLabelBuilder(final IOrder order,
                                          final String newLabel) {
        return SetLabelCommand.create(order, newLabel);
    }

    public SetGTTOption setGTTBuilder(final IOrder order,
                                      final long newGTT) {
        return SetGTTCommand.create(order, newGTT);
    }

    public SetAmountOption setAmountBuilder(final IOrder order,
                                            final double newAmount) {
        return SetAmountCommand.create(order, newAmount);
    }

    public SetOpenPriceOption setOpenPriceBuilder(final IOrder order,
                                                  final double newPrice) {
        return SetOpenPriceCommand.create(order, newPrice);
    }

    public SetSLOption setSLBuilder(final IOrder order,
                                    final double newSL) {
        return SetSLCommand.create(order, newSL);
    }

    public SetTPOption setTPBuilder(final IOrder order,
                                    final double newTP) {
        return SetTPCommand.create(order, newTP);
    }
}
