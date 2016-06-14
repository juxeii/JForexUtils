package com.jforex.programming.quote;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IHistory;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.OfferSide;
import com.jforex.programming.misc.RxUtil;

import rx.Observable;

public class TickQuoteProvider {

    private final Observable<TickQuote> tickQuoteObservable;
    private final Set<Instrument> subscribedInstruments;
    private final IHistory history;
    private final Map<Instrument, ITick> latestTickQuote = new ConcurrentHashMap<>();

    private final static Logger logger = LogManager.getLogger(TickQuoteProvider.class);

    public TickQuoteProvider(final Observable<TickQuote> tickQuoteObservable,
                             final Set<Instrument> subscribedInstruments,
                             final IHistory history) {
        this.tickQuoteObservable = tickQuoteObservable;
        this.subscribedInstruments = subscribedInstruments;
        this.history = history;

        initLatestTicksFromHistory();
        tickQuoteObservable.subscribe(this::onTickQuote);
    }

    private void initLatestTicksFromHistory() {
        subscribedInstruments.forEach(this::putTickFromHistory);
    }

    private void putTickFromHistory(final Instrument instrument) {
        Observable.fromCallable(() -> history.getLastTick(instrument))
                .flatMap(tick -> {
                    if (tick == null) {
                        logger.warn("Last tick for " + instrument + " from history returned null! Retrying...");
                        return Observable.error(new QuoteProviderException("History tick is null!"));
                    }
                    return Observable.just(tick);
                })
                .retryWhen(errors -> RxUtil.retryWithDelay(errors, 500L, TimeUnit.MILLISECONDS, 10))
                .first()
                .doOnNext(tick -> latestTickQuote.put(instrument, tick))
                .subscribe();
    }

    public ITick tick(final Instrument instrument) {
        return latestTickQuote.get(instrument);
    }

    public double ask(final Instrument instrument) {
        return tick(instrument).getAsk();
    }

    public double bid(final Instrument instrument) {
        return tick(instrument).getBid();
    }

    public double forOfferSide(final Instrument instrument,
                               final OfferSide offerSide) {
        return offerSide == OfferSide.BID
                ? bid(instrument)
                : ask(instrument);
    }

    public Observable<TickQuote> observable() {
        return tickQuoteObservable;
    }

    public Observable<TickQuote> subscribe(final Set<Instrument> instruments) {
        return tickQuoteObservable
                .filter(tickQuote -> instruments.contains(tickQuote.instrument()));
    }

    private void onTickQuote(final TickQuote tickQuote) {
        latestTickQuote.put(tickQuote.instrument(), tickQuote.tick());
    }
}
