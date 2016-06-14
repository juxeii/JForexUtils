package com.jforex.programming.quote;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.aeonbits.owner.ConfigFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jforex.programming.misc.JFHotObservable;
import com.jforex.programming.misc.JForexUtil;
import com.jforex.programming.misc.RxUtil;
import com.jforex.programming.settings.UserSettings;

import com.dukascopy.api.IHistory;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.OfferSide;

import rx.Observable;

public class TickQuoteHandler implements TickQuoteProvider {

    private final JForexUtil jforexUtil;
    private final Set<Instrument> subscribedInstruments;
    private final IHistory history;
    private final JFHotObservable<TickQuote> tickQuotePublisher = new JFHotObservable<>();
    private final Observable<TickQuote> tickQuoteObservable = tickQuotePublisher.get();
    private final Map<Instrument, ITick> latestTickQuote = new ConcurrentHashMap<>();

    private final static UserSettings userSettings = ConfigFactory.create(UserSettings.class);
    private final static Logger logger = LogManager.getLogger(TickQuoteHandler.class);

    public TickQuoteHandler(final JForexUtil jforexUtil,
                            final Set<Instrument> subscribedInstruments) {
        this.jforexUtil = jforexUtil;
        this.subscribedInstruments = subscribedInstruments;
        history = jforexUtil.history();

        initLatestTicksFromHistory();
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

    @Override
    public ITick tick(final Instrument instrument) {
        return latestTickQuote.get(instrument);
    }

    @Override
    public double ask(final Instrument instrument) {
        return tick(instrument).getAsk();
    }

    @Override
    public double bid(final Instrument instrument) {
        return tick(instrument).getBid();
    }

    @Override
    public double forOfferSide(final Instrument instrument,
                               final OfferSide offerSide) {
        return offerSide == OfferSide.BID
                ? bid(instrument)
                : ask(instrument);
    }

    @Override
    public Observable<TickQuote> observable() {
        return tickQuoteObservable;
    }

    @Override
    public Observable<TickQuote> quoteObservable(final Set<Instrument> instruments) {
        return tickQuoteObservable
                .filter(tickQuote -> instruments.contains(tickQuote.instrument()));
    }

    public void onTick(final Instrument instrument,
                       final ITick tick) {
        if (userSettings.enableWeekendQuoteFilter() && !jforexUtil.isMarketClosed(tick.getTime()))
            onTickQuote(new TickQuote(instrument, tick));
    }

    private void onTickQuote(final TickQuote tickQuote) {
        latestTickQuote.put(tickQuote.instrument(), tickQuote.tick());
        tickQuotePublisher.onNext(tickQuote);
    }
}
