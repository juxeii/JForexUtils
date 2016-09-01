package com.jforex.programming.order.process.option;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;

public interface SLOption extends CommonOption {

    public SLOption onSLReject(Consumer<IOrder> rejectAction);

    public SLOption onSLChange(Consumer<IOrder> doneAction);
}