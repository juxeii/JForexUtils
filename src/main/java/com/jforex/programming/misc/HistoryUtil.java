package com.jforex.programming.misc;

import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jforex.programming.quote.QuoteProviderException;

import com.dukascopy.api.IBar;
import com.dukascopy.api.IHistory;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;

import rx.Observable;

public class HistoryUtil {

    private final IHistory history;

    private final static Logger logger = LogManager.getLogger(HistoryUtil.class);

    public HistoryUtil(final IHistory history) {
        this.history = history;
    }

    public ITick latestTick(final Instrument instrument) {
        return Observable.fromCallable(() -> history.getLastTick(instrument))
                .flatMap(tick -> {
                    if (tick == null) {
                        logger.warn("Last tick for " + instrument + " from history returned null! Retrying...");
                        return Observable.error(new QuoteProviderException("History tick is null!"));
                    }
                    return Observable.just(tick);
                })
                .retryWhen(errors -> RxUtil.retryWithDelay(errors, 500L, TimeUnit.MILLISECONDS, 10))
                .first()
                .toBlocking()
                .first();
    }

    public IBar latestBar(final Instrument instrument,
                          final Period period,
                          final OfferSide offerSide) {
        return Observable.fromCallable(() -> history.getBar(instrument, period, offerSide, 1))
                .flatMap(bar -> {
                    if (bar == null) {
                        logger.error("Last bar for " + instrument + " and period " + period
                                + " and offerside " + offerSide + " from history returned null!");
                        return Observable.error(new QuoteProviderException("History bar is null!"));
                    }
                    return Observable.just(bar);
                })
                .retry(10)
                .onErrorResumeNext(e -> Observable.error(new QuoteProviderException(e.getMessage())))
                .toBlocking()
                .first();
    }
}
