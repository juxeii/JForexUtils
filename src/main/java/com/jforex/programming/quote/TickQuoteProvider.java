package com.jforex.programming.quote;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.dukascopy.api.IHistory;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.OfferSide;

import rx.Observable;

public class TickQuoteProvider {

    private final Observable<TickQuote> tickQuoteObservable;
    private final Set<Instrument> subscribedInstruments;
    private final IHistory history;
    private final Map<Instrument, ITick> latestTickQuote = new ConcurrentHashMap<>();

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
        subscribedInstruments.forEach(inst -> {
            final ITick latestTick = tickFromHistory(inst);
            latestTickQuote.put(inst, latestTick);
        });
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
        return offerSide == OfferSide.BID ? bid(instrument) : ask(instrument);
    }

    public Observable<TickQuote> observable() {
        return tickQuoteObservable;
    }

    public void subscribe(final Set<Instrument> instruments,
                          final TickQuoteConsumer tickQuoteConsumer) {
        tickQuoteObservable.filter(tickQuote -> instruments.contains(tickQuote.instrument()))
                           .subscribe(tickQuoteConsumer::onTickQuote);
    }

    private void onTickQuote(final TickQuote tickQuote) {
        latestTickQuote.put(tickQuote.instrument(), tickQuote.tick());
    }
}
