package com.jforex.programming.order.process.option;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.process.SetGTTProcess;

public interface GTTOption extends CommonOption<GTTOption> {

    public GTTOption onGTTReject(Consumer<IOrder> rejectAction);

    public GTTOption onGTTChange(Consumer<IOrder> doneAction);

    public SetGTTProcess build();
}
