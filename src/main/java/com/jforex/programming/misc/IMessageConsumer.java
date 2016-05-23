package com.jforex.programming.misc;

import com.dukascopy.api.IMessage;

@FunctionalInterface
public interface IMessageConsumer {

    public void onMessage(IMessage message);
}
