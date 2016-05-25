package com.jforex.programming.quote;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections4.keyvalue.MultiKey;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IBar;
import com.dukascopy.api.IHistory;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;

import rx.Observable;

public class BarQuoteProvider {

    private final Observable<BarQuote> barQuoteObservable;
    private final IHistory history;
    private final Map<MultiKey<Object>, BarQuote> latestBarQuote = new ConcurrentHashMap<>();

    private final static Logger logger = LogManager.getLogger(BarQuoteProvider.class);

    public BarQuoteProvider(final Observable<BarQuote> barQuoteObservable,
                            final IHistory history) {
        this.barQuoteObservable = barQuoteObservable;
        this.history = history;

        barQuoteObservable.subscribe(this::onBarQuote);
    }

    public IBar askBar(final Instrument instrument,
                       final Period period) {
        return bar(instrument, period, OfferSide.ASK);
    }

    public IBar bidBar(final Instrument instrument,
                       final Period period) {
        return bar(instrument, period, OfferSide.BID);
    }

    public IBar forOfferSide(final Instrument instrument,
                             final Period period,
                             final OfferSide offerSide) {
        return offerSide == OfferSide.ASK
                ? askBar(instrument, period)
                : bidBar(instrument, period);
    }

    private IBar bar(final Instrument instrument,
                     final Period period,
                     final OfferSide offerSide) {
        return latestBarQuote.containsKey(barQuoteKey(instrument, period))
                ? barQuoteByOfferSide(instrument, period, offerSide)
                : barFromHistory(instrument, period, offerSide);
    }

    private IBar barFromHistory(final Instrument instrument,
                                final Period period,
                                final OfferSide offerSide) {
        return Observable.fromCallable(() -> history.getBar(instrument, period, offerSide, 1))
                .flatMap(bar -> {
                    if (bar == null) {
                        logger.error("Last bar for " + instrument + " and period " + period
                                + " from history returned null!");
                        return Observable.error(new QuoteProviderException("History bar is null!"));
                    }
                    return Observable.just(bar);
                })
                .retry(10)
                .onErrorResumeNext(e -> Observable.error(new QuoteProviderException(e.getMessage())))
                .toBlocking()
                .first();
    }

    private IBar barQuoteByOfferSide(final Instrument instrument,
                                     final Period period,
                                     final OfferSide offerSide) {
        final BarQuote barQuote = latestBarQuote.get(barQuoteKey(instrument, period));
        return offerSide == OfferSide.ASK
                ? barQuote.askBar()
                : barQuote.bidBar();
    }

    private void onBarQuote(final BarQuote barQuote) {
        final MultiKey<Object> multiKey = barQuoteKey(barQuote.instrument(), barQuote.period());
        latestBarQuote.put(multiKey, barQuote);
    }

    public void subscribe(final Set<Instrument> instruments,
                          final Period period,
                          final BarQuoteConsumer barQuoteConsumer) {
        barQuoteObservable.filter(barQuote -> instruments.contains(barQuote.instrument())
                && period.equals(barQuote.period()))
                .subscribe(barQuoteConsumer::onBarQuote);
    }

    public Observable<BarQuote> observable() {
        return barQuoteObservable;
    }

    private MultiKey<Object> barQuoteKey(final Instrument instrument,
                                         final Period period) {
        return new MultiKey<Object>(instrument, period);
    }
}
