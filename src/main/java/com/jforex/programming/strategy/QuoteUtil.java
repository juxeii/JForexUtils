package com.jforex.programming.strategy;

import static com.google.common.base.Preconditions.checkNotNull;

import com.dukascopy.api.IBar;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;
import com.jforex.programming.quote.BarParams;
import com.jforex.programming.quote.BarQuote;
import com.jforex.programming.quote.BarQuoteProvider;
import com.jforex.programming.quote.BarQuoteRepository;
import com.jforex.programming.quote.TickQuote;
import com.jforex.programming.quote.TickQuoteProvider;
import com.jforex.programming.quote.TickQuoteRepository;
import com.jforex.programming.rx.JFHotPublisher;

public class QuoteUtil {

    private final ContextUtil contextUtil;
    private final TickQuoteProvider tickQuoteProvider;
    private final TickQuoteRepository tickQuoteRepository;
    private final BarQuoteProvider barQuoteProvider;
    private final BarQuoteRepository barQuoteRepository;
    private final boolean isWeekendQuoteFilter;
    private final JFHotPublisher<TickQuote> tickQuotePublisher = new JFHotPublisher<>();
    private final JFHotPublisher<BarQuote> barQuotePublisher = new JFHotPublisher<>();

    public QuoteUtil(final ContextUtil contextUtil,
                     final boolean isWeekendQuoteFilter) {
        this.contextUtil = contextUtil;
        this.isWeekendQuoteFilter = isWeekendQuoteFilter;
        tickQuoteRepository = new TickQuoteRepository(tickQuotePublisher.observable(),
                                                      contextUtil.historyUtil(),
                                                      contextUtil.context().getSubscribedInstruments());
        tickQuoteProvider = new TickQuoteProvider(tickQuotePublisher.observable(), tickQuoteRepository);
        barQuoteRepository = new BarQuoteRepository(barQuotePublisher.observable(), contextUtil.historyUtil());
        barQuoteProvider = new BarQuoteProvider(this,
                                                barQuotePublisher.observable(),
                                                barQuoteRepository);
    }

    public TickQuoteProvider tickQuoteProvider() {
        return tickQuoteProvider;
    }

    public BarQuoteProvider barQuoteProvider() {
        return barQuoteProvider;
    }

    public void onTick(final Instrument instrument,
                       final ITick tick) {
        if (shouldForwardQuote(tick.getTime())) {
            final TickQuote tickQuote = new TickQuote(instrument, tick);
            tickQuotePublisher.onNext(tickQuote);
        }
    }

    private boolean shouldForwardQuote(final long time) {
        return !isWeekendQuoteFilter
                || !contextUtil.isMarketClosedAtTime(time);
    }

    public void onBar(final Instrument instrument,
                      final Period period,
                      final IBar askBar,
                      final IBar bidBar) {
        checkNotNull(instrument);
        checkNotNull(period);
        checkNotNull(askBar);
        checkNotNull(bidBar);

        onOfferSidedBar(instrument,
                        period,
                        OfferSide.ASK,
                        askBar);
        onOfferSidedBar(instrument,
                        period,
                        OfferSide.BID,
                        bidBar);
    }

    private final void onOfferSidedBar(final Instrument instrument,
                                       final Period period,
                                       final OfferSide offerside,
                                       final IBar bar) {
        if (shouldForwardQuote(bar.getTime())) {
            final BarParams quoteParams = BarParams
                .forInstrument(instrument)
                .period(period)
                .offerSide(offerside);
            final BarQuote barQuote = new BarQuote(bar, quoteParams);
            barQuotePublisher.onNext(barQuote);
        }
    }

    public void initBarsFeed(final BarParams barParams) {
        contextUtil.initBarsFeed(barParams, this::onOfferSidedBar);
    }

    public void onStop() {
        tickQuotePublisher.unsubscribe();
        barQuotePublisher.unsubscribe();
    }
}
