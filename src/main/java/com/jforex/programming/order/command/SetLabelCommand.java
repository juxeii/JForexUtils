package com.jforex.programming.order.command;

import static com.jforex.programming.order.OrderStaticUtil.isLabelSetTo;
import static com.jforex.programming.order.event.OrderEventType.CHANGED_LABEL;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_LABEL_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.NOTIFICATION;

import com.dukascopy.api.IOrder;
import com.google.common.collect.Sets;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.event.OrderEventTypeData;

public final class SetLabelCommand extends OrderChangeCommand<String> {

    public SetLabelCommand(final IOrder orderToChangeLabel,
                           final String newLabel) {
        super(orderToChangeLabel,
              () -> orderToChangeLabel.setLabel(newLabel),
              OrderEventTypeData.changeLabelData,
              orderToChangeLabel.getLabel(),
              newLabel,
              "label");
    }

    @Override
    public final boolean filter() {
        return !isLabelSetTo(newValue).test(orderToChange);
    }

    @Override
    public OrderCallReason callReason() {
        return OrderCallReason.CHANGE_LABEL;
    }

    @Override
    protected void initDoneEvents() {
        doneEventTypes = Sets.immutableEnumSet(CHANGED_LABEL);
    }

    @Override
    protected void initRejectEvents() {
        rejectEventTypes = Sets.immutableEnumSet(CHANGE_LABEL_REJECTED);
    }

    @Override
    protected void initInfoEvents() {
        infoEventTypes = Sets.immutableEnumSet(NOTIFICATION);
    }
}
