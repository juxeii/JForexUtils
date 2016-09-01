package com.jforex.programming.order.process.option;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.process.CommonBuilder;

public interface GTTOption extends CommonOption {

    public CommonBuilder onGTTReject(Consumer<IOrder> rejectAction);

    public CommonBuilder onGTTChange(Consumer<IOrder> doneAction);
}
