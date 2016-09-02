package com.jforex.programming.order;

import java.util.Collection;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.command.CloseCommand;
import com.jforex.programming.order.command.MergeCommand;
import com.jforex.programming.order.command.SetAmountCommand;
import com.jforex.programming.order.command.SetGTTCommand;
import com.jforex.programming.order.command.SetLabelCommand;
import com.jforex.programming.order.command.SetOpenPriceCommand;
import com.jforex.programming.order.command.SetSLCommand;
import com.jforex.programming.order.command.SetTPCommand;
import com.jforex.programming.order.command.SubmitCommand;
import com.jforex.programming.order.event.OrderEvent;

import rx.Observable;

public class OrderUtil {

    private final OrderUtilObservable orderUtilObservable;

    public OrderUtil(final OrderUtilObservable orderUtilImpl) {
        this.orderUtilObservable = orderUtilImpl;
    }

    public final SubmitCommand.Option submitBuilder(final OrderParams orderParams) {
        final Observable<OrderEvent> observable = orderUtilObservable.submitOrder(orderParams);
        return SubmitCommand.create(orderParams, observable);
    }

    public final MergeCommand.Option mergeBuilder(final String mergeOrderLabel,
                                                  final Collection<IOrder> toMergeOrders) {
        final Observable<OrderEvent> observable = orderUtilObservable.mergeOrders(mergeOrderLabel, toMergeOrders);
        return MergeCommand.create(mergeOrderLabel, toMergeOrders, observable);
    }

    public final CloseCommand.Option closeBuilder(final IOrder orderToClose) {
        final Observable<OrderEvent> observable = orderUtilObservable.close(orderToClose);
        return CloseCommand.create(orderToClose, observable);
    }

    public final SetLabelCommand.Option setLabelBuilder(final IOrder order,
                                                        final String newLabel) {
        final Observable<OrderEvent> observable = orderUtilObservable.setLabel(order, newLabel);
        return SetLabelCommand.create(order, newLabel, observable);
    }

    public final SetGTTCommand.Option setGTTBuilder(final IOrder order,
                                                    final long newGTT) {
        final Observable<OrderEvent> observable = orderUtilObservable.setGoodTillTime(order, newGTT);
        return SetGTTCommand.create(order, newGTT, observable);
    }

    public final SetAmountCommand.Option setAmountBuilder(final IOrder order,
                                                          final double newAmount) {
        final Observable<OrderEvent> observable = orderUtilObservable.setRequestedAmount(order, newAmount);
        return SetAmountCommand.create(order, newAmount, observable);
    }

    public final SetOpenPriceCommand.Option setOpenPriceBuilder(final IOrder order,
                                                                final double newPrice) {
        final Observable<OrderEvent> observable = orderUtilObservable.setOpenPrice(order, newPrice);
        return SetOpenPriceCommand.create(order, newPrice, observable);
    }

    public final SetSLCommand.Option setSLBuilder(final IOrder order,
                                                  final double newSL) {
        final Observable<OrderEvent> observable = orderUtilObservable.setStopLossPrice(order, newSL);
        return SetSLCommand.create(order, newSL, observable);
    }

    public final SetTPCommand.Option setTPBuilder(final IOrder order,
                                                  final double newTP) {
        final Observable<OrderEvent> observable = orderUtilObservable.setTakeProfitPrice(order, newTP);
        return SetTPCommand.create(order, newTP, observable);
    }
}
