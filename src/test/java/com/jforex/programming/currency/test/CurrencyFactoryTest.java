package com.jforex.programming.currency.test;

import static com.jforex.programming.currency.CurrencyFactory.fromCode;
import static com.jforex.programming.currency.CurrencyFactory.fromCodes;
import static com.jforex.programming.currency.CurrencyFactory.fromNames;
import static com.jforex.programming.currency.CurrencyFactory.maybeFromName;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import org.junit.Test;

import com.dukascopy.api.ICurrency;
import com.google.common.collect.Sets;
import com.jforex.programming.currency.CurrencyCode;
import com.jforex.programming.currency.CurrencyFactory;
import com.jforex.programming.test.common.CurrencyUtilForTest;

public class CurrencyFactoryTest extends CurrencyUtilForTest {

    private final Set<String> currencyNamesAsSet = Sets.newHashSet(invalidEmptyCurrencyName,
                                                                   currencyNameLowerCaseJPY,
                                                                   currencyNameEUR);

    private final Set<CurrencyCode> currencyCodesAsSet = Sets.newHashSet(CurrencyCode.EUR,
                                                                         CurrencyCode.JPY);

    private final String currencyNamesAsArray[] = currencyNamesAsSet.stream().toArray(String[]::new);
    private final CurrencyCode currencyCodesAsArray[] = currencyCodesAsSet.stream().toArray(CurrencyCode[]::new);

    private ICurrency currencyForValidCurrencyName(final String currencyName) {
        return maybeFromName(currencyName).get();
    }

    private void assertCurrencies(final Set<ICurrency> currencies) {
        assertThat(currencies.size(), equalTo(2));
        assertTrue(currencies.contains(currencyEUR));
        assertTrue(currencies.contains(currencyJPY));
    }

    @Test
    public void testConstructorIsPrivate() throws Exception {
        assertPrivateConstructor(CurrencyFactory.class);
    }

    @Test
    public void testFromNameReturnsEmptyOptionalForInvalidName() {
        assertThat(maybeFromName(invalidEmptyCurrencyName), equalTo(Optional.empty()));
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
    public void testFromNamesWithEllipsis() {
        assertCurrencies(fromNames(currencyNamesAsArray));
    }

    @Test
    public void testFromCodeIsCorrect() {
        assertThat(fromCode(CurrencyCode.EUR), equalTo(currencyEUR));
    }

    @Test
    public void testFromCodesWithCollection() {
        assertCurrencies(fromCodes(currencyCodesAsSet));
    }

    @Test
    public void testFromCodesWithEllipsis() {
        assertCurrencies(fromCodes(currencyCodesAsArray));
    }
}
