package com.jforex.programming.order.command;

import com.jforex.programming.order.event.OrderEventTypeData;

import com.dukascopy.api.IOrder;

public class SetLabelCommand extends OrderChangeCommand<String> {

    public SetLabelCommand(final IOrder orderToChangeLabel,
                           final String newLabel) {
        super(orderToChangeLabel, () -> orderToChangeLabel.setLabel(newLabel));

        orderEventTypeData = OrderEventTypeData.changeLabelData;
        currentValue = orderToChangeLabel.getLabel();
        newValue = newLabel;
        valueName = "label";
    }
}
