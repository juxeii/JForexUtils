package com.jforex.programming.quote;

import java.util.List;

import com.dukascopy.api.IBar;

import rx.Observable;

public interface BarQuoteProvider {

    public IBar bar(BarParams barParams);

    public Observable<BarQuote> observableForParams(final List<BarParams> params);

    public Observable<BarQuote> observable();
}
