package com.jforex.programming.quote.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.dukascopy.api.Instrument;
import com.google.common.collect.Sets;
import com.jforex.programming.quote.QuoteException;
import com.jforex.programming.quote.TickQuote;
import com.jforex.programming.quote.TickQuoteRepository;
import com.jforex.programming.test.common.QuoteProviderForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

@RunWith(HierarchicalContextRunner.class)
public class TickQuoteRepositoryTest extends QuoteProviderForTest {

    private TickQuoteRepository tickQuoteRepository;

    private final Subject<TickQuote> quoteObservable = PublishSubject.create();
    private final Set<Instrument> subscribedInstruments = Sets.newHashSet(instrumentEURUSD,
                                                                          instrumentAUDUSD);

    @Before
    public void setUp() {
        setUpMocks();

        tickQuoteRepository = new TickQuoteRepository(quoteObservable,
                                                      historyUtilMock,
                                                      subscribedInstruments);
    }

    private void setUpMocks() {
        when(historyUtilMock.tickQuotesObservable(subscribedInstruments))
            .thenReturn(Observable.just(tickQuoteEURUSD, tickQuoteAUDUSD));
    }

    public class BeforeTicksReceived {

        @Test
        public void quotesForSubscribedInstrumentsComeFromHistory() {
            verify(historyUtilMock).tickQuotesObservable(subscribedInstruments);
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

            @Test
            public void getAllIsCorrect() {
                final Map<Instrument, TickQuote> tickMap = tickQuoteRepository.getAll();

                assertThat(tickMap.size(), equalTo(2));
                assertThat(tickMap.get(instrumentEURUSD),
                           equalTo(tickQuoteEURUSD));
                assertThat(tickMap.get(instrumentAUDUSD),
                           equalTo(tickQuoteAUDUSD));
            }
        }
    }

    public class ForInstrumentNotSubscribed {

        private final Instrument testInstrument = instrumentUSDJPY;

        public class WhenHistoryFails {

            @Before
            public void setUp() {
                when(historyUtilMock.tickQuoteObservable(testInstrument))
                    .thenThrow(new QuoteException(""));
            }

            @Test
            public void quoteIsSavedInCache() {
                assertNull(tickQuoteRepository.get(testInstrument));
            }
        }

        public class WhenHistoryHasQuote {

            @Before
            public void setUp() {
                when(historyUtilMock.tickQuoteObservable(testInstrument))
                    .thenReturn(Observable.just(tickQuoteUSDJPY));

                tickQuoteRepository.get(testInstrument);
            }

            @Test
            public void quoteIsSavedInCache() {
                tickQuoteRepository.get(testInstrument);

                verify(historyUtilMock).tickQuoteObservable(testInstrument);
            }
        }
    }
}
