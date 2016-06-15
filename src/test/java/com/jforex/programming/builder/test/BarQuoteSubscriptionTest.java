package com.jforex.programming.builder.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.dukascopy.api.Instrument;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;
import com.google.common.collect.Sets;
import com.jforex.programming.builder.BarQuoteSubscription;
import com.jforex.programming.test.common.InstrumentUtilForTest;

public class BarQuoteSubscriptionTest extends InstrumentUtilForTest {

    private BarQuoteSubscription barQuoteSubscription;

    private final Set<Instrument> instruments = Sets.newHashSet(instrumentEURUSD, instrumentAUDUSD);
    private final Period period = Period.FIVE_MINS;
    private final OfferSide offerSide = OfferSide.ASK;

    @Before
    public void setUp() {
        barQuoteSubscription = BarQuoteSubscription
                .forInstruments(instruments)
                .period(period)
                .offerSide(offerSide);
    }

    @Test
    public void assertValues() {
        assertThat(barQuoteSubscription.instruments(), equalTo(instruments));
        assertThat(barQuoteSubscription.period(), equalTo(period));
        assertThat(barQuoteSubscription.offerSide(), equalTo(offerSide));
    }
}
