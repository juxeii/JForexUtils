package com.jforex.programming.order.process.option;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.process.SetTPProcess;

public interface TPOption extends CommonOption<TPOption> {

    public TPOption onTPReject(Consumer<IOrder> rejectAction);

    public TPOption onTPChange(Consumer<IOrder> doneAction);

    public SetTPProcess build();
}