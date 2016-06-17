package com.jforex.programming.currency.test;

import static com.jforex.programming.currency.CurrencyUtil.equalsBaseCurrency;
import static com.jforex.programming.currency.CurrencyUtil.equalsQuoteCurrency;
import static com.jforex.programming.currency.CurrencyUtil.isInInstrument;
import static com.jforex.programming.currency.CurrencyUtil.isInInstruments;
import static com.jforex.programming.currency.CurrencyUtil.isNameValid;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.Set;

import org.junit.Test;

import com.dukascopy.api.Instrument;
import com.google.common.collect.Sets;
import com.jforex.programming.test.common.CurrencyUtilForTest;

public class CurrencyUtilTest extends CurrencyUtilForTest {

    private final Set<Instrument> instrumentsTestSet = Sets.newHashSet(instrumentEURUSD,
                                                                       instrumentUSDJPY);

    @Test
    public void testEmptyCurrencyNameIsNotValid() {
        assertFalse(isNameValid(invalidEmptyCurrencyName));
    }

    @Test
    public void testLowerCaseInvalidCurrencyNameIsNotValid() {
        assertFalse(isNameValid(invalidLowerCaseCurrencyName));
    }

    @Test
    public void testUpperCaseInvalidCurrencyNameIsNotValid() {
        assertFalse(isNameValid(invalidCurrencyName));
    }

    @Test
    public void testLowerCaseCurrencyNameIsValid() {
        assertTrue(isNameValid(currencyNameLowerCaseEUR));
    }

    @Test
    public void testUpperCaseCurrencyNameIsValid() {
        assertTrue(isNameValid(currencyNameEUR));
    }

    @Test
    public void testEqualsBaseCurrencyIsTrue() {
        assertTrue(equalsBaseCurrency(currencyEUR, instrumentEURAUD));
    }

    @Test
    public void testEqualsBaseCurrencyIsFalse() {
        assertFalse(equalsBaseCurrency(currencyGBP, instrumentEURAUD));
        assertFalse(equalsBaseCurrency(currencyAUD, instrumentEURAUD));
    }

    @Test
    public void testEqualsQuoteCurrencyIsTrue() {
        assertTrue(equalsQuoteCurrency(currencyAUD, instrumentEURAUD));
    }

    @Test
    public void testEqualsQuoteCurrencyIsFalse() {
        assertFalse(equalsQuoteCurrency(currencyUSD, instrumentEURAUD));
        assertFalse(equalsQuoteCurrency(currencyEUR, instrumentEURAUD));
    }

    @Test
    public void testIsInInstrumentReturnsFalseForInvalidName() {
        assertFalse(isInInstrument(invalidEmptyCurrencyName, instrumentEURUSD));
        assertFalse(isInInstrument(invalidCurrencyName, instrumentEURUSD));
        assertFalse(isInInstrument(invalidLowerCaseCurrencyName, instrumentEURUSD));
    }

    @Test
    public void testIsInInstrumentReturnsTrueForValidName() {
        assertTrue(isInInstrument(currencyNameEUR, instrumentEURUSD));
    }

    @Test
    public void testIsInInstrumentCorrectForBaseCurrency() {
        assertTrue(isInInstrument(currencyEUR, instrumentEURUSD));
    }

    @Test
    public void testIsInInstrumentCorrectForQuoteCurrency() {
        assertTrue(isInInstrument(currencyUSD, instrumentEURUSD));
    }

    @Test
    public void testIsInInstrumentIsFalseForNotContainingCurrency() {
        assertFalse(isInInstrument(currencyJPY, instrumentEURUSD));
    }

    @Test
    public void testIsInInstrumentsIsFalseForEmptyInstrumentCollection() {
        assertFalse(isInInstruments(currencyEUR, Collections.emptySet()));
    }

    @Test
    public void testIsInInstrumentsIsTrueForContainingCurrency() {
        assertTrue(isInInstruments(currencyEUR, instrumentsTestSet));
    }

    @Test
    public void testIsInInstrumentsIsFalseForNotContainingCurrency() {
        assertFalse(isInInstruments(currencyAUD, instrumentsTestSet));
    }
}
