package com.jforex.programming.object;

import com.jforex.programming.misc.HistoryUtil;
import com.jforex.programming.misc.JFHotPublisher;
import com.jforex.programming.misc.JForexUtil;
import com.jforex.programming.quote.BarQuote;
import com.jforex.programming.quote.BarQuoteProvider;
import com.jforex.programming.quote.BarQuoteRepository;
import com.jforex.programming.quote.TickQuote;
import com.jforex.programming.quote.TickQuoteProvider;
import com.jforex.programming.quote.TickQuoteRepository;

public class QuoteObjects {

    private final TickQuoteProvider tickQuoteProvider;
    private final TickQuoteRepository tickQuoteRepository;
    private final BarQuoteProvider barQuoteProvider;
    private final BarQuoteRepository barQuoteRepository;

    public QuoteObjects(final JForexUtil jForexUtil,
                        final HistoryUtil historyUtil,
                        final JFHotPublisher<TickQuote> tickQuotePublisher,
                        final JFHotPublisher<BarQuote> barQuotePublisher) {
        tickQuoteRepository = new TickQuoteRepository(tickQuotePublisher.observable(),
                                                      historyUtil,
                                                      jForexUtil.context().getSubscribedInstruments());
        tickQuoteProvider = new TickQuoteProvider(tickQuotePublisher.observable(), tickQuoteRepository);
        barQuoteRepository = new BarQuoteRepository(barQuotePublisher.observable(), historyUtil);
        barQuoteProvider = new BarQuoteProvider(jForexUtil,
                                                barQuotePublisher.observable(),
                                                barQuoteRepository);
    }

    public TickQuoteProvider tickQuoteProvider() {
        return tickQuoteProvider;
    }

    public BarQuoteProvider barQuoteProvider() {
        return barQuoteProvider;
    }
}
