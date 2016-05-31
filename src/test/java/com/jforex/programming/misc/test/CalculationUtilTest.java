package com.jforex.programming.misc.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.dukascopy.api.Instrument;
import com.dukascopy.api.OfferSide;
import com.jforex.programming.misc.CalculationUtil;
import com.jforex.programming.misc.MathUtil;
import com.jforex.programming.quote.TickQuoteProvider;
import com.jforex.programming.test.common.CurrencyUtilForTest;
import com.jforex.programming.test.common.QuoteProviderForTest;

public class CalculationUtilTest extends CurrencyUtilForTest {

    private CalculationUtil calculationUtil;

    @Mock
    public TickQuoteProvider tickQuoteProviderMock;

    @Before
    public void setUp() {
        initCommonTestFramework();
        setUpMocks();

        calculationUtil = new CalculationUtil(tickQuoteProviderMock);
    }

    private void setUpMocks() {
        QuoteProviderForTest.setQuoteExpectations(tickQuoteProviderMock, instrumentEURUSD, bidEURUSD, askEURUSD);
        QuoteProviderForTest.setQuoteExpectations(tickQuoteProviderMock, instrumentUSDJPY, bidUSDJPY, askUSDJPY);
        QuoteProviderForTest.setQuoteExpectations(tickQuoteProviderMock, instrumentEURJPY, bidEURJPY, askEURJPY);
    }

    private void assertPipDistance(final Instrument instrument,
                                   final double priceA,
                                   final double priceB) {
        final double pipDistance = CalculationUtil
                .pipDistanceFrom(priceA)
                .to(priceB)
                .forInstrument(instrument);

        assertThat(pipDistance,
                   equalTo(MathUtil.roundPips((priceA - priceB) / instrument.getPipValue())));
    }

    private double convertedAmountForInvertedQuote(final double amount,
                                                   final double quote) {
        return MathUtil.roundAmount(amount / quote);
    }

    private double convertedAmountForQuote(final double amount,
                                           final double quote) {
        return MathUtil.roundAmount(amount * quote);
    }

    private double scaledAmount(final double amount) {
        return MathUtil.roundAmount(amount / platformSettings.baseAmount());
    }

    @Test
    public void testConvertAmountIsCorrectForEqualCurrencies() {
        final double convertedAmount = calculationUtil
                .convertAmount(213456.78)
                .fromCurrency(currencyEUR)
                .toCurrency(currencyEUR)
                .forOfferSide(OfferSide.ASK);

        assertThat(convertedAmount, equalTo(213456.78));
    }

    @Test
    public void testConvertAmountIsCorrectForTargetCurrencyIsBase() {
        final double amount = 2000.567;
        final double convertedAmount = calculationUtil
                .convertAmount(amount)
                .fromCurrency(currencyUSD)
                .toCurrency(currencyEUR)
                .forOfferSide(OfferSide.BID);

        assertThat(convertedAmount, equalTo(convertedAmountForInvertedQuote(amount, bidEURUSD)));
    }

    @Test
    public void testConvertAmountIsCorrectForTargetCurrencyIsQuote() {
        final double amount = 45678.89;
        final double convertedAmount = calculationUtil
                .convertAmount(amount)
                .fromCurrency(currencyUSD)
                .toCurrency(currencyJPY)
                .forOfferSide(OfferSide.BID);

        assertThat(convertedAmount, equalTo(convertedAmountForQuote(amount, bidUSDJPY)));
    }

    @Test
    public void testPipValueIsCorrectForEqualCurrencies() {
        final double amount = 456789.887;
        final double pipValue = calculationUtil
                .pipValueInCurrency(currencyUSD)
                .ofInstrument(instrumentEURUSD)
                .withAmount(amount)
                .andOfferSide(OfferSide.ASK);

        assertThat(pipValue, equalTo(MathUtil.roundAmount(amount * instrumentEURUSD.getPipValue())));
    }

    @Test
    public void testPipValueIsCorrectForTargetCurrencyIsBase() {
        final double amount = 5456789.887;
        final double pipValue = calculationUtil
                .pipValueInCurrency(currencyEUR)
                .ofInstrument(instrumentEURUSD)
                .withAmount(amount)
                .andOfferSide(OfferSide.ASK);

        assertThat(pipValue, equalTo(MathUtil.roundAmount(amount * instrumentEURUSD.getPipValue() / askEURUSD)));
    }

    @Test
    public void testPipValueIsCorrectForTargetCurrencyIsQuote() {
        final double amount = 456789.887;
        final double pipValue = calculationUtil
                .pipValueInCurrency(currencyJPY)
                .ofInstrument(instrumentEURUSD)
                .withAmount(amount)
                .andOfferSide(OfferSide.BID);

        assertThat(pipValue, equalTo(MathUtil.roundAmount(amount * instrumentEURUSD.getPipValue() * bidUSDJPY)));
    }

    @Test
    public void testScalePipsToInstrumentIsCorrectForNonJPYInstruments() {
        assertThat(CalculationUtil.scalePipsToInstrument(20.5, instrumentEURUSD), equalTo(0.00205));
        assertThat(CalculationUtil.scalePipsToInstrument(7.9, instrumentGBPAUD), equalTo(0.00079));
    }

    @Test
    public void testScalePipsToInstrumentIsCorrectForJPYInstruments() {
        assertThat(CalculationUtil.scalePipsToInstrument(13.45, instrumentEURJPY), equalTo(0.135));
        assertThat(CalculationUtil.scalePipsToInstrument(4.78, instrumentUSDJPY), equalTo(0.048));
    }

    @Test
    public void testAddPipsIsCorrectForNonJPYInstruments() {
        assertThat(CalculationUtil.addPips(instrumentEURUSD, askEURUSD, 20.55), equalTo(askEURUSD + 0.00206));
        assertThat(CalculationUtil.addPips(instrumentGBPAUD, askGBPAUD, -7.41), equalTo(askGBPAUD - 0.00074));
    }

    @Test
    public void testAddPipsIsCorrectForJPYInstruments() {
        assertThat(CalculationUtil.addPips(instrumentEURJPY, askEURJPY, 13.25), equalTo(askEURJPY + 0.133));
        assertThat(CalculationUtil.addPips(instrumentUSDJPY, askUSDJPY, -12.54), equalTo(askUSDJPY - 0.125));
    }

    @Test
    public void testPipDistanceIsCorrectForNonJPYInstruments() {
        assertPipDistance(instrumentEURUSD, askEURUSD, bidEURUSD);
        assertPipDistance(instrumentGBPAUD, askGBPAUD, bidGBPAUD);
    }

    @Test
    public void testPipDistanceIsCorrectForJPYInstruments() {
        assertPipDistance(instrumentEURJPY, askEURJPY, bidEURJPY);
        assertPipDistance(instrumentUSDJPY, askUSDJPY, bidUSDJPY);
    }

    @Test
    public void testRoundedPriceIsPipDivisible() {
        assertTrue(CalculationUtil.isPricePipDivisible(instrumentEURUSD, 1.12345));
        assertTrue(CalculationUtil.isPricePipDivisible(instrumentUSDJPY, 133.243));
    }

    @Test
    public void testNonRoundedPriceIsNotPipDivisible() {
        assertFalse(CalculationUtil.isPricePipDivisible(instrumentEURUSD, 1.123455));
        assertFalse(CalculationUtil.isPricePipDivisible(instrumentUSDJPY, 133.2432));
    }

    @Test
    public void testScaleAmountToPlatformIsCorrectForFractionalAmount() {
        final double amount = 213456.78;
        final double scaledAmount = CalculationUtil.scaleToPlatformAmount(amount);

        assertThat(scaledAmount, equalTo(scaledAmount(amount)));
    }

    @Test
    public void testScaleAmountToPlatformIsCorrectForNonFractionalAmount() {
        final double amount = 1000000;
        final double scaledAmount = CalculationUtil.scaleToPlatformAmount(amount);

        assertThat(scaledAmount, equalTo(scaledAmount(amount)));
    }
}
