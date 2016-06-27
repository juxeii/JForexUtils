package com.jforex.programming.quote;

import java.util.List;
import java.util.stream.Collectors;

import com.dukascopy.api.IBar;
import com.jforex.programming.builder.BarQuoteFilter;
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
    public IBar quote(final BarQuoteFilter barQuoteFilter) {
        return barQuoteRepository.get(barQuoteFilter);
    }

    @Override
    public Observable<BarQuote> observableForFilters(final List<BarQuoteFilter> filters) {
        final List<Observable<BarQuote>> filterObservables = filters
                .stream()
                .map(filter -> {
                    if (filter.period().name() == null)
                        jforexUtil.subscribeToBarsFeed(filter);
                    return observableForFilter(filter);
                })
                .collect(Collectors.toList());

        return Observable.merge(filterObservables);
    }

    private Observable<BarQuote> observableForFilter(final BarQuoteFilter barQuoteFilter) {
        return barQuoteObservable
                .filter(barQuote -> barQuote.instrument() == barQuoteFilter.instrument())
                .filter(barQuote -> barQuoteFilter.period().compareTo(barQuote.period()) == 0)
                .filter(barQuote -> barQuote.offerSide() == barQuoteFilter.offerSide());
    }

    @Override
    public Observable<BarQuote> observable() {
        return barQuoteObservable;
    }
}
