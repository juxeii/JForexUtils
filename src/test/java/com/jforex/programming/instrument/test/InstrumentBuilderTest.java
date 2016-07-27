package com.jforex.programming.instrument.test;

import static com.jforex.programming.instrument.InstrumentBuilder.combineAllFromCurrencySet;
import static com.jforex.programming.instrument.InstrumentBuilder.combineAllWithAnchorCurrency;
import static com.jforex.programming.instrument.InstrumentBuilder.maybeFromName;
import static com.jforex.programming.instrument.InstrumentBuilder.maybeFromCurrencies;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

import java.util.Set;

import org.junit.Test;

import com.dukascopy.api.ICurrency;
import com.dukascopy.api.Instrument;
import com.google.common.collect.Sets;
import com.jforex.programming.instrument.InstrumentBuilder;
import com.jforex.programming.test.common.CurrencyUtilForTest;

public class InstrumentBuilderTest extends CurrencyUtilForTest {

    private final Set<Instrument> instrumentsForCombineTests =
            Sets.newHashSet(instrumentEURUSD,
                            instrumentEURAUD,
                            instrumentEURJPY,
                            instrumentAUDUSD,
                            instrumentUSDJPY,
                            instrumentAUDJPY);

    private final Set<Instrument> instrumentsForAnchorCurrencyTests =
            Sets.newHashSet(instrumentEURUSD,
                            instrumentEURAUD,
                            instrumentEURJPY);

    private void assertCombineCurrencySet(final Set<ICurrency> currencies,
                                          final Set<Instrument> instruments) {
        assertThat(combineAllFromCurrencySet(currencies), equalTo(instruments));
    }

    private void assertAnchorCurrencySet(final Set<ICurrency> partnerCurrencies,
                                         final Set<Instrument> instruments) {
        assertThat(combineAllWithAnchorCurrency(currencyEUR, partnerCurrencies),
                   equalTo(instruments));
    }

    @Test
    public void testConstructorIsPrivate() throws Exception {
        assertPrivateConstructor(InstrumentBuilder.class);
    }

    @Test
    public void testFromNameReturnsEmptyOptionalForInvalidInstrumentName() {
        assertFalse(maybeFromName(invalidEmptyCurrencyName).isPresent());
        assertFalse(maybeFromName(currencyNameEUR).isPresent());
        assertFalse(maybeFromName(currencyNameEUR).isPresent());
        assertFalse(maybeFromName("EURUSD").isPresent());
        assertFalse(maybeFromName("eurusd").isPresent());
    }

    @Test
    public void testFromNameReturnsOptionalWithCorrectInstrument() {
        assertThat(maybeFromName("EUR/USD").get(), equalTo(instrumentEURUSD));
        assertThat(maybeFromName("eUr/UsD").get(), equalTo(instrumentEURUSD));
        assertThat(maybeFromName("USD/EUR").get(), equalTo(instrumentEURUSD));
        assertThat(maybeFromName("UsD/eUr").get(), equalTo(instrumentEURUSD));
    }

    @Test
    public void testFromNameReturnsOptionalWithCorrectInstrumentForInvertedInstrumentName() {
        assertThat(maybeFromName("USD/EUR").get(), equalTo(instrumentEURUSD));
        assertThat(maybeFromName("UsD/eUr").get(), equalTo(instrumentEURUSD));
    }

    @Test
    public void testFromCurrenciesReturnsEmptyOptionalForEqualCurrencies() {
        assertFalse(maybeFromCurrencies(currencyEUR, currencyEUR).isPresent());
    }

    @Test
    public void testFromCurrenciesReturnsOptionalWithValidInstrument() {
        assertThat(maybeFromCurrencies(currencyEUR, currencyUSD).get(), equalTo(instrumentEURUSD));
        assertThat(maybeFromCurrencies(currencyUSD, currencyEUR).get(), equalTo(instrumentEURUSD));
    }

    @Test
    public void testCombineAllFromCurrencySetReturnsEmptySetForZeorCurrencies() {
        final Set<ICurrency> currencies = Sets.newHashSet();

        assertCombineCurrencySet(currencies, Sets.newHashSet());
    }

    @Test
    public void testCombineAllFromCurrencySetReturnsEmptySetForOneCurrency() {
        assertCombineCurrencySet(Sets.newHashSet(currencyEUR), Sets.newHashSet());
    }

    @Test
    public void testCombineAllFromCurrencySetReturnsEmptySetForOnlyEqualCurrencies() {
        final Set<ICurrency> currencies = Sets.newHashSet(currencyEUR, currencyEUR);

        assertCombineCurrencySet(currencies, Sets.newHashSet());
    }

    @Test
    public void testCombineAllFromCurrencySetReturnsCorrectInstruments() {
        final Set<ICurrency> currencies = Sets.newHashSet(currencyEUR,
                                                          currencyUSD,
                                                          currencyAUD,
                                                          currencyJPY);

        assertCombineCurrencySet(currencies, instrumentsForCombineTests);
    }

    @Test
    public void
           testCombineAllFromCurrencySetReturnsCorrectInstrumentsForEqualCurrenciesInCollection() {
        final Set<ICurrency> currencies = Sets.newHashSet(currencyEUR,
                                                          currencyEUR,
                                                          currencyUSD,
                                                          currencyAUD,
                                                          currencyJPY,
                                                          currencyJPY);

        assertThat(combineAllFromCurrencySet(currencies), equalTo(instrumentsForCombineTests));
    }

    @Test
    public void testCombineAllWithAnchorCurrencyReturnsEmptySetForZeorPartnerCurrencies() {
        final Set<ICurrency> partnerCurrencies = Sets.newHashSet();

        assertAnchorCurrencySet(partnerCurrencies, Sets.newHashSet());
    }

    @Test
    public void testCombineAllWithAnchorCurrencyReturnsEmptySetForEqualPartnerCurrency() {
        assertAnchorCurrencySet(Sets.newHashSet(currencyEUR), Sets.newHashSet());
    }

    @Test
    public void testCombineAllWithAnchorCurrencyReturnsCorrectInstruments() {
        final Set<ICurrency> partnerCurrencies = Sets.newHashSet(currencyEUR,
                                                                 currencyUSD,
                                                                 currencyAUD,
                                                                 currencyJPY);

        assertAnchorCurrencySet(partnerCurrencies, instrumentsForAnchorCurrencyTests);
    }

    @Test
    public void
           testCombineAllWithAnchorCurrencyReturnsCorrectInstrumentsForEqualCurrenciesInCollection() {
        final Set<ICurrency> partnerCurrencies = Sets.newHashSet(currencyEUR,
                                                                 currencyUSD,
                                                                 currencyUSD,
                                                                 currencyAUD,
                                                                 currencyJPY,
                                                                 currencyJPY);

        assertAnchorCurrencySet(partnerCurrencies, instrumentsForAnchorCurrencyTests);
    }
}
