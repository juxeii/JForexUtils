package com.jforex.programming.math.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.dukascopy.api.OfferSide;
import com.jforex.programming.math.CalculationUtil;
import com.jforex.programming.math.MathUtil;
import com.jforex.programming.test.common.QuoteProviderForTest;

public class CalculationUtilTest extends QuoteProviderForTest {

    private CalculationUtil calculationUtil;

    @Before
    public void setUp() {
        setUpMocks();

        calculationUtil = new CalculationUtil(tickQuoteHandlerMock);
    }

    private void setUpMocks() {
        setTickExpectations(tickQuoteEURUSD);
        setTickExpectations(tickQuoteUSDJPY);
        setTickExpectations(tickQuoteEURJPY);
    }

    private double convertedAmountForInvertedQuote(final double amount,
                                                   final double quote) {
        return MathUtil.roundAmount(amount / quote);
    }

    private double convertedAmountForQuote(final double amount,
                                           final double quote) {
        return MathUtil.roundAmount(amount * quote);
    }

    @Test
    public void testConvertAmountIsCorrectForEqualCurrencies() {
        final double amount = 213456.78;
        final double convertedAmount = calculationUtil.convertAmount(amount,
                                                                     currencyEUR,
                                                                     currencyEUR,
                                                                     OfferSide.ASK);
        assertThat(convertedAmount, equalTo(amount));
    }

    @Test
    public void testConvertAmountIsCorrectForTargetCurrencyIsBase() {
        final double amount = 2000.567;
        final double convertedAmount = calculationUtil.convertAmount(amount,
                                                                     currencyUSD,
                                                                     currencyEUR,
                                                                     OfferSide.BID);
        assertThat(convertedAmount,
                   equalTo(convertedAmountForInvertedQuote(amount, bidEURUSD)));
    }

    @Test
    public void testConvertAmountIsCorrectForTargetCurrencyIsQuote() {
        final double amount = 45678.89;
        final double convertedAmount = calculationUtil.convertAmount(amount,
                                                                     currencyUSD,
                                                                     currencyJPY,
                                                                     OfferSide.BID);
        assertThat(convertedAmount,
                   equalTo(convertedAmountForQuote(amount, bidUSDJPY)));
    }

    @Test
    public void testPipValueIsCorrectForEqualCurrencies() {
        final double amount = 456789.887;
        final double pipValue = calculationUtil.pipValueInCurrency(amount,
                                                                   instrumentEURUSD,
                                                                   currencyUSD,
                                                                   OfferSide.ASK);
        assertThat(pipValue, equalTo(MathUtil
            .roundAmount(amount * instrumentEURUSD.getPipValue())));
    }

    @Test
    public void testPipValueIsCorrectForTargetCurrencyIsBase() {
        final double amount = 5456789.887;
        final double pipValue = calculationUtil.pipValueInCurrency(amount,
                                                                   instrumentEURUSD,
                                                                   currencyEUR,
                                                                   OfferSide.ASK);
        assertThat(pipValue, equalTo(MathUtil
            .roundAmount(amount * instrumentEURUSD.getPipValue() / askEURUSD)));
    }

    @Test
    public void testPipValueIsCorrectForTargetCurrencyIsQuote() {
        final double amount = 456789.887;
        final double pipValue = calculationUtil.pipValueInCurrency(amount,
                                                                   instrumentEURUSD,
                                                                   currencyJPY,
                                                                   OfferSide.BID);
        assertThat(pipValue, equalTo(MathUtil
            .roundAmount(amount * instrumentEURUSD.getPipValue() * bidUSDJPY)));
    }

    @Test
    public void calculateSLPriceWithPipsIsCorrectForBuyOrder() {
        when(tickQuoteHandlerMock.bid(instrumentEURUSD))
            .thenReturn(1.12345);

        final double slPrice = calculationUtil.slPriceForPips(buyOrderEURUSD, 13.5);

        assertThat(slPrice, equalTo(1.1221));
    }

    @Test
    public void calculateSLPriceWithPipsIsCorrectForSellOrder() {
        when(tickQuoteHandlerMock.ask(instrumentEURUSD))
            .thenReturn(1.12345);

        final double slPrice = calculationUtil.slPriceForPips(sellOrderEURUSD, 12.3);

        assertThat(slPrice, equalTo(1.12468));
    }

    @Test
    public void calculateTPPriceWithPipsIsCorrectForBuyOrder() {
        when(tickQuoteHandlerMock.bid(instrumentEURUSD))
            .thenReturn(1.12345);

        final double tpPrice = calculationUtil.tpPriceForPips(buyOrderEURUSD, 13.5);

        assertThat(tpPrice, equalTo(1.1248));
    }

    @Test
    public void calculateTPPriceWithPipsIsCorrectForSellOrder() {
        when(tickQuoteHandlerMock.ask(instrumentEURUSD))
            .thenReturn(1.12345);

        final double tpPrice = calculationUtil.tpPriceForPips(sellOrderEURUSD, 12.3);

        assertThat(tpPrice, equalTo(1.12222));
    }
}
