package com.jforex.programming.misc;

import java.util.AbstractMap.SimpleEntry;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IBar;
import com.dukascopy.api.IHistory;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;
import com.jforex.programming.quote.BarQuoteParams;
import com.jforex.programming.quote.QuoteProviderException;
import com.jforex.programming.quote.TickQuote;

import rx.Observable;

public class HistoryUtil {

    private final IHistory history;
    private final int maxBarTickRetries = 10;

    private static final Logger logger = LogManager.getLogger(HistoryUtil.class);

    public HistoryUtil(final IHistory history) {
        this.history = history;
    }

    public Map<Instrument, TickQuote> tickQuotes(final Set<Instrument> instruments) {
        return instruments.stream()
                .map(instrument -> {
                    final TickQuote tickQuote = new TickQuote(instrument, latestTick(instrument));
                    return new SimpleEntry<>(instrument, tickQuote);
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public ITick latestTick(final Instrument instrument) {
        return Observable
                .fromCallable(() -> history.getLastTick(instrument))
                .doOnError(e -> logger.warn("Get last tick for " + instrument +
                        " from history failed with exception " + e.getMessage()
                        + "! Will retry now..."))
                .flatMap(tick -> observableForHistory(tick, "tick"))
                .retry(maxBarTickRetries)
                .toBlocking()
                .single();
    }

    public IBar latestBar(final BarQuoteParams barQuoteParams) {
        final Instrument instrument = barQuoteParams.instrument();
        final Period period = barQuoteParams.period();
        final OfferSide offerSide = barQuoteParams.offerSide();

        return Observable
                .fromCallable(() -> history.getBar(instrument, period, offerSide, 1))
                .doOnError(e -> logger.error("Last bar for " + instrument
                        + " and period " + period + " and offerside " + offerSide
                        + " from history failed with exception " + e.getMessage()
                        + "! Will retry now..."))
                .flatMap(bar -> observableForHistory(bar, "bar"))
                .retry(maxBarTickRetries)
                .toBlocking()
                .single();
    }

    private <T> Observable<T> observableForHistory(final T value,
                                                   final String valueName) {
        return value == null
                ? Observable.error(new QuoteProviderException("History " + valueName + " is null!"))
                : Observable.just(value);
    }
}
