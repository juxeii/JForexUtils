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
import com.dukascopy.api.JFException;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;
import com.jforex.programming.quote.BarParams;
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
                .doOnError(e -> logger.error(e.getMessage()
                        + "! Will retry latest tick from history now..."))
                .retry(maxBarTickRetries)
                .toBlocking()
                .single();
    }

    private ITick latestHistoryTick(final Instrument instrument) throws JFException {
        final ITick tick = history.getLastTick(instrument);
        if (tick == null)
            throw new QuoteProviderException("Latest tick from history for " + instrument
                    + " returned null!");
        return tick;
    }

    public IBar barQuote(final BarParams barParams) {
        final Instrument instrument = barParams.instrument();
        final Period period = barParams.period();
        final OfferSide offerSide = barParams.offerSide();

        return Observable
                .fromCallable(() -> latestHistoryBar(instrument, period, offerSide))
                .doOnError(e -> logger.error(e.getMessage()
                        + "! Will retry latest bar from history now..."))
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
            throw new QuoteProviderException("Latest bar from history for " + instrument
                    + " " + period + " " + offerSide + " returned null!");
        return bar;
    }
}
