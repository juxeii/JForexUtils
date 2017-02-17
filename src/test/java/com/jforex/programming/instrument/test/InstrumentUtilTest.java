package com.jforex.programming.instrument.test;

import static com.jforex.programming.instrument.InstrumentUtil.baseCurrencyName;
import static com.jforex.programming.instrument.InstrumentUtil.baseJavaCurrency;
import static com.jforex.programming.instrument.InstrumentUtil.isPricePipDivisible;
import static com.jforex.programming.instrument.InstrumentUtil.nameFromCurrencies;
import static com.jforex.programming.instrument.InstrumentUtil.numberOfDigits;
import static com.jforex.programming.instrument.InstrumentUtil.quoteCurrencyName;
import static com.jforex.programming.instrument.InstrumentUtil.quoteJavaCurrency;
import static com.jforex.programming.instrument.InstrumentUtil.scalePipsToPrice;
import static com.jforex.programming.instrument.InstrumentUtil.scalePriceToPips;
import static com.jforex.programming.instrument.InstrumentUtil.toStringNoSeparator;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.dukascopy.api.ICurrency;
import com.dukascopy.api.OfferSide;
import com.jforex.programming.currency.CurrencyCode;
import com.jforex.programming.instrument.InstrumentUtil;
import com.jforex.programming.math.CalculationUtil;
import com.jforex.programming.math.MathUtil;
import com.jforex.programming.test.common.QuoteProviderForTest;

public class InstrumentUtilTest extends QuoteProviderForTest {

    private InstrumentUtil instrumentUtil;

    @Mock
    private CalculationUtil calculationUtilMock;

    @Before
    public void setUp() {
        setUpMocks();

        instrumentUtil = new InstrumentUtil(instrumentEURUSD,
                                            tickQuoteHandlerMock,
                                            barQuoteHandlerMock,
                                            calculationUtilMock);
    }

    private void setUpMocks() {
        setTickExpectations(tickQuoteEURUSD);
    }

    @Test
    public void testTickReturnsTickMock() {
        assertThat(instrumentUtil.tickQuote(), equalTo(tickEURUSD));
    }

    @Test
    public void testAskReturnsAskQuote() {
        assertThat(instrumentUtil.askQuote(), equalTo(askEURUSD));
    }

    @Test
    public void testBidReturnsBidQuote() {
        assertThat(instrumentUtil.bidQuote(), equalTo(bidEURUSD));
    }

    @Test
    public void barQuoteReturnsQuoteFromQuoteProvider() {
        when(barQuoteHandlerMock.bar(askBarEURUSDParams)).thenReturn(askBarEURUSD);

        assertThat(instrumentUtil.barQuote(askBarEURUSDParams), equalTo(askBarEURUSD));
    }

    @Test
    public void testSpreadInPipsCalculationIsCorrect() {
        final double expectedSpreadInPips = 1.0 / instrumentEURUSD.getPipValue()
                * MathUtil.roundPrice(askEURUSD - bidEURUSD, instrumentEURUSD);

        assertThat(instrumentUtil.spreadInPips(), equalTo(expectedSpreadInPips));
    }

    @Test
    public void testSpreadCalculationIsCorrect() {
        final double expectedSpread = MathUtil.roundPrice(askEURUSD - bidEURUSD, instrumentEURUSD);

        assertThat(instrumentUtil.spread(), equalTo(expectedSpread));
    }

    @Test
    public void scalePipsIsCorrect() {
        assertThat(instrumentUtil.scalePipsToPrice(20.5), equalTo(0.00205));
        assertThat(instrumentUtil.scalePipsToPrice(7.9), equalTo(0.00079));
    }

    @Test
    public void addPipsToPriceIsCorrect() {
        assertThat(instrumentUtil.addPipsToPrice(askEURUSD, 20.55),
                   closeTo(askEURUSD + 0.00206, 0.001));
        assertThat(instrumentUtil.addPipsToPrice(askGBPAUD, -7.41),
                   closeTo(askGBPAUD - 0.00074, 0.001));
    }

    @Test
    public void pipDistanceOfPricesIsCorrect() {
        assertThat(instrumentUtil.pipDistanceOfPrices(askEURUSD, bidEURUSD),
                   equalTo(MathUtil.roundPips((askEURUSD - bidEURUSD) / instrumentEURUSD.getPipValue())));
    }

    @Test
    public void isPricePipDivisibleIsCorrect() {
        assertTrue(instrumentUtil.isPricePipDivisible(1.12345));
        assertTrue(instrumentUtil.isPricePipDivisible(133.243));
    }

    @Test
    public void convertAmountForSameInstrumentCallsCalculcationUtilCorrect() {
        final double amount = 213456.78;
        when(calculationUtilMock.convertAmount(amount,
                                               currencyEUR,
                                               currencyEUR,
                                               OfferSide.ASK))
                                                   .thenReturn(amount);

        final double convertedAmount = instrumentUtil.convertAmount(amount,
                                                                    instrumentEURUSD,
                                                                    OfferSide.ASK);

        verify(calculationUtilMock).convertAmount(amount,
                                                  currencyEUR,
                                                  currencyEUR,
                                                  OfferSide.ASK);
        assertThat(convertedAmount, equalTo(amount));
    }

    @Test
    public void convertAmountForOtherInstrumentCallsCalculcationUtilCorrect() {
        final double amount = 213456.78;
        when(calculationUtilMock.convertAmount(amount,
                                               currencyEUR,
                                               currencyAUD,
                                               OfferSide.ASK))
                                                   .thenReturn(amount);

        final double convertedAmount = instrumentUtil.convertAmount(amount,
                                                                    instrumentAUDJPY,
                                                                    OfferSide.ASK);

        verify(calculationUtilMock).convertAmount(amount,
                                                  currencyEUR,
                                                  currencyAUD,
                                                  OfferSide.ASK);
        assertThat(convertedAmount, equalTo(amount));
    }

    @Test
    public void pipValueInCurrencyForSameCurrencyCallsCalculcationUtilCorrect() {
        final double amount = 213456.78;
        final double expectedPipValue = 1.23456;
        when(calculationUtilMock.pipValueInCurrency(amount,
                                                    instrumentEURUSD,
                                                    currencyUSD,
                                                    OfferSide.ASK))
                                                        .thenReturn(expectedPipValue);

        final double pipValue = instrumentUtil.pipValueInCurrency(amount,
                                                                  currencyUSD,
                                                                  OfferSide.ASK);

        verify(calculationUtilMock).pipValueInCurrency(amount,
                                                       instrumentEURUSD,
                                                       currencyUSD,
                                                       OfferSide.ASK);
        assertThat(pipValue, equalTo(expectedPipValue));
    }

    @Test
    public void pipValueInCurrencyForOtherCurrencyCallsCalculcationUtilCorrect() {
        final double amount = 213456.78;
        final double expectedPipValue = 1.23456;
        when(calculationUtilMock.pipValueInCurrency(amount,
                                                    instrumentEURUSD,
                                                    currencyJPY,
                                                    OfferSide.ASK))
                                                        .thenReturn(expectedPipValue);

        final double pipValue = instrumentUtil.pipValueInCurrency(amount,
                                                                  currencyJPY,
                                                                  OfferSide.ASK);

        verify(calculationUtilMock).pipValueInCurrency(amount,
                                                       instrumentEURUSD,
                                                       currencyJPY,
                                                       OfferSide.ASK);
        assertThat(pipValue, equalTo(expectedPipValue));
    }

    @Test
    public void testBaseJavaCurrencyIsCorrect() {
        assertThat(instrumentUtil.baseJavaCurrency(), equalTo(currencyEUR.getJavaCurrency()));
    }

    @Test
    public void testQuoteJavaCurrencyIsCorrect() {
        assertThat(instrumentUtil.quoteJavaCurrency(), equalTo(currencyUSD.getJavaCurrency()));
    }

    @Test
    public void testNumberOfDigitsIsCorrect() {
        assertThat(instrumentUtil.numberOfDigits(),
                   equalTo(InstrumentUtil.numberOfDigits(instrumentEURUSD)));
    }

    @Test
    public void testToStringNoSeparatorIsCorrect() {
        assertThat(instrumentUtil.toStringNoSeparator(),
                   equalTo(InstrumentUtil.toStringNoSeparator(instrumentEURUSD)));
    }

    @Test
    public void testToStringIsCorrect() {
        assertThat(instrumentUtil.toString(),
                   equalTo(InstrumentUtil.nameFromCurrencies(currencyEUR, currencyUSD)));
    }

    @Test
    public void testCurrenciesIsCorrect() {
        final Set<ICurrency> currencies = instrumentUtil.currencies();

        assertThat(currencies.size(), equalTo(2));
        assertTrue(currencies.contains(currencyEUR));
        assertTrue(currencies.contains(currencyUSD));
    }

    @Test
    public void testContainsCurrencyIsCorrect() {
        assertTrue(instrumentUtil.containsCurrency(currencyEUR));
        assertTrue(instrumentUtil.containsCurrency(currencyUSD));
        assertFalse(instrumentUtil.containsCurrency(currencyJPY));
    }

    @Test
    public void testContainsCurrencyCodeIsCorrect() {
        assertTrue(instrumentUtil.containsCurrencyCode(CurrencyCode.EUR));
        assertTrue(instrumentUtil.containsCurrencyCode(CurrencyCode.USD));
    }

    @Test
    public void testNumberOfDigitsIsFiveForNonJPYInstruments() {
        assertThat(numberOfDigits(instrumentEURUSD),
                   equalTo(noOfDigitsNonJPYInstrument));
    }

    @Test
    public void scalePipsToPriceIsCorrectForNonJPYInstruments() {
        assertThat(scalePipsToPrice(instrumentEURUSD, 20.5),
                   equalTo(0.00205));
        assertThat(scalePipsToPrice(instrumentGBPAUD, 7.9),
                   equalTo(0.00079));
    }

    @Test
    public void scalePipsToPriceIsCorrectForJPYInstruments() {
        assertThat(scalePipsToPrice(instrumentEURJPY, 13.45),
                   equalTo(0.135));
        assertThat(scalePipsToPrice(instrumentUSDJPY, 4.78),
                   equalTo(0.048));
    }

    @Test
    public void staticPipDistanceOfPricesIsCorrect() {
        assertThat(InstrumentUtil.pipDistanceOfPrices(instrumentEURUSD, askEURUSD, bidEURUSD),
                   equalTo(MathUtil.roundPips((askEURUSD - bidEURUSD) / instrumentEURUSD.getPipValue())));
    }

    @Test
    public void scalePriceToPipsIsCorrect() {
        final double price = 0.00125;

        assertThat(scalePriceToPips(instrumentEURUSD, price),
                   equalTo(12.5));
    }

    @Test
    public void scalePriceToPipsForJPYIsCorrect() {
        final double price = 0.031;

        assertThat(scalePriceToPips(instrumentEURJPY, price),
                   equalTo(3.1));
    }

    @Test
    public void testRoundedPriceIsPipDivisible() {
        assertTrue(isPricePipDivisible(instrumentEURUSD, 1.12345));
        assertTrue(isPricePipDivisible(instrumentUSDJPY, 133.243));
    }

    @Test
    public void testNonRoundedPriceIsNotPipDivisible() {
        assertFalse(isPricePipDivisible(instrumentEURUSD, 1.123455));
        assertFalse(isPricePipDivisible(instrumentUSDJPY, 133.2432));
    }

    @Test
    public void testNumberOfDigitsIsThreeForJPYInstruments() {
        assertThat(numberOfDigits(instrumentUSDJPY),
                   equalTo(noOfDigitsJPYInstrument));
    }

    @Test
    public void testToStringNoSeparator() {
        assertThat(toStringNoSeparator(instrumentUSDJPY), equalTo("USDJPY"));
    }

    @Test
    public void testBaseJavaCurrency() {
        assertThat(baseJavaCurrency(instrumentEURUSD), equalTo(currencyEUR.getJavaCurrency()));
    }

    @Test
    public void testQuoteJavaCurrency() {
        assertThat(quoteJavaCurrency(instrumentEURUSD), equalTo(currencyUSD.getJavaCurrency()));
    }

    @Test
    public void baseCurrencyNameIsCorrect() {
        assertThat(baseCurrencyName(instrumentEURUSD), equalTo("EUR"));
    }

    @Test
    public void quoteCurrencyNameIsCorrect() {
        assertThat(quoteCurrencyName(instrumentEURUSD), equalTo("USD"));
    }

    @Test
    public void testNameFromCurrencies() {
        assertThat(nameFromCurrencies(currencyEUR, currencyEUR), equalTo("EUR/EUR"));
        assertThat(nameFromCurrencies(currencyEUR, currencyUSD), equalTo("EUR/USD"));
        assertThat(nameFromCurrencies(currencyUSD, currencyEUR), equalTo("USD/EUR"));
    }
}
