package com.jforex.programming.instrument.test;

import static com.jforex.programming.instrument.InstrumentUtil.baseJavaCurrency;
import static com.jforex.programming.instrument.InstrumentUtil.nameFromCurrencies;
import static com.jforex.programming.instrument.InstrumentUtil.numberOfDigits;
import static com.jforex.programming.instrument.InstrumentUtil.quoteJavaCurrency;
import static com.jforex.programming.instrument.InstrumentUtil.toStringNoSeparator;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.dukascopy.api.ICurrency;
import com.jforex.programming.currency.CurrencyCode;
import com.jforex.programming.instrument.InstrumentUtil;
import com.jforex.programming.math.CalculationUtil;
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
    public void testSpreadCalculationIsCorrect() {
        final double spreadOfCalculcationUtil = 12.3;

        when(calculationUtilMock.pipDistance(instrumentEURUSD, askEURUSD, bidEURUSD))
            .thenReturn(spreadOfCalculcationUtil);

        assertThat(instrumentUtil.spread(), equalTo(spreadOfCalculcationUtil));
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
    public void testNameFromCurrencies() {
        assertThat(nameFromCurrencies(currencyEUR, currencyEUR), equalTo("EUR/EUR"));
        assertThat(nameFromCurrencies(currencyEUR, currencyUSD), equalTo("EUR/USD"));
        assertThat(nameFromCurrencies(currencyUSD, currencyEUR), equalTo("USD/EUR"));
    }
}
