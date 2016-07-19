package com.jforex.programming.order.command;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEventTypeData;

public class SetLabelCommand extends OrderChangeCommand<String> {

    public SetLabelCommand(final IOrder orderToChangeLabel,
                           final String newLabel) {
        super(orderToChangeLabel,
              () -> orderToChangeLabel.setLabel(newLabel),
              OrderEventTypeData.changeLabelData,
              orderToChangeLabel.getLabel(),
              newLabel,
              "label");
    }
}
