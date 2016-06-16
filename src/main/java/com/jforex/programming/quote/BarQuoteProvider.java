package com.jforex.programming.quote;

import com.jforex.programming.builder.BarQuoteSubscription;

import com.dukascopy.api.IBar;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;

import rx.Observable;

public interface BarQuoteProvider {

    public IBar askBar(final Instrument instrument,
                       final Period period);

    public IBar bidBar(final Instrument instrument,
                       final Period period);

    public IBar forOfferSide(final Instrument instrument,
                             final Period period,
                             final OfferSide offerSide);

    public Observable<BarQuote> observableForSubscription(final BarQuoteSubscription subscription);

    public Observable<BarQuote> observable();
}
