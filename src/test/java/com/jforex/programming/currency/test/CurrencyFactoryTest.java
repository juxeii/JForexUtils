package com.jforex.programming.currency.test;

import static com.jforex.programming.currency.CurrencyFactory.fromInstrument;
import static com.jforex.programming.currency.CurrencyFactory.fromInstruments;
import static com.jforex.programming.currency.CurrencyFactory.fromNames;
import static com.jforex.programming.currency.CurrencyFactory.maybeFromName;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.Set;

import org.junit.Test;

import com.dukascopy.api.ICurrency;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFCurrency;
import com.google.common.collect.Sets;
import com.jforex.programming.currency.CurrencyCode;
import com.jforex.programming.currency.CurrencyFactory;
import com.jforex.programming.test.common.CurrencyUtilForTest;

public class CurrencyFactoryTest extends CurrencyUtilForTest {

    private final Set<String> currencyNamesAsSet = Sets.newHashSet(invalidEmptyCurrencyName,
                                                                   currencyNameLowerCaseJPY,
                                                                   currencyNameEUR);
    private final Set<Instrument> instrumentsAsSet = Sets.newHashSet(instrumentEURUSD,
                                                                     instrumentUSDJPY);

    private final String currencyNamesAsArray[] = currencyNamesAsSet.stream().toArray(String[]::new);
    private final Instrument instrumentsAsArray[] = instrumentsAsSet.stream().toArray(Instrument[]::new);

    private ICurrency currencyForValidCurrencyName(final String currencyName) {
        return maybeFromName(currencyName).get();
    }

    private void assertCurrencies(final Set<ICurrency> currencies) {
        assertThat(currencies.size(), equalTo(2));
        assertTrue(currencies.contains(currencyEUR));
        assertTrue(currencies.contains(currencyJPY));
    }

    private void assertStaticCurrency(final ICurrency currency,
                                      final CurrencyCode currencyCode) {
        assertThat(currency, equalTo(JFCurrency.getInstance(currencyCode.toString())));
    }

    private void assertCurrenciesFromInstruments(final Set<ICurrency> currencies) {
        assertThat(currencies.size(), equalTo(3));
        assertTrue(currencies.contains(currencyEUR));
        assertTrue(currencies.contains(currencyUSD));
        assertTrue(currencies.contains(currencyJPY));
    }

    @Test
    public void staticCurrenciesAreCorrect() {
        assertStaticCurrency(CurrencyFactory.EUR, CurrencyCode.EUR);
        assertStaticCurrency(CurrencyFactory.CHF, CurrencyCode.CHF);
        assertStaticCurrency(CurrencyFactory.USD, CurrencyCode.USD);
        assertStaticCurrency(CurrencyFactory.GBP, CurrencyCode.GBP);
        assertStaticCurrency(CurrencyFactory.JPY, CurrencyCode.JPY);
        assertStaticCurrency(CurrencyFactory.AUD, CurrencyCode.AUD);
        assertStaticCurrency(CurrencyFactory.NZD, CurrencyCode.NZD);
        assertStaticCurrency(CurrencyFactory.CAD, CurrencyCode.CAD);
        assertStaticCurrency(CurrencyFactory.HKD, CurrencyCode.HKD);
        assertStaticCurrency(CurrencyFactory.SGD, CurrencyCode.SGD);
        assertStaticCurrency(CurrencyFactory.SEK, CurrencyCode.SEK);
        assertStaticCurrency(CurrencyFactory.CZK, CurrencyCode.CZK);
        assertStaticCurrency(CurrencyFactory.RON, CurrencyCode.RON);
        assertStaticCurrency(CurrencyFactory.NOK, CurrencyCode.NOK);
        assertStaticCurrency(CurrencyFactory.TRY, CurrencyCode.TRY);
        assertStaticCurrency(CurrencyFactory.RUB, CurrencyCode.RUB);
        assertStaticCurrency(CurrencyFactory.CNH, CurrencyCode.CNH);
        assertStaticCurrency(CurrencyFactory.DKK, CurrencyCode.DKK);
        assertStaticCurrency(CurrencyFactory.HUF, CurrencyCode.HUF);
        assertStaticCurrency(CurrencyFactory.PLN, CurrencyCode.PLN);
        assertStaticCurrency(CurrencyFactory.BRL, CurrencyCode.BRL);
        assertStaticCurrency(CurrencyFactory.MXN, CurrencyCode.MXN);
        assertStaticCurrency(CurrencyFactory.ZAR, CurrencyCode.ZAR);
    }

    @Test
    public void testConstructorIsPrivate() throws Exception {
        assertPrivateConstructor(CurrencyFactory.class);
    }

    @Test
    public void fromNameReturnsEmptyOptionalForInvalidName() {
        assertFalse(maybeFromName(invalidEmptyCurrencyName).isPresent());
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
    public void testFromNamesRetunsEmptySetForEmptyCollection() {
        assertTrue(fromNames(Collections.<String> emptySet()).isEmpty());
    }

    @Test
    public void testFromNamesWithCollection() {
        assertCurrencies(fromNames(currencyNamesAsSet));
    }

    @Test
    public void testFromNamesWithVarargs() {
        assertCurrencies(fromNames(currencyNamesAsArray));
    }

    @Test
    public void testCurrenciesFromInstrument() {
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
    public void fromInstrumentsWithVarargs() {
        assertCurrenciesFromInstruments(fromInstruments(instrumentsAsArray));
    }
}
