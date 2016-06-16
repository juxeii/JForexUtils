package com.jforex.programming.quote;

import java.util.Set;

import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.OfferSide;

import rx.Observable;

public class TickQuoteHandler implements TickQuoteProvider {

    private final Observable<TickQuote> tickQuoteObservable;
    private final TickQuoteRepository tickQuoteRepository;

    public TickQuoteHandler(final Observable<TickQuote> tickQuoteObservable,
                            final TickQuoteRepository tickQuoteRepository) {
        this.tickQuoteObservable = tickQuoteObservable;
        this.tickQuoteRepository = tickQuoteRepository;
    }

    @Override
    public ITick tick(final Instrument instrument) {
        return tickQuoteRepository.get(instrument);
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
    public Observable<TickQuote> observableForInstruments(final Set<Instrument> instruments) {
        return tickQuoteObservable
                .filter(tickQuote -> instruments.contains(tickQuote.instrument()));
    }
}
