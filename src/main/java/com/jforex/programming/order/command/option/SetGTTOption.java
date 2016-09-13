package com.jforex.programming.order.command.option;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.command.SetGTTCommand;

public interface SetGTTOption extends CommonOption<SetGTTOption> {

    public SetGTTOption doOnSetGTTReject(Consumer<IOrder> rejectConsumer);

    public SetGTTOption doOnSetGTT(Consumer<IOrder> doneConsumer);

    public SetGTTCommand build();
}
