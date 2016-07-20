package com.jforex.programming.quote;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;
import java.util.stream.Collectors;

import com.dukascopy.api.IBar;
import com.jforex.programming.misc.JForexUtil;

import rx.Observable;

public class BarQuoteHandler implements BarQuoteProvider {

    private final JForexUtil jforexUtil;
    private final Observable<BarQuote> barQuoteObservable;
    private final BarQuoteRepository barQuoteRepository;

    public BarQuoteHandler(final JForexUtil jforexUtil,
                           final Observable<BarQuote> barQuoteObservable,
                           final BarQuoteRepository barQuoteRepository) {
        this.jforexUtil = jforexUtil;
        this.barQuoteObservable = barQuoteObservable;
        this.barQuoteRepository = barQuoteRepository;
    }

    @Override
    public IBar bar(final BarQuoteParams barQuoteParams) {
        return barQuoteRepository
                .get(checkNotNull(barQuoteParams))
                .bar();
    }

    @Override
    public Observable<BarQuote>
           observableForFilters(final List<BarQuoteParams> barQuoteParamsList) {
        final List<Observable<BarQuote>> paramsObservables = checkNotNull(barQuoteParamsList)
                .stream()
                .map(params -> {
                    if (params.period().name() == null)
                        jforexUtil.subscribeToBarsFeed(params);
                    return observableForFilter(params);
                })
                .collect(Collectors.toList());

        return Observable.merge(paramsObservables);
    }

    private Observable<BarQuote> observableForFilter(final BarQuoteParams barQuoteParams) {
        return barQuoteObservable
                .filter(barQuote -> barQuote.instrument() == barQuoteParams.instrument())
                .filter(barQuote -> barQuoteParams.period().compareTo(barQuote.period()) == 0)
                .filter(barQuote -> barQuote.offerSide() == barQuoteParams.offerSide());
    }

    @Override
    public Observable<BarQuote> observable() {
        return barQuoteObservable;
    }
}
