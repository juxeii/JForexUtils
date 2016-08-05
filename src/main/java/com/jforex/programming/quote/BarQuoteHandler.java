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
    public IBar bar(final BarParams barParams) {
        return barQuoteRepository
            .get(checkNotNull(barParams))
            .bar();
    }

    @Override
    public Observable<BarQuote> observableForParamsList(final List<BarParams> barParamsList) {
        final List<Observable<BarQuote>> paramsObservables = checkNotNull(barParamsList)
            .stream()
            .map(params -> {
                if (params.period().name() == null)
                    jforexUtil.subscribeToBarsFeed(params);
                return observableForFilter(params);
            })
            .collect(Collectors.toList());

        return Observable.merge(paramsObservables);
    }

    private Observable<BarQuote> observableForFilter(final BarParams barParams) {
        return barQuoteObservable
            .filter(barQuote -> barQuote.instrument() == barParams.instrument())
            .filter(barQuote -> barParams.period().compareTo(barQuote.period()) == 0)
            .filter(barQuote -> barQuote.offerSide() == barParams.offerSide());
    }

    @Override
    public Observable<BarQuote> observable() {
        return barQuoteObservable;
    }
}
