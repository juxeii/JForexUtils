package com.jforex.programming.order.process.option;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;

public interface TPOption extends CommonOption {

    public TPOption onTPReject(Consumer<IOrder> rejectAction);

    public TPOption onTPChange(Consumer<IOrder> doneAction);
}