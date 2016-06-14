package com.jforex.programming.quote;

import java.util.Set;

import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.OfferSide;

import rx.Observable;

public interface TickQuoteProvider {

    public ITick tick(final Instrument instrument);

    public double ask(final Instrument instrument);

    public double bid(final Instrument instrument);

    public double forOfferSide(final Instrument instrument,
                               final OfferSide offerSide);

    public Observable<TickQuote> quoteObservable(final Set<Instrument> instruments);

    public Observable<TickQuote> observable();
}
