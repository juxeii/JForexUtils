package com.jforex.programming.order.process.option;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;

public interface GTTOption extends CommonOption {

    public GTTOption onGTTReject(Consumer<IOrder> rejectAction);

    public GTTOption onGTTChange(Consumer<IOrder> doneAction);
}
