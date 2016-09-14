package com.jforex.programming.order;

import static com.jforex.programming.order.OrderStaticUtil.runnableToCallable;
import static com.jforex.programming.order.event.OrderEventType.CHANGED_AMOUNT;
import static com.jforex.programming.order.event.OrderEventType.CHANGED_GTT;
import static com.jforex.programming.order.event.OrderEventType.CHANGED_LABEL;
import static com.jforex.programming.order.event.OrderEventType.CHANGED_PRICE;
import static com.jforex.programming.order.event.OrderEventType.CHANGED_SL;
import static com.jforex.programming.order.event.OrderEventType.CHANGED_TP;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_AMOUNT_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_GTT_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_LABEL_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_PRICE_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_SL_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_TP_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.NOTIFICATION;

import java.util.Collection;
import java.util.EnumSet;

import com.dukascopy.api.IOrder;
import com.jforex.programming.misc.IEngineUtil;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.command.ChangeCommand;
import com.jforex.programming.order.command.CloseCommand;
import com.jforex.programming.order.command.MergeCommand;
import com.jforex.programming.order.command.SubmitCommand;
import com.jforex.programming.order.command.option.ChangeOption;
import com.jforex.programming.order.command.option.CloseOption;
import com.jforex.programming.order.command.option.MergeOption;
import com.jforex.programming.order.command.option.SubmitOption;
import com.jforex.programming.order.event.OrderEventTypeData;

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

    public ChangeOption setLabelBuilder(final IOrder order,
                                        final String newLabel) {
        return ChangeCommand.create(runnableToCallable(() -> order.setLabel(newLabel), order),
                                    OrderCallReason.CHANGE_LABEL,
                                    new OrderEventTypeData(EnumSet.of(CHANGED_LABEL),
                                                           EnumSet.of(CHANGE_LABEL_REJECTED),
                                                           EnumSet.of(NOTIFICATION)));
    }

    public ChangeOption setGTTBuilder(final IOrder order,
                                      final long newGTT) {
        return ChangeCommand.create(runnableToCallable(() -> order.setGoodTillTime(newGTT), order),
                                    OrderCallReason.CHANGE_GTT,
                                    new OrderEventTypeData(EnumSet.of(CHANGED_GTT),
                                                           EnumSet.of(CHANGE_GTT_REJECTED),
                                                           EnumSet.of(NOTIFICATION)));
    }

    public ChangeOption setAmountBuilder(final IOrder order,
                                         final double newAmount) {
        return ChangeCommand.create(runnableToCallable(() -> order.setRequestedAmount(newAmount), order),
                                    OrderCallReason.CHANGE_AMOUNT,
                                    new OrderEventTypeData(EnumSet.of(CHANGED_AMOUNT),
                                                           EnumSet.of(CHANGE_AMOUNT_REJECTED),
                                                           EnumSet.of(NOTIFICATION)));
    }

    public ChangeOption setOpenPriceBuilder(final IOrder order,
                                            final double newOpenPrice) {
        return ChangeCommand.create(runnableToCallable(() -> order.setOpenPrice(newOpenPrice), order),
                                    OrderCallReason.CHANGE_PRICE,
                                    new OrderEventTypeData(EnumSet.of(CHANGED_PRICE),
                                                           EnumSet.of(CHANGE_PRICE_REJECTED),
                                                           EnumSet.of(NOTIFICATION)));
    }

    public ChangeOption setSLBuilder(final IOrder order,
                                     final double newSL) {
        return ChangeCommand.create(runnableToCallable(() -> order.setStopLossPrice(newSL), order),
                                    OrderCallReason.CHANGE_SL,
                                    new OrderEventTypeData(EnumSet.of(CHANGED_SL),
                                                           EnumSet.of(CHANGE_SL_REJECTED),
                                                           EnumSet.of(NOTIFICATION)));
    }

    public ChangeOption setTPBuilder(final IOrder order,
                                     final double newTP) {
        return ChangeCommand.create(runnableToCallable(() -> order.setTakeProfitPrice(newTP), order),
                                    OrderCallReason.CHANGE_TP,
                                    new OrderEventTypeData(EnumSet.of(CHANGED_TP),
                                                           EnumSet.of(CHANGE_TP_REJECTED),
                                                           EnumSet.of(NOTIFICATION)));
    }
}
