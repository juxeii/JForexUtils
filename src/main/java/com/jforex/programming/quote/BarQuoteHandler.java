package com.jforex.programming.quote;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;
import java.util.stream.Collectors;

import com.dukascopy.api.IBar;
import com.jforex.programming.misc.JForexUtil;

import io.reactivex.Flowable;

public class BarQuoteHandler implements BarQuoteProvider {

    private final JForexUtil jforexUtil;
    private final Flowable<BarQuote> barQuoteObservable;
    private final BarQuoteRepository barQuoteRepository;

    public BarQuoteHandler(final JForexUtil jforexUtil,
                           final Flowable<BarQuote> barQuoteObservable,
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
    public Flowable<BarQuote> observableForParamsList(final List<BarParams> barParamsList) {
        final List<Flowable<BarQuote>> paramsObservables = checkNotNull(barParamsList)
            .stream()
            .map(barParams -> {
                if (barParams.period().name() == null)
                    jforexUtil.subscribeToBarsFeed(barParams);
                return barQuoteObservable
                    .filter(barQuote -> barQuote.barParams().equals(barParams));
            })
            .collect(Collectors.toList());

        return Flowable.merge(paramsObservables);
    }

    @Override
    public Flowable<BarQuote> observable() {
        return barQuoteObservable;
    }
}
