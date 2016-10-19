package com.jforex.programming.currency;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toSet;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import com.dukascopy.api.ICurrency;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFCurrency;
import com.google.common.collect.Sets;

/**
 * Provides a collection of <a href=
 * "https://www.dukascopy.com/client/javadoc/com/dukascopy/api/ICurrency.html">ICurrency</a>
 * instances for all major currencies.
 *
 * Various methods create ICurrency instance(s) from different sources.
 */
public final class CurrencyFactory {

    private CurrencyFactory() {
    }

    /** ICurrency instance for the Australian Dollar. */
    public static final ICurrency AUD = fromCode(CurrencyCode.AUD);
    /** ICurrency instance for the Brazilian Real. */
    public static final ICurrency BRL = fromCode(CurrencyCode.BRL);
    /** ICurrency instance for the Canadian Dollar. */
    public static final ICurrency CAD = fromCode(CurrencyCode.CAD);
    /** ICurrency instance for the Swiss Franc. */
    public static final ICurrency CHF = fromCode(CurrencyCode.CHF);
    /** ICurrency instance for the Chinese Yuan. */
    public static final ICurrency CNH = fromCode(CurrencyCode.CNH);
    /** ICurrency instance for the Czech Koruna. */
    public static final ICurrency CZK = fromCode(CurrencyCode.CZK);
    /** ICurrency instance for the Danish Krone. */
    public static final ICurrency DKK = fromCode(CurrencyCode.DKK);
    /** ICurrency instance for the Euro. */
    public static final ICurrency EUR = fromCode(CurrencyCode.EUR);
    /** ICurrency instance for the British Pound. */
    public static final ICurrency GBP = fromCode(CurrencyCode.GBP);
    /** ICurrency instance for the Hongkong Dollar. */
    public static final ICurrency HKD = fromCode(CurrencyCode.HKD);
    /** ICurrency instance for the Hungarian Forint. */
    public static final ICurrency HUF = fromCode(CurrencyCode.HUF);
    /** ICurrency instance for the Japanese Yen. */
    public static final ICurrency JPY = fromCode(CurrencyCode.JPY);
    /** ICurrency instance for the Mexican Peso. */
    public static final ICurrency MXN = fromCode(CurrencyCode.MXN);
    /** ICurrency instance for the Norwegian Krone. */
    public static final ICurrency NOK = fromCode(CurrencyCode.NOK);
    /** ICurrency instance for the New Zealand Dollar. */
    public static final ICurrency NZD = fromCode(CurrencyCode.NZD);
    /** ICurrency instance for the Polish Zloty. */
    public static final ICurrency PLN = fromCode(CurrencyCode.PLN);
    /** ICurrency instance for the Romanian Leu. */
    public static final ICurrency RON = fromCode(CurrencyCode.RON);
    /** ICurrency instance for the Russian Ruble. */
    public static final ICurrency RUB = fromCode(CurrencyCode.RUB);
    /** ICurrency instance for the Swedish Krona. */
    public static final ICurrency SEK = fromCode(CurrencyCode.SEK);
    /** ICurrency instance for the Singapur Dollar. */
    public static final ICurrency SGD = fromCode(CurrencyCode.SGD);
    /** ICurrency instance for the Turkish Lira. */
    public static final ICurrency TRY = fromCode(CurrencyCode.TRY);
    /** ICurrency instance for the US Dollar. */
    public static final ICurrency USD = fromCode(CurrencyCode.USD);
    /** ICurrency instance for the South African Rand. */
    public static final ICurrency ZAR = fromCode(CurrencyCode.ZAR);

    private static final ICurrency fromCode(final CurrencyCode currencyCode) {
        return instanceFromName(currencyCode.toString());
    }

    public static final Optional<ICurrency> maybeFromName(final String currencyName) {
        checkNotNull(currencyName);

        return CurrencyUtil.isNameValid(currencyName)
                ? Optional.of(instanceFromName(currencyName))
                : Optional.empty();
    }

    public static final Set<ICurrency> fromNames(final Collection<String> currencyNames) {
        checkNotNull(currencyNames);

        return currencyNames
            .stream()
            .map(CurrencyFactory::maybeFromName)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(toSet());
    }

    public static final Set<ICurrency> fromNames(final String... currencyNames) {
        checkNotNull(currencyNames);

        return fromNames(asList(currencyNames));
    }

    public static final Set<ICurrency> fromInstrument(final Instrument instrument) {
        checkNotNull(instrument);

        return Sets.newHashSet(instrument.getPrimaryJFCurrency(),
                               instrument.getSecondaryJFCurrency());
    }

    public static final Set<ICurrency> fromInstruments(final Collection<Instrument> instruments) {
        checkNotNull(instruments);

        return instruments
            .stream()
            .map(CurrencyFactory::fromInstrument)
            .flatMap(Set::stream)
            .collect(toSet());
    }

    public static final Set<ICurrency> fromInstruments(final Instrument... instruments) {
        checkNotNull(instruments);

        return fromInstruments(asList(instruments));
    }

    private static final ICurrency instanceFromName(final String currencyName) {
        return JFCurrency.getInstance(currencyName.toUpperCase());
    }
}
