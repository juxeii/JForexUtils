package com.jforex.programming.currency.test;

import static com.jforex.programming.currency.CurrencyBuilder.fromInstrument;
import static com.jforex.programming.currency.CurrencyBuilder.fromInstruments;
import static com.jforex.programming.currency.CurrencyBuilder.fromName;
import static com.jforex.programming.currency.CurrencyBuilder.fromNames;
import static com.jforex.programming.currency.CurrencyBuilder.instanceFromName;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import org.junit.Test;

import com.dukascopy.api.ICurrency;
import com.dukascopy.api.Instrument;
import com.jforex.programming.test.common.CurrencyUtilForTest;

public class CurrencyBuilderTest extends CurrencyUtilForTest {

    private final Set<String> currencyNamesAsSet = createSet(invalidEmptyCurrencyName,
                                                             invalidLowerCaseCurrencyName,
                                                             currencyNameLowerCaseJPY,
                                                             currencyNameEUR);

    private final Set<Instrument> instrumentsAsSet = createSet(instrumentEURUSD,
                                                               instrumentUSDJPY);

    private final String currencyNamesAsArray[] = currencyNamesAsSet.stream().toArray(String[]::new);
    private final Instrument instrumentsAsArray[] = instrumentsAsSet.stream().toArray(Instrument[]::new);

    private ICurrency currencyForValidCurrencyName(final String currencyName) {
        return fromName(currencyName).get();
    }

    private void assertCurrenciesFromNames(final Set<ICurrency> currencies) {
        assertThat(currencies.size(), equalTo(2));
        assertTrue(currencies.contains(currencyEUR));
        assertTrue(currencies.contains(currencyJPY));
    }

    private void assertCurrenciesFromInstruments(final Set<ICurrency> currencies) {
        assertThat(currencies.size(), equalTo(3));
        assertTrue(currencies.contains(currencyEUR));
        assertTrue(currencies.contains(currencyUSD));
        assertTrue(currencies.contains(currencyJPY));
    }

    @Test
    public void testFromNameReturnsEmptyOptionalForInvalidName() {
        assertThat(fromName(invalidEmptyCurrencyName), equalTo(Optional.empty()));
        assertThat(fromName(invalidLowerCaseCurrencyName), equalTo(Optional.empty()));
    }

    @Test
    public void testFromNameReturnsCorrectCurrencyIfUpperCase() {
        assertThat(currencyForValidCurrencyName(currencyNameEUR), equalTo(currencyEUR));
    }

    @Test
    public void testFromNameReturnsCorrectCurrencyIfLowerCase() {
        assertThat(currencyForValidCurrencyName(currencyNameLowerCaseEUR), equalTo(currencyEUR));
    }

    @Test
    public void testInstanceFromNameReturnsInstanceForInvalidCurrencyName() {
        assertTrue(instanceFromName(invalidCurrencyName) instanceof ICurrency);
    }

    @Test
    public void testFromNamesRetunsEmptySetForEmptyCollection() {
        assertTrue(fromNames(Collections.<String> emptySet()).isEmpty());
    }

    @Test
    public void testFromNamesWithCollection() {
        assertCurrenciesFromNames(fromNames(currencyNamesAsSet));
    }

    @Test
    public void testFromNamesWithEllipsis() {
        assertCurrenciesFromNames(fromNames(currencyNamesAsArray));
    }

    @Test
    public void testFromInstrument() {
        final Set<ICurrency> currencies = fromInstrument(instrumentEURUSD);
        assertThat(currencies.size(), equalTo(2));
        assertTrue(currencies.contains(currencyEUR));
        assertTrue(currencies.contains(currencyUSD));
    }

    @Test
    public void testFromInstrumentsRetunsEmptySetForEmptyCollection() {
        assertTrue(fromInstruments(Collections.<Instrument> emptySet()).isEmpty());
    }

    @Test
    public void testFromInstrumentsWithCollection() {
        assertCurrenciesFromInstruments(fromInstruments(instrumentsAsSet));
    }

    @Test
    public void testFromInstrumentsWithEllipsis() {
        assertCurrenciesFromInstruments(fromInstruments(instrumentsAsArray));
    }
}
