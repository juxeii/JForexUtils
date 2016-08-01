package com.jforex.programming.currency;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toSet;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import com.dukascopy.api.ICurrency;
import com.dukascopy.api.JFCurrency;

public final class CurrencyFactory {

    public static final ICurrency EUR = fromCode(CurrencyCode.EUR);
    public static final ICurrency CHF = fromCode(CurrencyCode.CHF);
    public static final ICurrency USD = fromCode(CurrencyCode.USD);
    public static final ICurrency GBP = fromCode(CurrencyCode.GBP);
    public static final ICurrency JPY = fromCode(CurrencyCode.JPY);
    public static final ICurrency AUD = fromCode(CurrencyCode.AUD);
    public static final ICurrency NZD = fromCode(CurrencyCode.NZD);
    public static final ICurrency CAD = fromCode(CurrencyCode.CAD);
    public static final ICurrency HKD = fromCode(CurrencyCode.HKD);
    public static final ICurrency SGD = fromCode(CurrencyCode.SGD);
    public static final ICurrency SEK = fromCode(CurrencyCode.SEK);
    public static final ICurrency CZK = fromCode(CurrencyCode.CZK);
    public static final ICurrency RON = fromCode(CurrencyCode.RON);
    public static final ICurrency NOK = fromCode(CurrencyCode.NOK);
    public static final ICurrency TRY = fromCode(CurrencyCode.TRY);
    public static final ICurrency RUB = fromCode(CurrencyCode.RUB);
    public static final ICurrency CNH = fromCode(CurrencyCode.CNH);
    public static final ICurrency DKK = fromCode(CurrencyCode.DKK);
    public static final ICurrency HUF = fromCode(CurrencyCode.HUF);
    public static final ICurrency PLN = fromCode(CurrencyCode.PLN);
    public static final ICurrency BRL = fromCode(CurrencyCode.BRL);
    public static final ICurrency MXN = fromCode(CurrencyCode.MXN);
    public static final ICurrency ZAR = fromCode(CurrencyCode.ZAR);

    private CurrencyFactory() {
    }

    private static final ICurrency fromCode(final CurrencyCode currencyCode) {
        return instanceFromName(currencyCode.toString());
    }

    public static final Optional<ICurrency> maybeFromName(final String currencyName) {
        return CurrencyUtil.isNameValid(checkNotNull(currencyName))
                ? Optional.of(instanceFromName(currencyName))
                : Optional.empty();
    }

    public static final Set<ICurrency> fromNames(final Collection<String> currencyNames) {
        return checkNotNull(currencyNames)
                .stream()
                .map(CurrencyFactory::maybeFromName)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toSet());
    }

    public static final Set<ICurrency> fromNames(final String... currencyNames) {
        return fromNames(asList(checkNotNull(currencyNames)));
    }

    private static final ICurrency instanceFromName(final String currencyName) {
        return JFCurrency.getInstance(currencyName.toUpperCase());
    }
}
