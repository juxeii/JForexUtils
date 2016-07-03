package com.jforex.programming.quote;

import java.util.List;
import java.util.stream.Collectors;

import com.jforex.programming.misc.JForexUtil;

import com.dukascopy.api.IBar;

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
    public IBar quote(final BarQuoteParams barQuoteParams) {
        return barQuoteRepository.get(barQuoteParams).bar();
    }

    @Override
    public Observable<BarQuote> observableForFilters(final List<BarQuoteParams> barQuoteParams) {
        final List<Observable<BarQuote>> paramsObservables = barQuoteParams
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
