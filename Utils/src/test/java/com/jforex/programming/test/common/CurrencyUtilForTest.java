package com.jforex.programming.test.common;

import com.dukascopy.api.ICurrency;

public class CurrencyUtilForTest extends InstrumentUtilForTest {

    public final ICurrency currencyEUR = instrumentEURUSD.getPrimaryJFCurrency();
    public final ICurrency currencyUSD = instrumentEURUSD.getSecondaryJFCurrency();
    public final ICurrency currencyJPY = instrumentUSDJPY.getSecondaryJFCurrency();
    public final ICurrency currencyAUD = instrumentGBPAUD.getSecondaryJFCurrency();
    public final ICurrency currencyGBP = instrumentGBPAUD.getPrimaryJFCurrency();

    public final String currencyNameEUR = "EUR";
    public final String currencyNameAUD = "AUD";
    public final String currencyNameUSD = "USD";
    public final String currencyNameJPY = "JPY";
    public final String invalidEmptyCurrencyName = "";
    public final String invalidCurrencyName = "HUG";
    public final String invalidLowerCaseCurrencyName = "hug";
    public final String currencyNameLowerCaseEUR = "eur";
    public final String currencyNameLowerCaseJPY = "jpy";
}
