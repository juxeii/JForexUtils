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
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.dukascopy.api.IBar;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Period;
import com.jforex.programming.instrument.InstrumentUtil;
import com.jforex.programming.misc.CalculationUtil;
import com.jforex.programming.quote.BarQuoteProvider;
import com.jforex.programming.quote.TickQuoteProvider;
import com.jforex.programming.test.common.CurrencyUtilForTest;
import com.jforex.programming.test.common.QuoteProviderForTest;
import com.jforex.programming.test.fakes.ITickForTest;

public class InstrumentUtilTest extends CurrencyUtilForTest {

    private InstrumentUtil instrumentUtil;

    @Mock private TickQuoteProvider tickQuoteProviderMock;
    @Mock private BarQuoteProvider barQuoteProviderMock;
    @Mock private IBar askBar;
    @Mock private IBar bidBar;
    private final ITick tickEURUSD = new ITickForTest(bidEURUSD, askEURUSD);
    private final Period testAskBarPeriod = Period.ONE_MIN;
    private final Period testBidBarPeriod = Period.FIFTEEN_MINS;

    @Before
    public void setUp() {
        initCommonTestFramework();
        setUpMocks();

        instrumentUtil = new InstrumentUtil(instrumentEURUSD, tickQuoteProviderMock, barQuoteProviderMock);
    }

    private void setUpMocks() {
        QuoteProviderForTest.setQuoteExpectations(tickQuoteProviderMock, instrumentEURUSD, bidEURUSD, askEURUSD);

        when(barQuoteProviderMock.askBar(instrumentEURUSD, testAskBarPeriod)).thenReturn(askBar);
        when(barQuoteProviderMock.bidBar(instrumentEURUSD, testBidBarPeriod)).thenReturn(bidBar);
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
    public void testAskBarReturnsAskBarQuote() {
        assertThat(instrumentUtil.askBar(testAskBarPeriod), equalTo(askBar));
    }

    @Test
    public void testBidBarReturnsBidBarQuote() {
        assertThat(instrumentUtil.bidBar(testBidBarPeriod), equalTo(bidBar));
    }

    @Test
    public void testSpreadCalculationIsCorrect() {
        assertThat(instrumentUtil.spread(),
                   equalTo(CalculationUtil.pipDistance(instrumentEURUSD, askEURUSD, bidEURUSD)));
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
        assertTrue(instrumentUtil.containsCurrency(currencyNameEUR));
        assertTrue(instrumentUtil.containsCurrency(currencyNameUSD));
        assertFalse(instrumentUtil.containsCurrency(invalidEmptyCurrencyName));
        assertFalse(instrumentUtil.containsCurrency(invalidCurrencyName));
        assertFalse(instrumentUtil.containsCurrency(invalidLowerCaseCurrencyName));
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
