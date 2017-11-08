package com.jforex.programming.instrument.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.jforex.programming.instrument.CrossInstrument;
import com.jforex.programming.instrument.FxRate;
import com.jforex.programming.test.common.CurrencyUtilForTest;

public class CrossInstrumentTest extends CurrencyUtilForTest {

    private CrossInstrument crossInstrumentA;
    private CrossInstrument crossInstrumentB;
    private CrossInstrument crossInstrumentC;
    private CrossInstrument crossInstrumentD;
    private CrossInstrument crossInstrumentE;

    @Before
    public void setUp() {
        crossInstrumentA = new CrossInstrument(instrumentEURUSD, instrumentGBPUSD);
        crossInstrumentB = new CrossInstrument(instrumentEURGBP, instrumentGBPUSD);
        crossInstrumentC = new CrossInstrument(instrumentEURJPY, instrumentGBPJPY);
        crossInstrumentD = new CrossInstrument(instrumentEURGBP, instrumentGBPJPY);
        crossInstrumentE = new CrossInstrument(instrumentEURGBP, instrumentEURUSD);
    }

    @Test
    public void instrumentIsCorrect() {
        assertThat(crossInstrumentA.get(), equalTo(instrumentEURGBP));
        assertThat(crossInstrumentB.get(), equalTo(instrumentEURUSD));
        assertThat(crossInstrumentC.get(), equalTo(instrumentEURGBP));
        assertThat(crossInstrumentD.get(), equalTo(instrumentEURJPY));
        assertThat(crossInstrumentE.get(), equalTo(instrumentGBPUSD));
    }

    @Test
    public void crossCurrencyIsCorrect() {
        assertThat(crossInstrumentA.crossCurrency(), equalTo(currencyUSD));
        assertThat(crossInstrumentB.crossCurrency(), equalTo(currencyGBP));
        assertThat(crossInstrumentC.crossCurrency(), equalTo(currencyJPY));
        assertThat(crossInstrumentD.crossCurrency(), equalTo(currencyGBP));
        assertThat(crossInstrumentE.crossCurrency(), equalTo(currencyEUR));
    }

    @Test
    public void crossRateIsCorrect() {
        FxRate rateA = new FxRate(1.15863, instrumentEURUSD);
        FxRate rateB = new FxRate(1.31044, instrumentGBPUSD);
        assertThat(crossInstrumentA.rate(rateA, rateB).value(),
                   equalTo(0.88415));
        assertThat(crossInstrumentA.rate(rateA, rateB).instrument(),
                   equalTo(instrumentEURGBP));

        rateA = new FxRate(0.88415, instrumentEURGBP);
        rateB = new FxRate(1.31044, instrumentGBPUSD);
        assertThat(crossInstrumentB.rate(rateA, rateB).value(),
                   equalTo(1.15863));

        rateA = new FxRate(131.452, instrumentEURJPY);
        rateB = new FxRate(148.653, instrumentGBPJPY);
        assertThat(crossInstrumentC.rate(rateA, rateB).value(),
                   equalTo(0.88429));

        rateA = new FxRate(0.88429, instrumentEURGBP);
        rateB = new FxRate(148.653, instrumentGBPJPY);
        assertThat(crossInstrumentD.rate(rateA, rateB).value(),
                   equalTo(131.452));

        rateA = new FxRate(0.86347, instrumentEURGBP);
        rateB = new FxRate(1.07829, instrumentEURUSD);
        assertThat(crossInstrumentE.rate(rateA, rateB).value(),
                   equalTo(1.24879));
    }
}
