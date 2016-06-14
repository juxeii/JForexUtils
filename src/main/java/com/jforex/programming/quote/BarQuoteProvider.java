package com.jforex.programming.quote;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.aeonbits.owner.ConfigFactory;
import org.apache.commons.collections4.keyvalue.MultiKey;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IBar;
import com.dukascopy.api.IContext;
import com.dukascopy.api.IHistory;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;
import com.dukascopy.api.feed.IBarFeedListener;
import com.jforex.programming.builder.BarQuoteSubscription;
import com.jforex.programming.misc.JFHotObservable;
import com.jforex.programming.misc.JForexUtil;
import com.jforex.programming.settings.UserSettings;

import rx.Observable;

public class BarQuoteProvider implements IBarFeedListener {

    private final JForexUtil jforexUtil;
    private final IContext context;
    private final IHistory history;
    private final Observable<BarQuote> barQuoteObservable;
    private final JFHotObservable<BarQuote> customBarQuotePublisher = new JFHotObservable<>();
    private final Map<MultiKey<Object>, BarQuote> latestBarQuote = new ConcurrentHashMap<>();

    private final static UserSettings userSettings = ConfigFactory.create(UserSettings.class);
    private final static Logger logger = LogManager.getLogger(BarQuoteProvider.class);

    public BarQuoteProvider(final JForexUtil jforexUtil,
                            final Observable<BarQuote> barQuoteObservable) {
        this.jforexUtil = jforexUtil;
        this.barQuoteObservable = barQuoteObservable;
        context = jforexUtil.context();
        history = jforexUtil.history();

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

    private void onBarQuote(final BarQuote barQuote) {
        final MultiKey<Object> multiKey = barQuoteKey(barQuote.instrument(),
                                                      barQuote.period(),
                                                      barQuote.offerSide());
        latestBarQuote.put(multiKey, barQuote);
    }

    public Observable<BarQuote> subscribe(final BarQuoteSubscription subscription) {
        final Period subscriptionPeriod = subscription.period();
        if (subscriptionPeriod.name() != null) {
            return barQuoteObservable
                    .filter(barQuote -> subscription.instruments().contains(barQuote.instrument()))
                    .filter(barQuote -> subscriptionPeriod.compareTo(barQuote.period()) == 0)
                    .filter(barQuote -> barQuote.offerSide() == subscription.offerSide());
        } else {
            subscription.instruments().forEach(instrument -> context.subscribeToBarsFeed(instrument,
                                                                                         subscriptionPeriod,
                                                                                         subscription.offerSide(),
                                                                                         this));
            return customBarQuotePublisher.get()
                    .filter(barQuote -> subscription.instruments().contains(barQuote.instrument()))
                    .filter(barQuote -> subscriptionPeriod.compareTo(barQuote.period()) == 0)
                    .filter(barQuote -> barQuote.offerSide() == subscription.offerSide());
        }
    }

    public Observable<BarQuote> observable() {
        return barQuoteObservable;
    }

    private MultiKey<Object> barQuoteKey(final Instrument instrument,
                                         final Period period,
                                         final OfferSide offerSide) {
        return new MultiKey<Object>(instrument, period, offerSide);
    }

    @Override
    public void onBar(final Instrument instrument,
                      final Period period,
                      final OfferSide offerSide,
                      final IBar bar) {
        if (userSettings.enableWeekendQuoteFilter() && !jforexUtil.isMarketClosed(bar.getTime()))
            customBarQuotePublisher.onNext(new BarQuote(bar, instrument, period, offerSide));
    }
}
