package com.jforex.programming.order.command.option;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.command.SetOpenPriceCommand;

public interface SetOpenPriceOption extends CommonOption<SetOpenPriceOption> {

    public SetOpenPriceOption doOnSetOpenPriceReject(Consumer<IOrder> rejectAction);

    public SetOpenPriceOption doOnSetOpenPrice(Consumer<IOrder> doneAction);

    public SetOpenPriceCommand build();
}
