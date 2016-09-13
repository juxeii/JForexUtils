package com.jforex.programming.quote;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;
import java.util.stream.Collectors;

import com.dukascopy.api.IBar;
import com.jforex.programming.misc.JForexUtil;

import io.reactivex.Observable;

public class BarQuoteProvider {

    private final JForexUtil jforexUtil;
    private final Observable<BarQuote> barQuoteObservable;
    private final BarQuoteRepository barQuoteRepository;

    public BarQuoteProvider(final JForexUtil jforexUtil,
                            final Observable<BarQuote> barQuoteObservable,
                            final BarQuoteRepository barQuoteRepository) {
        this.jforexUtil = jforexUtil;
        this.barQuoteObservable = barQuoteObservable;
        this.barQuoteRepository = barQuoteRepository;
    }

    public IBar bar(final BarParams barParams) {
        return barQuoteRepository
            .get(checkNotNull(barParams))
            .bar();
    }

    public Observable<BarQuote> observableForParamsList(final List<BarParams> barParamsList) {
        final List<Observable<BarQuote>> paramsObservables = checkNotNull(barParamsList)
            .stream()
            .map(barParams -> {
                if (barParams.period().name() == null)
                    jforexUtil.subscribeToBarsFeed(barParams);
                return barQuoteObservable
                    .filter(barQuote -> barQuote.barParams().equals(barParams));
            })
            .collect(Collectors.toList());

        return Observable.merge(paramsObservables);
    }

    public Observable<BarQuote> observable() {
        return barQuoteObservable;
    }
}
