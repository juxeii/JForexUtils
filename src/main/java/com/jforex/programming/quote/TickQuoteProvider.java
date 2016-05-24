package com.jforex.programming.quote;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IHistory;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.OfferSide;

import rx.Observable;

public class TickQuoteProvider {

    private final Observable<TickQuote> tickQuoteObservable;
    private final Set<Instrument> subscribedInstruments;
    private final IHistory history;
    private final Map<Instrument, ITick> latestTickQuote = new ConcurrentHashMap<>();

    private final static Logger logger = LogManager.getLogger(TickQuoteProvider.class);

    public TickQuoteProvider(final Observable<TickQuote> tickQuoteObservable,
                             final Set<Instrument> subscribedInstruments,
                             final IHistory history) {
        this.tickQuoteObservable = tickQuoteObservable;
        this.subscribedInstruments = subscribedInstruments;
        this.history = history;

        initLatestTicksFromHistory();
        tickQuoteObservable.subscribe(this::onTickQuote);
    }

    private void initLatestTicksFromHistory() {
        subscribedInstruments.forEach(instrument -> latestTickQuote.put(instrument, tickFromHistory(instrument)));
    }

    private ITick tickFromHistory(final Instrument instrument) {
        try {
            ITick historyTick = null;
            for (int i = 0; i < 10; ++i) {
                historyTick = history.getLastTick(instrument);
                if (historyTick == null) {
                    logger.warn("Last tick for " + instrument +
                            " from history returned null! Retry no " + (i + 1) + " starts...");
                    Thread.sleep(500L);
                } else
                    break;
            }
            return historyTick;
        } catch (final JFException e) {
            throw new QuoteProviderException("Exception occured while retreiving quote for " + instrument
                    + " Message: " + e.getMessage());
        } catch (final InterruptedException e) {
            throw new QuoteProviderException("InterruptedException occured while retry waiting! " + e.getMessage());
        }
    }

    public ITick tick(final Instrument instrument) {
        return latestTickQuote.get(instrument);
    }

    public double ask(final Instrument instrument) {
        return tick(instrument).getAsk();
    }

    public double bid(final Instrument instrument) {
        return tick(instrument).getBid();
    }

    public double forOfferSide(final Instrument instrument,
                               final OfferSide offerSide) {
        return offerSide == OfferSide.BID
                ? bid(instrument)
                : ask(instrument);
    }

    public Observable<TickQuote> observable() {
        return tickQuoteObservable;
    }

    public void subscribe(final Set<Instrument> instruments,
                          final TickQuoteConsumer tickQuoteConsumer) {
        tickQuoteObservable.filter(tickQuote -> instruments.contains(tickQuote.instrument()))
                .subscribe(tickQuoteConsumer::onTickQuote);
    }

    private void onTickQuote(final TickQuote tickQuote) {
        latestTickQuote.put(tickQuote.instrument(), tickQuote.tick());
    }
}
