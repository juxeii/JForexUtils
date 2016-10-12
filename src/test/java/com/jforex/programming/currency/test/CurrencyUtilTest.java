package com.jforex.programming.currency.test;

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
import com.jforex.programming.currency.CurrencyUtil;
import com.jforex.programming.test.common.CurrencyUtilForTest;

public class CurrencyUtilTest extends CurrencyUtilForTest {

    private final Set<Instrument> instrumentsForTest = Sets.newHashSet(instrumentEURUSD, instrumentUSDJPY);

    @Test
    public void testConstructorIsPrivate() throws Exception {
        assertPrivateConstructor(CurrencyUtil.class);
    }

    @Test
    public void testEmptyCurrencyNameIsNotValid() {
        assertFalse(isNameValid(invalidEmptyCurrencyName));
    }

    @Test
    public void testUpperCaseInvalidCurrencyNameIsNotValid() {
        assertFalse(isNameValid(unknownCurrencyName));
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
    public void testIsInInstrumentReturnsFalseForInvalidName() {
        assertFalse(isInInstrument(invalidEmptyCurrencyName, instrumentEURUSD));
        assertFalse(isInInstrument(unknownCurrencyName, instrumentEURUSD));
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
        assertTrue(isInInstruments(currencyEUR, instrumentsForTest));
    }

    @Test
    public void testIsInInstrumentsIsFalseForNotContainingCurrency() {
        assertFalse(isInInstruments(currencyAUD, instrumentsForTest));
    }
}
