package com.jforex.programming.order.process.option;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.command.SetOpenPriceCommand;

public interface OpenPriceOption extends CommonOption<OpenPriceOption> {

    public OpenPriceOption doOnSetOpenPriceReject(Consumer<IOrder> rejectAction);

    public OpenPriceOption doOnSetOpenPrice(Consumer<IOrder> doneAction);

    public SetOpenPriceCommand build();
}
