package com.jforex.programming.math.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.jforex.programming.math.PipValueBuilder;
import com.jforex.programming.test.common.CurrencyUtilForTest;

import com.dukascopy.api.OfferSide;

public class PipValueBuilderTest extends CurrencyUtilForTest {

    private final double amount = 0.12;

    private final double pipValue(final PipValueBuilder pipValueBuilder) {
        assertThat(pipValueBuilder.targetCurrency(), equalTo(currencyEUR));
        assertThat(pipValueBuilder.instrument(), equalTo(instrumentEURUSD));
        assertThat(pipValueBuilder.amount(), equalTo(amount));

        return 0;
    }

    @Test
    public void assertValues() {
        new PipValueBuilder(this::pipValue)
                .pipValueInCurrency(currencyEUR)
                .ofInstrument(instrumentEURUSD)
                .withAmount(amount)
                .andOfferSide(OfferSide.ASK);
    }
}
