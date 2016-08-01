package com.jforex.programming.mm.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.dukascopy.api.OfferSide;
import com.jforex.programming.math.CalculationUtil;
import com.jforex.programming.mm.RiskPercentMM;
import com.jforex.programming.test.common.QuoteProviderForTest;

public class RiskPercentMMTest extends QuoteProviderForTest {

    private RiskPercentMM riskPercentMM;

    private CalculationUtil calculationUtil;
    private final double equity = 2187.89;

    @Before
    public void setUp() {
        setUpMocks();

        calculationUtil = new CalculationUtil(tickQuoteHandlerMock);
        riskPercentMM = new RiskPercentMM(accountMock, calculationUtil);
    }

    private void setUpMocks() {
        when(accountMock.getAccountCurrency()).thenReturn(currencyEUR);
        when(accountMock.getEquity()).thenReturn(equity);
        setTickExpectations(tickQuoteEURUSD);
    }

    @Test
    public void testPercentOfEquityIsCorrect() {
        assertThat(riskPercentMM.percentOfEquity(12.5), equalTo(equity * 0.125));
        assertThat(riskPercentMM.percentOfEquity(0.0), equalTo(0.0));
        assertThat(riskPercentMM.percentOfEquity(123), equalTo(equity * 1.23));
    }

    @Test
    public void testAmountForRiskIsCorrectForAccountCurrencyEqualToQuoteCurrency() {
        when(accountMock.getAccountCurrency()).thenReturn(currencyUSD);
        when(accountMock.getEquity()).thenReturn(12345.67);

        final double amount = riskPercentMM.amountForRisk(instrumentEURUSD,
                                                          OfferSide.ASK,
                                                          10.0,
                                                          22.4);

        assertThat(amount, equalTo(0.551146));
    }

    @Test
    public void testAmountForRiskIsCorrectForAccountCurrencyDifferentToQuoteCurrency() {
        when(accountMock.getEquity()).thenReturn(12345.67);

        final double amount = riskPercentMM.amountForRisk(instrumentEURUSD,
                                                          OfferSide.ASK,
                                                          10.0,
                                                          22.4);

        assertThat(amount, equalTo(0.6117));
    }

    @Test
    public void testAmountForFixMarginIsCorrectForAccountCurrencyEqualToQuoteCurrency() {
        when(accountMock.getAccountCurrency()).thenReturn(currencyUSD);

        final double amount = riskPercentMM.amountForFixMargin(instrumentEURUSD,
                                                               OfferSide.BID,
                                                               12.3,
                                                               10.0);

        assertThat(amount, equalTo(0.0123));
    }

    @Test
    public void testAmountForFixMarginIsCorrectForAccountCurrencyDifferentToQuoteCurrency() {
        final double amount = riskPercentMM.amountForFixMargin(instrumentEURUSD,
                                                               OfferSide.BID,
                                                               12.3,
                                                               10.0);

        assertThat(amount, equalTo(0.01365));
    }
}
