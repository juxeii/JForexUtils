package com.jforex.programming.misc;

import java.util.AbstractMap.SimpleEntry;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jforex.programming.quote.BarQuoteParams;
import com.jforex.programming.quote.QuoteProviderException;
import com.jforex.programming.quote.TickQuote;

import com.dukascopy.api.IBar;
import com.dukascopy.api.IHistory;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;

import rx.Observable;

public class HistoryUtil {

    private final IHistory history;
    private final int maxBarTickRetries = 10;

    private static final Logger logger = LogManager.getLogger(HistoryUtil.class);

    public HistoryUtil(final IHistory history) {
        this.history = history;
    }

    public Map<Instrument, TickQuote> tickQuotes(final Set<Instrument> instruments) {
        return instruments
                .stream()
                .map(instrument -> {
                    final TickQuote tickQuote = new TickQuote(instrument, tickQuote(instrument));
                    return new SimpleEntry<>(instrument, tickQuote);
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public ITick tickQuote(final Instrument instrument) {
        return Observable
                .fromCallable(() -> latestHistoryTick(instrument))
                .doOnError(e -> logger.warn("Get last tick for " + instrument +
                        " from history failed with exception: " + e.getMessage()
                        + "! Will retry now..."))
                .retry(maxBarTickRetries)
                .toBlocking()
                .single();
    }

    private ITick latestHistoryTick(final Instrument instrument) throws JFException {
        final ITick tick = history.getLastTick(instrument);
        if (tick == null)
            throw new QuoteProviderException("Last history tick for " + instrument + " returned null!");
        return tick;
    }

    public IBar barQuote(final BarQuoteParams barQuoteParams) {
        final Instrument instrument = barQuoteParams.instrument();
        final Period period = barQuoteParams.period();
        final OfferSide offerSide = barQuoteParams.offerSide();

        return Observable
                .fromCallable(() -> latestHistoryBar(instrument, period, offerSide))
                .doOnError(e -> logger.error("Last bar for " + instrument
                        + " and period " + period + " and offerside " + offerSide
                        + " from history failed with exception: " + e.getMessage()
                        + "! Will retry now..."))
                .retry(maxBarTickRetries)
                .toBlocking()
                .single();
    }

    private IBar latestHistoryBar(final Instrument instrument,
                                  final Period period,
                                  final OfferSide offerSide) throws JFException {
        final IBar bar = history.getBar(instrument,
                                        period,
                                        offerSide,
                                        1);
        if (bar == null)
            throw new QuoteProviderException("Last history bar for " + instrument + " returned null!");
        return bar;
    }
}
