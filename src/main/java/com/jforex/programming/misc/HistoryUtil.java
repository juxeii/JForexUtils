package com.jforex.programming.misc;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IBar;
import com.dukascopy.api.IHistory;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;
import com.jforex.programming.order.task.params.RetryParams;
import com.jforex.programming.quote.BarParams;
import com.jforex.programming.quote.QuoteException;
import com.jforex.programming.quote.TickQuote;
import com.jforex.programming.rx.RetryDelay;
import com.jforex.programming.rx.RxUtil;
import com.jforex.programming.settings.UserSettings;
import com.jforex.programming.strategy.StrategyUtil;

import io.reactivex.Observable;

public class HistoryUtil {

    private final IHistory history;

    private static final UserSettings userSettings = StrategyUtil.userSettings;
    private static final long delayOnHistoryFailRetry = userSettings.delayOnHistoryFailRetry();
    private static final int maxRetriesOnHistoryFail = userSettings.maxRetriesOnHistoryFail();
    private static final Logger logger = LogManager.getLogger(HistoryUtil.class);

    public HistoryUtil(final IHistory history) {
        this.history = history;
    }

    public Observable<TickQuote> tickQuotesObservable(final Set<Instrument> instruments) {
        return Observable
            .fromIterable(instruments)
            .flatMap(this::tickQuoteObservable);
    }

    public Observable<TickQuote> tickQuoteObservable(final Instrument instrument) {
        return lastestTickObservable(instrument)
            .flatMap(tick -> Observable.just(new TickQuote(instrument, tick)));
    }

    public Observable<ITick> lastestTickObservable(final Instrument instrument) {
        return Observable
            .fromCallable(() -> latestHistoryTick(instrument))
            .doOnError(e -> logger.error(e.getMessage() + " Will retry latest tick from history now..."))
            .retryWhen(RxUtil.retryWithDelay(retryParams()));
    }

    private RetryParams retryParams() {
        return new RetryParams(maxRetriesOnHistoryFail,
                               attempt -> new RetryDelay(delayOnHistoryFailRetry, TimeUnit.MILLISECONDS));
    }

    private ITick latestHistoryTick(final Instrument instrument) throws JFException {
        final ITick tick = history.getLastTick(instrument);
        if (tick == null) {
            final String errorMsg = "Latest tick from history for " + instrument + " returned null!";
            logger.error(errorMsg);
            throw new QuoteException(errorMsg);
        }
        return tick;
    }

    public Observable<IBar> latestBarObservable(final BarParams barParams) {
        final Instrument instrument = barParams.instrument();
        final Period period = barParams.period();
        final OfferSide offerSide = barParams.offerSide();

        return Observable
            .fromCallable(() -> latestHistoryBar(instrument, period, offerSide))
            .doOnError(e -> logger.error(e.getMessage() + " Will retry latest bar from history now..."))
            .retryWhen(RxUtil.retryWithDelay(retryParams()));
    }

    private IBar latestHistoryBar(final Instrument instrument,
                                  final Period period,
                                  final OfferSide offerSide) throws JFException {
        final IBar bar = history.getBar(instrument,
                                        period,
                                        offerSide,
                                        1);
        if (bar == null) {
            final String errorMsg = "Latest bar from history for " + instrument
                    + " " + period + " " + offerSide + " returned null!";
            logger.error(errorMsg);
            throw new QuoteException(errorMsg);
        }
        return bar;
    }
}
