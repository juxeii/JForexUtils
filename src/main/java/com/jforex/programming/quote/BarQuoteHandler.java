package com.jforex.programming.quote;

import com.dukascopy.api.IBar;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;
import com.jforex.programming.builder.BarQuoteParams;
import com.jforex.programming.misc.JForexUtil;

import rx.Observable;

public class BarQuoteHandler implements BarQuoteProvider {

    private final JForexUtil jforexUtil;
    private final Observable<BarQuote> barQuoteObservable;
    private final BarQuoteRepository barQuoteRepository;

    public BarQuoteHandler(final JForexUtil jforexUtil,
                           final Observable<BarQuote> barQuoteObservable,
                           final BarQuoteRepository barQuoteRepository) {
        this.jforexUtil = jforexUtil;
        this.barQuoteObservable = barQuoteObservable;
        this.barQuoteRepository = barQuoteRepository;
    }

    @Override
    public IBar askBar(final Instrument instrument,
                       final Period period) {
        return bar(instrument, period, OfferSide.ASK);
    }

    @Override
    public IBar bidBar(final Instrument instrument,
                       final Period period) {
        return bar(instrument, period, OfferSide.BID);
    }

    @Override
    public IBar forOfferSide(final Instrument instrument,
                             final Period period,
                             final OfferSide offerSide) {
        return bar(instrument, period, offerSide);
    }

    private IBar bar(final Instrument instrument,
                     final Period period,
                     final OfferSide offerSide) {
        return barQuoteRepository.get(instrument, period, offerSide);
    }

    @Override
    public Observable<BarQuote> observableForSubscription(final BarQuoteParams subscription) {
        if (subscription.period().name() == null)
            subscribeInstrumentsToContext(subscription);
        return quoteFilterObservable(subscription);
    }

    private void subscribeInstrumentsToContext(final BarQuoteParams subscription) {
        subscription.instruments()
                .forEach(instrument -> jforexUtil.subscribeToBarsFeed(instrument,
                                                                      subscription.period(),
                                                                      subscription.offerSide()));
    }

    private Observable<BarQuote> quoteFilterObservable(final BarQuoteParams subscription) {
        return barQuoteObservable
                .filter(barQuote -> subscription.instruments().contains(barQuote.instrument()))
                .filter(barQuote -> subscription.period().compareTo(barQuote.period()) == 0)
                .filter(barQuote -> barQuote.offerSide() == subscription.offerSide());
    }

    @Override
    public Observable<BarQuote> observable() {
        return barQuoteObservable;
    }
}
