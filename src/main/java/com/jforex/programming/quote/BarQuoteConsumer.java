package com.jforex.programming.quote;

@FunctionalInterface
public interface BarQuoteConsumer {

    public void onBarQuote(BarQuote barQuote);
}
