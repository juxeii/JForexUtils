package com.jforex.programming.quote;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Set;

import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.OfferSide;

import io.reactivex.Flowable;

public class TickQuoteHandler implements TickQuoteProvider {

    private final Flowable<TickQuote> tickQuoteObservable;
    private final TickQuoteRepository tickQuoteRepository;

    public TickQuoteHandler(final Flowable<TickQuote> tickQuoteObservable,
                            final TickQuoteRepository tickQuoteRepository) {
        this.tickQuoteObservable = tickQuoteObservable;
        this.tickQuoteRepository = tickQuoteRepository;
    }

    @Override
    public ITick tick(final Instrument instrument) {
        return tickQuoteRepository
            .get(checkNotNull(instrument))
            .tick();
    }

    @Override
    public double ask(final Instrument instrument) {
        return tick(checkNotNull(instrument)).getAsk();
    }

    @Override
    public double bid(final Instrument instrument) {
        return tick(checkNotNull(instrument)).getBid();
    }

    @Override
    public double forOfferSide(final Instrument instrument,
                               final OfferSide offerSide) {
        checkNotNull(instrument);
        checkNotNull(offerSide);

        return offerSide == OfferSide.BID
                ? bid(instrument)
                : ask(instrument);
    }

    @Override
    public Flowable<TickQuote> observable() {
        return tickQuoteObservable;
    }

    @Override
    public Flowable<TickQuote> observableForInstruments(final Set<Instrument> instruments) {
        checkNotNull(instruments);

        return tickQuoteObservable
            .filter(tickQuote -> instruments.contains(tickQuote.instrument()));
    }
}
