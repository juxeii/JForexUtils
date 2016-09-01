package com.jforex.programming.order.process.option;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.process.SetOpenPriceProcess;

public interface OpenPriceOption extends CommonOption<OpenPriceOption> {

    public OpenPriceOption onOpenPriceReject(Consumer<IOrder> rejectAction);

    public OpenPriceOption onOpenPriceChange(Consumer<IOrder> doneAction);

    public SetOpenPriceProcess build();
}
