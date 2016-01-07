package com.jforex.programming.quote;

@FunctionalInterface
public interface TickQuoteConsumer {

    abstract void onTickQuote(TickQuote tickQuote);
}
