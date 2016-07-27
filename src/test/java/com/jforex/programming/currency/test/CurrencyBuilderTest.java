package com.jforex.programming.currency.test;

import static com.jforex.programming.currency.CurrencyBuilder.maybeFromName;
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
import com.google.common.collect.Sets;
import com.jforex.programming.currency.CurrencyBuilder;
import com.jforex.programming.test.common.CurrencyUtilForTest;

public class CurrencyBuilderTest extends CurrencyUtilForTest {

    private final Set<String> currencyNamesAsSet = Sets.newHashSet(invalidEmptyCurrencyName,
                                                                   invalidLowerCaseCurrencyName,
                                                                   currencyNameLowerCaseJPY,
                                                                   currencyNameEUR);

    private final String currencyNamesAsArray[] =
            currencyNamesAsSet.stream().toArray(String[]::new);

    private ICurrency currencyForValidCurrencyName(final String currencyName) {
        return maybeFromName(currencyName).get();
    }

    private void assertCurrenciesFromNames(final Set<ICurrency> currencies) {
        assertThat(currencies.size(), equalTo(2));
        assertTrue(currencies.contains(currencyEUR));
        assertTrue(currencies.contains(currencyJPY));
    }

    @Test
    public void testConstructorIsPrivate() throws Exception {
        assertPrivateConstructor(CurrencyBuilder.class);
    }

    @Test
    public void testFromNameReturnsEmptyOptionalForInvalidName() {
        assertThat(maybeFromName(invalidEmptyCurrencyName), equalTo(Optional.empty()));
        assertThat(maybeFromName(invalidLowerCaseCurrencyName), equalTo(Optional.empty()));
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
}
