package com.jforex.programming.test.common;

import com.dukascopy.api.ICurrency;

public class CurrencyUtilForTest extends InstrumentUtilForTest {

    public final ICurrency currencyEUR = instrumentEURUSD.getPrimaryJFCurrency();
    public final ICurrency currencyUSD = instrumentEURUSD.getSecondaryJFCurrency();
    public final ICurrency currencyJPY = instrumentUSDJPY.getSecondaryJFCurrency();
    public final ICurrency currencyAUD = instrumentGBPAUD.getSecondaryJFCurrency();
    public final ICurrency currencyGBP = instrumentGBPAUD.getPrimaryJFCurrency();

    public final String currencyNameEUR = currencyEUR.getCurrencyCode();
    public final String currencyNameAUD = currencyAUD.getCurrencyCode();
    public final String currencyNameUSD = currencyUSD.getCurrencyCode();
    public final String currencyNameJPY = currencyJPY.getCurrencyCode();
    public final String currencyNameLowerCaseEUR = currencyNameEUR.toLowerCase();
    public final String currencyNameLowerCaseJPY = currencyNameJPY.toLowerCase();
    public final String invalidEmptyCurrencyName = "";
    public final String unknownCurrencyName = "HUG";
}
