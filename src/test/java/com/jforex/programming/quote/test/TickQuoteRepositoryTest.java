package com.jforex.programming.quote.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.dukascopy.api.Instrument;
import com.google.common.collect.Sets;
import com.jforex.programming.quote.TickQuote;
import com.jforex.programming.quote.TickQuoteRepository;
import com.jforex.programming.test.common.QuoteProviderForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

@RunWith(HierarchicalContextRunner.class)
public class TickQuoteRepositoryTest extends QuoteProviderForTest {

    private TickQuoteRepository tickQuoteRepository;

    private final Map<Instrument, TickQuote> historyQuotes = new HashMap<>();
    private final Subject<TickQuote, TickQuote> quoteObservable = PublishSubject.create();
    private final Set<Instrument> subscribedInstruments = Sets.newHashSet(instrumentEURUSD,
                                                                          instrumentAUDUSD);

    @Before
    public void setUp() {
        setUpMocks();
        historyQuotes.put(instrumentEURUSD, tickQuoteEURUSD);
        historyQuotes.put(instrumentAUDUSD, tickQuoteAUDUSD);

        tickQuoteRepository = new TickQuoteRepository(quoteObservable,
                                                      historyUtilMock,
                                                      subscribedInstruments);
    }

    private void setUpMocks() {
        when(historyUtilMock.tickQuotes(subscribedInstruments))
                .thenReturn(historyQuotes);
    }

    public class BeforeTicksReceived {

        @Test
        public void quotesForSubscribedInstrumentsComeFromHistory() {
            verify(historyUtilMock).tickQuotes(subscribedInstruments);
        }

        @Test
        public void quoteForEURUSDComesFromHistory() {
            assertThat(tickQuoteRepository.get(instrumentEURUSD),
                       equalTo(tickQuoteEURUSD));
        }

        @Test
        public void quoteForAUDUSDComesFromHistory() {
            assertThat(tickQuoteRepository.get(instrumentAUDUSD),
                       equalTo(tickQuoteAUDUSD));
        }

        public class AfterReceivedQuotes {

            @Before
            public void setUp() {
                quoteObservable.onNext(tickQuoteEURUSD);
                quoteObservable.onNext(tickQuoteAUDUSD);
            }

            @Test
            public void quoteForEURUSDComesFromObservable() {
                verifyNoMoreInteractions(historyMock);

                assertThat(tickQuoteRepository.get(instrumentEURUSD),
                           equalTo(tickQuoteEURUSD));
            }

            @Test
            public void quoteForAUDUSDComesFromObservable() {
                verifyNoMoreInteractions(historyMock);

                assertThat(tickQuoteRepository.get(instrumentAUDUSD),
                           equalTo(tickQuoteAUDUSD));
            }
        }
    }
}
