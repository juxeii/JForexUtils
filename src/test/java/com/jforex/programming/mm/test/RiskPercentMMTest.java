package com.jforex.programming.mm.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.dukascopy.api.IAccount;
import com.dukascopy.api.OfferSide;
import com.jforex.programming.misc.CalculationUtil;
import com.jforex.programming.mm.RiskPercentMM;
import com.jforex.programming.quote.TickQuoteHandler;
import com.jforex.programming.test.common.CurrencyUtilForTest;
import com.jforex.programming.test.common.QuoteProviderForTest;

public class RiskPercentMMTest extends CurrencyUtilForTest {

    private RiskPercentMM riskPercentMM;

    @Mock
    private IAccount accountMock;
    @Mock
    private TickQuoteHandler quoteProviderMock;
    private CalculationUtil calculationUtil;
    private final double askEURUSD = 1.10987;
    private final double bidEURUSD = 1.10975;
    private final double equity = 2187.89;

    @Before
    public void setUp() {
        setUpMocks();

        calculationUtil = new CalculationUtil(quoteProviderMock);
        riskPercentMM = new RiskPercentMM(accountMock, calculationUtil);
    }

    private void setUpMocks() {
        initCommonTestFramework();

        when(accountMock.getAccountCurrency()).thenReturn(currencyEUR);
        when(accountMock.getEquity()).thenReturn(equity);
        QuoteProviderForTest.setQuoteExpectations(quoteProviderMock, instrumentEURUSD, bidEURUSD, askEURUSD);
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

        final double amount = riskPercentMM.amountForRisk(instrumentEURUSD, OfferSide.ASK, 10.0, 22.4);

        assertThat(amount, equalTo(0.551146));
    }

    @Test
    public void testAmountForRiskIsCorrectForAccountCurrencyDifferentToQuoteCurrency() {
        when(accountMock.getEquity()).thenReturn(12345.67);

        final double amount = riskPercentMM.amountForRisk(instrumentEURUSD, OfferSide.ASK, 10.0, 22.4);

        assertThat(amount, equalTo(0.6117));
    }

    @Test
    public void testAmountForFixMarginIsCorrectForAccountCurrencyEqualToQuoteCurrency() {
        when(accountMock.getAccountCurrency()).thenReturn(currencyUSD);

        final double amount = riskPercentMM.amountForFixMargin(instrumentEURUSD, OfferSide.BID, 12.3, 10.0);

        assertThat(amount, equalTo(0.0123));
    }

    @Test
    public void testAmountForFixMarginIsCorrectForAccountCurrencyDifferentToQuoteCurrency() {
        final double amount = riskPercentMM.amountForFixMargin(instrumentEURUSD, OfferSide.BID, 12.3, 10.0);

        assertThat(amount, equalTo(0.01365));
    }
}
