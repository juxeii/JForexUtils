package com.jforex.programming.quote;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Set;

import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.OfferSide;

import io.reactivex.Observable;

public class TickQuoteProvider {

    private final Observable<TickQuote> tickQuoteObservable;
    private final TickQuoteRepository tickQuoteRepository;

    public TickQuoteProvider(final Observable<TickQuote> tickQuoteObservable,
                             final TickQuoteRepository tickQuoteRepository) {
        this.tickQuoteObservable = tickQuoteObservable;
        this.tickQuoteRepository = tickQuoteRepository;
    }

    public ITick tick(final Instrument instrument) {
        checkNotNull(instrument);

        return tickQuoteRepository
            .get(instrument)
            .tick();
    }

    public double ask(final Instrument instrument) {
        checkNotNull(instrument);

        return tick(instrument).getAsk();
    }

    public double bid(final Instrument instrument) {
        checkNotNull(instrument);

        return tick(instrument).getBid();
    }

    public double forOfferSide(final Instrument instrument,
                               final OfferSide offerSide) {
        checkNotNull(instrument);
        checkNotNull(offerSide);

        return offerSide == OfferSide.BID
                ? bid(instrument)
                : ask(instrument);
    }

    public Observable<TickQuote> observable() {
        return tickQuoteObservable;
    }

    public Observable<TickQuote> observableForInstruments(final Set<Instrument> instruments) {
        checkNotNull(instruments);

        return tickQuoteObservable
            .filter(tickQuote -> instruments.contains(tickQuote.instrument()));
    }

    public TickQuoteRepository repository() {
        return tickQuoteRepository;
    }
}
