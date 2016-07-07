package com.jforex.programming.quote;

import java.util.List;

import com.dukascopy.api.IBar;

import rx.Observable;

public interface BarQuoteProvider {

    public IBar quote(BarQuoteParams barQuoteParams);

    public Observable<BarQuote> observableForFilters(final List<BarQuoteParams> filters);

    public Observable<BarQuote> observable();
}
