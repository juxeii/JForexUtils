package com.jforex.programming.quote;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.aeonbits.owner.ConfigFactory;
import org.apache.commons.collections4.keyvalue.MultiKey;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jforex.programming.builder.BarQuoteSubscription;
import com.jforex.programming.misc.JFHotObservable;
import com.jforex.programming.misc.JForexUtil;
import com.jforex.programming.settings.UserSettings;

import com.dukascopy.api.IBar;
import com.dukascopy.api.IContext;
import com.dukascopy.api.IHistory;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;

import rx.Observable;

public class BarQuoteHandler implements BarQuoteProvider {

    private final JForexUtil jforexUtil;
    private final IContext context;
    private final IHistory history;
    private final JFHotObservable<BarQuote> barQuotePublisher = new JFHotObservable<>();
    private final Observable<BarQuote> barQuoteObservable = barQuotePublisher.get();
    private final Map<MultiKey<Object>, BarQuote> latestBarQuote = new ConcurrentHashMap<>();

    private final static UserSettings userSettings = ConfigFactory.create(UserSettings.class);
    private final static Logger logger = LogManager.getLogger(BarQuoteHandler.class);

    public BarQuoteHandler(final JForexUtil jforexUtil) {
        this.jforexUtil = jforexUtil;
        context = jforexUtil.context();
        history = jforexUtil.history();
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
        return latestBarQuote.containsKey(barQuoteKey(instrument, period, offerSide))
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
        final BarQuote barQuote = latestBarQuote.get(barQuoteKey(instrument, period, offerSide));
        return barQuote.bar();
    }

    @Override
    public Observable<BarQuote> quoteObservable(final BarQuoteSubscription subscription) {
        final Period subscriptionPeriod = subscription.period();
        if (subscriptionPeriod.name() == null) {
            subscription.instruments().forEach(instrument -> context.subscribeToBarsFeed(instrument,
                                                                                         subscriptionPeriod,
                                                                                         subscription.offerSide(),
                                                                                         this::onBar));
        }
        return barQuoteObservable
                .filter(barQuote -> subscription.instruments().contains(barQuote.instrument()))
                .filter(barQuote -> subscriptionPeriod.compareTo(barQuote.period()) == 0)
                .filter(barQuote -> barQuote.offerSide() == subscription.offerSide());
    }

    @Override
    public Observable<BarQuote> observable() {
        return barQuoteObservable;
    }

    private MultiKey<Object> barQuoteKey(final Instrument instrument,
                                         final Period period,
                                         final OfferSide offerSide) {
        return new MultiKey<Object>(instrument, period, offerSide);
    }

    public void onBar(final Instrument instrument,
                      final Period period,
                      final OfferSide offerSide,
                      final IBar bar) {
        if (userSettings.enableWeekendQuoteFilter() && !jforexUtil.isMarketClosed(bar.getTime()))
            onBarQuote(new BarQuote(bar, instrument, period, offerSide));
    }

    private void onBarQuote(final BarQuote barQuote) {
        final MultiKey<Object> multiKey = barQuoteKey(barQuote.instrument(),
                                                      barQuote.period(),
                                                      barQuote.offerSide());
        latestBarQuote.put(multiKey, barQuote);
        barQuotePublisher.onNext(barQuote);
    }
}
