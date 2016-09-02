package com.jforex.programming.order;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.order.command.CloseCommand;
import com.jforex.programming.order.command.ClosePositionCommand;
import com.jforex.programming.order.command.MergeCommand;
import com.jforex.programming.order.command.MergePositionCommand;
import com.jforex.programming.order.command.SetAmountCommand;
import com.jforex.programming.order.command.SetGTTCommand;
import com.jforex.programming.order.command.SetLabelCommand;
import com.jforex.programming.order.command.SetOpenPriceCommand;
import com.jforex.programming.order.command.SetSLCommand;
import com.jforex.programming.order.command.SetTPCommand;
import com.jforex.programming.order.command.SubmitAndMergeCommand;
import com.jforex.programming.order.command.SubmitCommand;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.position.PositionOrders;

import rx.Observable;

public class OrderUtil {

    private final OrderUtilObservable orderUtilImpl;

    public OrderUtil(final OrderUtilObservable orderUtilImpl) {
        this.orderUtilImpl = orderUtilImpl;
    }

    public PositionOrders positionOrders(final Instrument instrument) {
        return orderUtilImpl.positionOrders(checkNotNull(instrument));
    }

    public final SubmitCommand.Option submitBuilder(final OrderParams orderParams) {
        final Observable<OrderEvent> observable = orderUtilImpl.submitOrder(orderParams);
        return SubmitCommand.create(orderParams, observable);
    }

    public final SubmitAndMergeCommand.Option submitAndMergePositionBuilder(final OrderParams orderParams,
                                                                            final String mergeOrderLabel) {
        final Observable<OrderEvent> observable = orderUtilImpl.submitAndMergePosition(orderParams, mergeOrderLabel);
        return SubmitAndMergeCommand.create(orderParams, mergeOrderLabel, observable);
    }

    public final SubmitAndMergeCommand.Option submitAndMergePositionToParamsBuilder(final OrderParams orderParams,
                                                                                    final String mergeOrderLabel) {

        final Observable<OrderEvent> observable =
                orderUtilImpl.submitAndMergePositionToParams(orderParams, mergeOrderLabel);
        return SubmitAndMergeCommand.create(orderParams, mergeOrderLabel, observable);
    }

    public final MergeCommand.Option mergeBuilder(final String mergeOrderLabel,
                                                  final Collection<IOrder> toMergeOrders) {
        final Observable<OrderEvent> observable = orderUtilImpl.mergeOrders(mergeOrderLabel, toMergeOrders);
        return MergeCommand.create(mergeOrderLabel, toMergeOrders, observable);
    }

    public final MergePositionCommand.Option mergePositionBuilder(final String mergeOrderLabel,
                                                                  final Instrument instrument) {
        final Observable<OrderEvent> observable = orderUtilImpl.mergePositionOrders(mergeOrderLabel, instrument);
        return MergePositionCommand.create(mergeOrderLabel, instrument, observable);
    }

    public final CloseCommand.Option closeBuilder(final IOrder orderToClose) {
        final Observable<OrderEvent> observable = orderUtilImpl.close(orderToClose);
        return CloseCommand.create(orderToClose, observable);
    }

    public final ClosePositionCommand.Option closePositionBuilder(final Instrument instrument) {
        final Observable<OrderEvent> observable = orderUtilImpl.closePosition(instrument);
        return ClosePositionCommand.create(instrument, observable);
    }

    public final SetLabelCommand.Option setLabelBuilder(final IOrder order,
                                                        final String newLabel) {
        final Observable<OrderEvent> observable = orderUtilImpl.setLabel(order, newLabel);
        return SetLabelCommand.create(order, newLabel, observable);
    }

    public final SetGTTCommand.Option setGTTBuilder(final IOrder order,
                                                    final long newGTT) {
        final Observable<OrderEvent> observable = orderUtilImpl.setGoodTillTime(order, newGTT);
        return SetGTTCommand.create(order, newGTT, observable);
    }

    public final SetAmountCommand.Option setAmountBuilder(final IOrder order,
                                                          final double newAmount) {
        final Observable<OrderEvent> observable = orderUtilImpl.setRequestedAmount(order, newAmount);
        return SetAmountCommand.create(order, newAmount, observable);
    }

    public final SetOpenPriceCommand.Option setOpenPriceBuilder(final IOrder order,
                                                                final double newPrice) {
        final Observable<OrderEvent> observable = orderUtilImpl.setOpenPrice(order, newPrice);
        return SetOpenPriceCommand.create(order, newPrice, observable);
    }

    public final SetSLCommand.Option startSLChange(final IOrder order,
                                                   final double newSL) {
        final Observable<OrderEvent> observable = orderUtilImpl.setStopLossPrice(order, newSL);
        return SetSLCommand.create(order, newSL, observable);
    }

    public final SetTPCommand.Option startTPChange(final IOrder order,
                                                   final double newTP) {
        final Observable<OrderEvent> observable = orderUtilImpl.setTakeProfitPrice(order, newTP);
        return SetTPCommand.create(order, newTP, observable);
    }
}
