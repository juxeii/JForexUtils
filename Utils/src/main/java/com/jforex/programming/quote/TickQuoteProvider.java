package com.jforex.programming.quote;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.dukascopy.api.IHistory;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.OfferSide;

import rx.Observable;

public class TickQuoteProvider {

    private final Observable<TickQuote> tickObservable;
    private final IHistory history;
    private final Map<Instrument, ITick> latestTickByInstrument = new ConcurrentHashMap<>();

    public TickQuoteProvider(final Observable<TickQuote> tickObservable,
                             final IHistory history) {
        this.tickObservable = tickObservable;
        this.history = history;

        tickObservable.subscribe(this::onTickQuote);
    }

    public ITick tick(final Instrument instrument) {
        return latestTickByInstrument.getOrDefault(instrument, tickFromHistory(instrument));
    }

    private ITick tickFromHistory(final Instrument instrument) {
        try {
            final ITick historyTick = history.getLastTick(instrument);
            if (historyTick == null)
                throw new QuoteProviderException("Last tick for " + instrument + " from history returned null!");
            return historyTick;
        } catch (final JFException e) {
            throw new QuoteProviderException("Exception occured while retreiving quote for " + instrument
                    + " Message: " + e.getMessage());
        }
    }

    public double ask(final Instrument instrument) {
        return tick(instrument).getAsk();
    }

    public double bid(final Instrument instrument) {
        return tick(instrument).getBid();
    }

    public double forOfferSide(final Instrument instrument,
                               final OfferSide offerSide) {
        return offerSide == OfferSide.BID ? bid(instrument) : ask(instrument);
    }

    public Observable<TickQuote> observable() {
        return tickObservable;
    }

    public void subscribe(final Instrument instrument,
                          final TickQuoteConsumer tickQuoteConsumer) {
        tickObservable.filter(tickQuote -> instrument == tickQuote.instrument())
                      .subscribe(tickQuoteConsumer::onTickQuote);
    }

    private void onTickQuote(final TickQuote tickQuote) {
        latestTickByInstrument.put(tickQuote.instrument(), tickQuote.tick());
    }
}
