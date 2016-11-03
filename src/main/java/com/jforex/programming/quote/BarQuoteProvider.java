package com.jforex.programming.quote;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import com.dukascopy.api.IBar;
import com.jforex.programming.strategy.QuoteUtil;

import io.reactivex.Observable;

public class BarQuoteProvider {

    private final QuoteUtil quoteUtil;
    private final Observable<BarQuote> barQuoteObservable;
    private final BarQuoteRepository barQuoteRepository;

    public BarQuoteProvider(final QuoteUtil quoteUtil,
                            final Observable<BarQuote> barQuoteObservable,
                            final BarQuoteRepository barQuoteRepository) {
        this.quoteUtil = quoteUtil;
        this.barQuoteObservable = barQuoteObservable;
        this.barQuoteRepository = barQuoteRepository;
    }

    public IBar bar(final BarParams barParams) {
        checkNotNull(barParams);

        return barQuoteRepository
            .get(barParams)
            .bar();
    }

    public Observable<BarQuote> observableForParamsList(final List<BarParams> barParamsList) {
        checkNotNull(barParamsList);

        return Observable.merge(Observable
            .fromIterable(barParamsList)
            .map(this::observableForParams)
            .toList()
            .blockingGet());
    }

    private final Observable<BarQuote> observableForParams(final BarParams barParams) {
        checkNotNull(barParams);

        if (barParams.period().name() == null)
            quoteUtil.initBarsFeed(barParams);
        return barQuoteObservable.filter(barQuote -> barQuote.barParams().equals(barParams));
    }

    public Observable<BarQuote> observable() {
        return barQuoteObservable;
    }
}
