package com.jforex.programming.order.process.option;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.process.CommonBuilder;

public interface OpenPriceOption extends CommonOption {

    public CommonBuilder onOpenPriceReject(Consumer<IOrder> rejectAction);

    public CommonBuilder onOpenPriceChange(Consumer<IOrder> doneAction);
}
