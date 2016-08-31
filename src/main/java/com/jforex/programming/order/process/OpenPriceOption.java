package com.jforex.programming.order.process;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;

public interface OpenPriceOption extends CommonOption {

    public CommonBuilder onOpenPriceReject(Consumer<IOrder> rejectAction);

    public CommonBuilder onOpenPriceChange(Consumer<IOrder> doneAction);
}
