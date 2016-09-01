package com.jforex.programming.order.process.option;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.process.CommonBuilder;

public interface TPOption extends CommonOption {

    public CommonBuilder onTPReject(Consumer<IOrder> rejectAction);

    public CommonBuilder onTPChange(Consumer<IOrder> doneAction);
}