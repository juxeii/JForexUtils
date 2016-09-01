package com.jforex.programming.order.process.option;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;

public interface OpenPriceOption extends CommonOption {

    public OpenPriceOption onOpenPriceReject(Consumer<IOrder> rejectAction);

    public OpenPriceOption onOpenPriceChange(Consumer<IOrder> doneAction);
}
