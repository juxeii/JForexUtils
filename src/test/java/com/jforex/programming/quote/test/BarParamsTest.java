package com.jforex.programming.quote.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;
import com.jforex.programming.quote.BarParams;
import com.jforex.programming.test.common.InstrumentUtilForTest;

public class BarParamsTest extends InstrumentUtilForTest {

    private final BarParams barParams = BarParams
        .forInstrument(instrumentEURUSD)
        .period(Period.DAILY)
        .offerSide(OfferSide.ASK);

    @Test
    public void assertValues() {
        assertThat(barParams.instrument(), equalTo(instrumentEURUSD));
        assertThat(barParams.period(), equalTo(Period.DAILY));
        assertThat(barParams.offerSide(), equalTo(OfferSide.ASK));
    }

    @Test
    public void isEqualsContractOK() {
        testEqualsContract(barParams);
    }
}
