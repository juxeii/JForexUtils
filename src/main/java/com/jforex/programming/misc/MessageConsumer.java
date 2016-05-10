package com.jforex.programming.misc;

import com.dukascopy.api.IMessage;

@FunctionalInterface
public interface MessageConsumer {

    public void onMessage(IMessage message);
}
