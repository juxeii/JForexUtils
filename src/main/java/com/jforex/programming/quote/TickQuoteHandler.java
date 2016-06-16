package com.jforex.programming.quote;

import java.util.Map;
import java.util.Set;

import org.aeonbits.owner.ConfigFactory;

import com.jforex.programming.misc.HistoryUtil;
import com.jforex.programming.misc.JFHotObservable;
import com.jforex.programming.misc.JForexUtil;
import com.jforex.programming.settings.UserSettings;

import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.OfferSide;

import rx.Observable;

public class TickQuoteHandler implements TickQuoteProvider {

    private final JForexUtil jforexUtil;
    private final JFHotObservable<TickQuote> tickQuotePublisher = new JFHotObservable<>();
    private final Observable<TickQuote> tickQuoteObservable = tickQuotePublisher.observable();
    private final Map<Instrument, ITick> latestTickQuote;

    private final static UserSettings userSettings = ConfigFactory.create(UserSettings.class);

    public TickQuoteHandler(final JForexUtil jforexUtil,
                            final HistoryUtil historyUtil,
                            final Set<Instrument> subscribedInstruments) {
        this.jforexUtil = jforexUtil;

        latestTickQuote = historyUtil.latestTicks(subscribedInstruments);
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
        if (!userSettings.enableWeekendQuoteFilter() || !jforexUtil.isMarketClosed(tick.getTime()))
            onTickQuote(new TickQuote(instrument, tick));
    }

    private void onTickQuote(final TickQuote tickQuote) {
        latestTickQuote.put(tickQuote.instrument(), tickQuote.tick());
        tickQuotePublisher.onNext(tickQuote);
    }
}
