package com.jforex.programming.misc;

import com.dukascopy.api.IMessage;

@FunctionalInterface
public interface MessageConsumer {

    abstract void onMessage(IMessage message);
}
