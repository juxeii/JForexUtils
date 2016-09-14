package com.jforex.programming.order;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;
import java.util.function.Function;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.order.command.CloseCommand;
import com.jforex.programming.order.command.Command;
import com.jforex.programming.order.command.MergeCommand;
import com.jforex.programming.order.command.option.ChangeOption;
import com.jforex.programming.order.command.option.CloseOption;
import com.jforex.programming.order.command.option.MergeOption;
import com.jforex.programming.order.command.option.SubmitOption;
import com.jforex.programming.position.PositionOrders;
import com.jforex.programming.position.PositionUtil;

import io.reactivex.Completable;

public final class OrderUtil {

    private final OrderUtilBuilder orderUtilBuilder;
    private final PositionUtil positionUtil;
    private final OrderUtilCompletable orderUtilCompletable;

    public OrderUtil(final OrderUtilBuilder orderUtilBuilder,
                     final PositionUtil positionUtil,
                     final OrderUtilCompletable orderUtilCompletable) {
        this.orderUtilBuilder = orderUtilBuilder;
        this.positionUtil = positionUtil;
        this.orderUtilCompletable = orderUtilCompletable;
    }

    public final SubmitOption submitBuilder(final OrderParams orderParams) {
        return orderUtilBuilder.submitBuilder(checkNotNull(orderParams));
    }

    public final MergeOption mergeBuilder(final String mergeOrderLabel,
                                          final Collection<IOrder> toMergeOrders) {
        return orderUtilBuilder.mergeBuilder(checkNotNull(mergeOrderLabel),
                                             checkNotNull(toMergeOrders));
    }

    public final CloseOption closeBuilder(final IOrder orderToClose) {
        return orderUtilBuilder.closeBuilder(checkNotNull(orderToClose));
    }

    public final ChangeOption setLabelBuilder(final IOrder order,
                                              final String newLabel) {
        return orderUtilBuilder.setLabelBuilder(checkNotNull(order),
                                                checkNotNull(newLabel));
    }

    public final ChangeOption setGTTBuilder(final IOrder order,
                                            final long newGTT) {
        return orderUtilBuilder.setGTTBuilder(checkNotNull(order), newGTT);
    }

    public final ChangeOption setAmountBuilder(final IOrder order,
                                               final double newAmount) {
        return orderUtilBuilder.setAmountBuilder(checkNotNull(order), newAmount);
    }

    public final ChangeOption setOpenPriceBuilder(final IOrder order,
                                                  final double newPrice) {
        return orderUtilBuilder.setOpenPriceBuilder(checkNotNull(order), newPrice);
    }

    public final ChangeOption setSLBuilder(final IOrder order,
                                           final double newSL) {
        return orderUtilBuilder.setSLBuilder(checkNotNull(order), newSL);
    }

    public final ChangeOption setTPBuilder(final IOrder order,
                                           final double newTP) {
        return orderUtilBuilder.setTPBuilder(checkNotNull(order), newTP);
    }

    public final Completable mergePosition(final Instrument instrument,
                                           final Function<Collection<IOrder>, MergeCommand> mergeCommandFactory) {
        return positionUtil.merge(checkNotNull(instrument),
                                  checkNotNull(mergeCommandFactory));
    }

    public final Completable mergeAllPositions(final Function<Collection<IOrder>, MergeCommand> mergeCommandFactory) {
        return positionUtil.mergeAll(checkNotNull(mergeCommandFactory));
    }

    public final Completable closePosition(final Instrument instrument,
                                           final Function<Collection<IOrder>, MergeCommand> mergeCommandFactory,
                                           final Function<IOrder, CloseCommand> closeCommandFactory) {
        return positionUtil.close(checkNotNull(instrument),
                                  checkNotNull(mergeCommandFactory),
                                  checkNotNull(closeCommandFactory));
    }

    public final Completable closeAllPositions(final Function<Collection<IOrder>, MergeCommand> mergeCommandFactory,
                                               final Function<IOrder, CloseCommand> closeCommandFactory) {
        return positionUtil.closeAll(checkNotNull(mergeCommandFactory),
                                     checkNotNull(closeCommandFactory));
    }

    public final Completable commandToCompletable(final Command command) {
        return orderUtilCompletable.forCommand(checkNotNull(command));
    }

    public final PositionOrders positionOrders(final Instrument instrument) {
        return positionUtil.positionOrders(checkNotNull(instrument));
    }
}
