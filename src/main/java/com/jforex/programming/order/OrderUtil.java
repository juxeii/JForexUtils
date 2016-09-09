package com.jforex.programming.order;

import java.util.Set;
import java.util.function.Function;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.order.command.CloseCommand;
import com.jforex.programming.order.command.MergeCommand;
import com.jforex.programming.order.process.option.CloseOption;
import com.jforex.programming.order.process.option.MergeOption;
import com.jforex.programming.order.process.option.SetAmountOption;
import com.jforex.programming.order.process.option.SetGTTOption;
import com.jforex.programming.order.process.option.SetLabelOption;
import com.jforex.programming.order.process.option.SetOpenPriceOption;
import com.jforex.programming.order.process.option.SetSLOption;
import com.jforex.programming.order.process.option.SetTPOption;
import com.jforex.programming.order.process.option.SubmitOption;
import com.jforex.programming.position.PositionOrders;
import com.jforex.programming.position.PositionUtil;

import rx.Completable;

public final class OrderUtil {

    private final OrderUtilBuilder orderUtilBuilder;
    private final PositionUtil positionUtil;

    public OrderUtil(final OrderUtilBuilder orderUtilBuilder,
                     final PositionUtil positionUtil) {
        this.orderUtilBuilder = orderUtilBuilder;
        this.positionUtil = positionUtil;
    }

    public final SubmitOption submitBuilder(final OrderParams orderParams) {
        return orderUtilBuilder.submitBuilder(orderParams);
    }

    public final MergeOption mergeBuilder(final String mergeOrderLabel,
                                          final Set<IOrder> toMergeOrders) {
        return orderUtilBuilder.mergeBuilder(mergeOrderLabel, toMergeOrders);
    }

    public final CloseOption closeBuilder(final IOrder orderToClose) {
        return orderUtilBuilder.closeBuilder(orderToClose);
    }

    public final SetLabelOption setLabelBuilder(final IOrder order,
                                                final String newLabel) {
        return orderUtilBuilder.setLabelBuilder(order, newLabel);
    }

    public final SetGTTOption setGTTBuilder(final IOrder order,
                                            final long newGTT) {
        return orderUtilBuilder.setGTTBuilder(order, newGTT);
    }

    public final SetAmountOption setAmountBuilder(final IOrder order,
                                                  final double newAmount) {
        return orderUtilBuilder.setAmountBuilder(order, newAmount);
    }

    public final SetOpenPriceOption setOpenPriceBuilder(final IOrder order,
                                                        final double newPrice) {
        return orderUtilBuilder.setOpenPriceBuilder(order, newPrice);
    }

    public final SetSLOption setSLBuilder(final IOrder order,
                                          final double newSL) {
        return orderUtilBuilder.setSLBuilder(order, newSL);
    }

    public final SetTPOption setTPBuilder(final IOrder order,
                                          final double newTP) {
        return orderUtilBuilder.setTPBuilder(order, newTP);
    }

    public final Completable mergePosition(final Instrument instrument,
                                           final Function<Set<IOrder>, MergeCommand> mergeCommandFactory) {
        return positionUtil.mergePosition(instrument, mergeCommandFactory);
    }

    public final Completable mergeAllPositions(final Function<Set<IOrder>, MergeCommand> mergeCommandFactory) {
        return positionUtil.mergeAllPositions(mergeCommandFactory);
    }

    public final Completable closePosition(final Instrument instrument,
                                           final Function<Set<IOrder>, MergeCommand> mergeCommandFactory,
                                           final Function<IOrder, CloseCommand> closeCommandFactory) {
        return positionUtil.closePosition(instrument, mergeCommandFactory, closeCommandFactory);
    }

    public final Completable closeAllPositions(final Function<Set<IOrder>, MergeCommand> mergeCommandFactory,
                                               final Function<IOrder, CloseCommand> closeCommandFactory) {
        return positionUtil.closeAllPositions(mergeCommandFactory, closeCommandFactory);
    }

    public final PositionOrders positionOrders(final Instrument instrument) {
        return positionUtil.positionOrders(instrument);
    }
}
