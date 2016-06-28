package com.jforex.programming.quote.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;
import com.jforex.programming.quote.BarQuoteFilter;
import com.jforex.programming.test.common.InstrumentUtilForTest;

public class BarQuoteFilterTest extends InstrumentUtilForTest {

    private BarQuoteFilter barQuoteFilter;

    private final Period period = Period.FIVE_MINS;
    private final OfferSide offerSide = OfferSide.ASK;

    @Before
    public void setUp() {
        barQuoteFilter = BarQuoteFilter
                .forInstrument(instrumentEURUSD)
                .period(period)
                .offerSide(offerSide);
    }

    @Test
    public void assertValues() {
        assertThat(barQuoteFilter.instrument(), equalTo(instrumentEURUSD));
        assertThat(barQuoteFilter.period(), equalTo(period));
        assertThat(barQuoteFilter.offerSide(), equalTo(offerSide));
    }
}
