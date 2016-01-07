package com.jforex.programming.quote;

@FunctionalInterface
public interface BarQuoteConsumer {

    abstract void onBarQuote(BarQuote barQuote);
}
