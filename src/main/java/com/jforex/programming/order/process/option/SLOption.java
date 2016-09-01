package com.jforex.programming.order.process.option;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.process.CommonBuilder;

public interface SLOption extends CommonOption {

    public CommonBuilder onSLReject(Consumer<IOrder> rejectAction);

    public CommonBuilder onSLChange(Consumer<IOrder> doneAction);
}