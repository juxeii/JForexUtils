package com.jforex.programming.math.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.jforex.programming.math.PipDistanceBuilder;
import com.jforex.programming.test.common.CurrencyUtilForTest;

public class PipDistanceBuilderTest extends CurrencyUtilForTest {

    private final double fromPrice = askEURUSD;
    private final double toPrice = bidEURUSD;

    private final double pipDistance(final PipDistanceBuilder pipDistanceBuilder) {
        assertThat(pipDistanceBuilder.priceFrom(), equalTo(fromPrice));
        assertThat(pipDistanceBuilder.priceTo(), equalTo(toPrice));
        assertThat(pipDistanceBuilder.instrument(), equalTo(instrumentEURUSD));

        return 0;
    }

    @Test
    public void assertValues() {
        new PipDistanceBuilder(this::pipDistance)
                .pipDistanceFrom(fromPrice)
                .to(toPrice)
                .forInstrument(instrumentEURUSD);
    }
}
