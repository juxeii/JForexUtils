package com.jforex.programming.misc;

import java.util.AbstractMap.SimpleEntry;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IBar;
import com.dukascopy.api.IHistory;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;
import com.jforex.programming.quote.QuoteProviderException;
import com.jforex.programming.quote.TickQuote;

import rx.Observable;

public class HistoryUtil {

    private final IHistory history;

    private final static Logger logger = LogManager.getLogger(HistoryUtil.class);

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
        return Observable.fromCallable(() -> history.getLastTick(instrument))
                .flatMap(tick -> tickObservableForHistoryTick(instrument, tick))
                .doOnError(e -> logger.warn("Last tick for " + instrument + " from history returned null! Retrying..."))
                .retryWhen(errors -> RxUtil.retryWithDelay(errors, 500L, TimeUnit.MILLISECONDS, 10))
                .first()
                .toBlocking()
                .first();
    }

    private Observable<ITick> tickObservableForHistoryTick(final Instrument instrument,
                                                           final ITick tick) {
        return tick == null
                ? Observable.error(new QuoteProviderException("History tick is null!"))
                : Observable.just(tick);
    }

    public IBar latestBar(final Instrument instrument,
                          final Period period,
                          final OfferSide offerSide) {
        return Observable.fromCallable(() -> history.getBar(instrument, period, offerSide, 1))
                .flatMap(bar -> barObservableForHistoryBar(instrument, period, offerSide, bar))
                .doOnError(e -> logger.error("Last bar for " + instrument + " and period " + period
                        + " and offerside " + offerSide + " from history returned null!"))
                .retry(10)
                .onErrorResumeNext(e -> Observable.error(new QuoteProviderException(e.getMessage())))
                .toBlocking()
                .first();
    }

    private Observable<IBar> barObservableForHistoryBar(final Instrument instrument,
                                                        final Period period,
                                                        final OfferSide offerSide,
                                                        final IBar bar) {
        return bar == null
                ? Observable.error(new QuoteProviderException("History bar is null!"))
                : Observable.just(bar);
    }
}
