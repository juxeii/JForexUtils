package com.jforex.programming.quote.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.jforex.programming.quote.TickQuote;
import com.jforex.programming.test.common.QuoteProviderForTest;

public class TickQuoteTest extends QuoteProviderForTest {

    private final TickQuote tickQuote = new TickQuote(instrumentEURUSD, tickEURUSD);

    @Test
    public void allAccessorsAreCorrect() {
        assertThat(tickQuote.instrument(), equalTo(instrumentEURUSD));
        assertThat(tickQuote.tick(), equalTo(tickEURUSD));
    }

    @Test
    public void isEqualsContractOK() {
        testEqualsContract(tickQuote);
    }
}
