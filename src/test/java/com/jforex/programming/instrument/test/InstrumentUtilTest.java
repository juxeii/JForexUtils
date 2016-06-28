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

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.dukascopy.api.IBar;
import com.dukascopy.api.ITick;
import com.jforex.programming.instrument.InstrumentUtil;
import com.jforex.programming.math.CalculationUtil;
import com.jforex.programming.quote.BarQuoteHandler;
import com.jforex.programming.quote.TickQuoteHandler;
import com.jforex.programming.test.common.CurrencyUtilForTest;
import com.jforex.programming.test.common.QuoteProviderForTest;
import com.jforex.programming.test.fakes.ITickForTest;

public class InstrumentUtilTest extends CurrencyUtilForTest {

    private InstrumentUtil instrumentUtil;

    @Mock
    private TickQuoteHandler tickQuoteProviderMock;
    @Mock
    private BarQuoteHandler barQuoteProviderMock;
    @Mock
    private IBar askBar;
    @Mock
    private IBar bidBar;
    private final ITick tickEURUSD = new ITickForTest(bidEURUSD, askEURUSD);

    @Before
    public void setUp() {
        initCommonTestFramework();
        setUpMocks();

        instrumentUtil = new InstrumentUtil(instrumentEURUSD, tickQuoteProviderMock, barQuoteProviderMock);
    }

    private void setUpMocks() {
        QuoteProviderForTest.setQuoteExpectations(tickQuoteProviderMock, instrumentEURUSD, bidEURUSD, askEURUSD);
    }

    @Test
    public void testTickReturnsTickMock() {
        when(tickQuoteProviderMock.tick(instrumentEURUSD)).thenReturn(tickEURUSD);

        assertThat(instrumentUtil.tick(), equalTo(tickEURUSD));
    }

    @Test
    public void testAskReturnsAskQuote() {
        assertThat(instrumentUtil.ask(), equalTo(askEURUSD));
    }

    @Test
    public void testBidReturnsBidQuote() {
        assertThat(instrumentUtil.bid(), equalTo(bidEURUSD));
    }

    @Test
    public void testSpreadCalculationIsCorrect() {
        assertThat(instrumentUtil.spread(),
                   equalTo(CalculationUtil
                           .pipDistanceFrom(askEURUSD)
                           .to(bidEURUSD)
                           .forInstrument(instrumentEURUSD)));
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
        assertThat(instrumentUtil.numberOfDigits(), equalTo(InstrumentUtil.numberOfDigits(instrumentEURUSD)));
    }

    @Test
    public void testToStringNoSeparatorIsCorrect() {
        assertThat(instrumentUtil.toStringNoSeparator(), equalTo(InstrumentUtil.toStringNoSeparator(instrumentEURUSD)));
    }

    @Test
    public void testContainsCurrencyIsCorrect() {
        assertTrue(instrumentUtil.containsCurrency(currencyEUR));
        assertTrue(instrumentUtil.containsCurrency(currencyUSD));
        assertFalse(instrumentUtil.containsCurrency(currencyJPY));
    }

    @Test
    public void testContainsCurrencyForNameIsCorrect() {
        assertTrue(instrumentUtil.containsCurrencyCode(currencyNameEUR));
        assertTrue(instrumentUtil.containsCurrencyCode(currencyNameUSD));
        assertFalse(instrumentUtil.containsCurrencyCode(invalidEmptyCurrencyName));
        assertFalse(instrumentUtil.containsCurrencyCode(invalidCurrencyName));
        assertFalse(instrumentUtil.containsCurrencyCode(invalidLowerCaseCurrencyName));
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
