package com.jforex.programming.quote;

import java.util.List;

import com.dukascopy.api.IBar;

import io.reactivex.Flowable;

public interface BarQuoteProvider {

    public IBar bar(BarParams barParams);

    public Flowable<BarQuote> observableForParamsList(List<BarParams> barParamsList);

    public Flowable<BarQuote> observable();
}
