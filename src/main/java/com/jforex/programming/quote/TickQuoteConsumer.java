package com.jforex.programming.quote;

@FunctionalInterface
public interface TickQuoteConsumer {

    public void onTickQuote(TickQuote tickQuote);
}
