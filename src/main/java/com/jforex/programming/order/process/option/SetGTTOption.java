package com.jforex.programming.order.process.option;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.command.SetGTTCommand;

public interface SetGTTOption extends CommonOption<SetGTTOption> {

    public SetGTTOption doOnSetGTTReject(Consumer<IOrder> rejectAction);

    public SetGTTOption doOnSetGTT(Consumer<IOrder> doneAction);

    public SetGTTCommand build();
}
