package com.jforex.programming.quote;

import java.util.List;

import com.dukascopy.api.IBar;

import io.reactivex.Observable;

public interface BarQuoteProvider {

    public IBar bar(BarParams barParams);

    public Observable<BarQuote> observableForParamsList(List<BarParams> barParamsList);

    public Observable<BarQuote> observable();
}
