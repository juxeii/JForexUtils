package com.jforex.programming.order.process.option;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.process.CommonBuilder;

public interface LabelOption extends CommonOption {

    public CommonBuilder onLabelReject(Consumer<IOrder> rejectAction);

    public CommonBuilder onLabelChange(Consumer<IOrder> doneAction);
}