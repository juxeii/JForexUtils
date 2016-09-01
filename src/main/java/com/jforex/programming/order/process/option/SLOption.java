package com.jforex.programming.order.process.option;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.process.SetSLProcess;

public interface SLOption extends CommonOption<SLOption> {

    public SLOption onSLReject(Consumer<IOrder> rejectAction);

    public SLOption onSLChange(Consumer<IOrder> doneAction);

    public SetSLProcess build();
}