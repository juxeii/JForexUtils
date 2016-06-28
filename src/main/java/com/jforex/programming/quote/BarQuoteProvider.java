package com.jforex.programming.quote;

import java.util.List;

import com.dukascopy.api.IBar;

import rx.Observable;

public interface BarQuoteProvider {

    public IBar quote(BarQuoteFilter barQuoteFilter);

    public Observable<BarQuote> observableForFilters(final List<BarQuoteFilter> filters);

    public Observable<BarQuote> observable();
}
