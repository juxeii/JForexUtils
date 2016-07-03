package com.jforex.programming.quote;

import java.util.List;

import com.dukascopy.api.IBar;

import rx.Observable;

public interface BarQuoteProvider {

    public IBar quote(BarQuoteParams filter);

    public Observable<BarQuote> observableForFilters(final List<BarQuoteParams> filters);

    public Observable<BarQuote> observable();
}
