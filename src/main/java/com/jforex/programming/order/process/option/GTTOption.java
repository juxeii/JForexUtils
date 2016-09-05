package com.jforex.programming.order.process.option;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.command.SetGTTCommand;

public interface GTTOption extends CommonOption<GTTOption> {

    public GTTOption doOnSetGTTReject(Consumer<IOrder> rejectAction);

    public GTTOption doOnSetGTT(Consumer<IOrder> doneAction);

    public SetGTTCommand build();
}
