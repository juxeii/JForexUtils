package com.jforex.programming.builder.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.jforex.programming.builder.ConversionBuilder;
import com.jforex.programming.test.common.CurrencyUtilForTest;

import com.dukascopy.api.OfferSide;

public class ConversionBuilderTest extends CurrencyUtilForTest {

    private final double amount = 0.12;

    private final double convert(final ConversionBuilder conversionBuilder) {
        assertThat(conversionBuilder.amount(), equalTo(amount));
        assertThat(conversionBuilder.sourceCurrency(), equalTo(currencyEUR));
        assertThat(conversionBuilder.targetCurrency(), equalTo(currencyAUD));
        assertThat(conversionBuilder.offerSide(), equalTo(OfferSide.ASK));

        return 0;
    }

    @Test
    public void assertValues() {
        new ConversionBuilder(this::convert)
                .convertAmount(amount)
                .fromCurrency(currencyEUR)
                .toCurrency(currencyAUD)
                .forOfferSide(OfferSide.ASK);
    }
}
