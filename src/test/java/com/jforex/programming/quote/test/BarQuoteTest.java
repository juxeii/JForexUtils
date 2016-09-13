package com.jforex.programming.quote.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.jforex.programming.quote.BarQuote;
import com.jforex.programming.test.common.QuoteProviderForTest;

public class BarQuoteTest extends QuoteProviderForTest {

    private final BarQuote barQuote = new BarQuote(askBarEURUSD, askBarEURUSDParams);

    @Test
    public void allAccessorsAreCorrect() {
        assertThat(barQuote.bar(), equalTo(askBarEURUSD));
        assertThat(barQuote.barParams(), equalTo(askBarEURUSDParams));
        assertThat(barQuote.instrument(), equalTo(askBarEURUSDParams.instrument()));
        assertThat(barQuote.period(), equalTo(askBarEURUSDParams.period()));
        assertThat(barQuote.offerSide(), equalTo(askBarEURUSDParams.offerSide()));
    }

    @Test
    public void isEqualsContractOK() {
        testEqualsContract(barQuote);
    }
}
