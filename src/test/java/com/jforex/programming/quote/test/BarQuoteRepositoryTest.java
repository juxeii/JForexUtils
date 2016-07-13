package com.jforex.programming.quote.test;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.jforex.programming.quote.BarQuote;
import com.jforex.programming.quote.BarQuoteRepository;
import com.jforex.programming.test.common.QuoteProviderForTest;

import com.dukascopy.api.IBar;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

@RunWith(HierarchicalContextRunner.class)
public class BarQuoteRepositoryTest extends QuoteProviderForTest {

    private BarQuoteRepository barQuoteRepository;

    private final Subject<BarQuote, BarQuote> quoteObservable = PublishSubject.create();

    @Before
    public void setUp() {
        initCommonTestFramework();

        barQuoteRepository = new BarQuoteRepository(quoteObservable, historyUtilMock);
    }

    public class BeforeBarsReceived {

        @Test
        public void askQuoteForEURUSDComesFromHistory() {
            when(historyUtilMock.latestBar(askBarEURUSDParams))
                    .thenReturn(askBarEURUSD);

            final BarQuote receivedQuoteEURUSD = barQuoteRepository.get(askBarEURUSDParams);

            assertEqualBarQuotes(receivedQuoteEURUSD, askBarQuoteEURUSD);
            verify(historyUtilMock).latestBar(askBarEURUSDParams);
        }

        @Test
        public void bidQuoteForAUDUSDComesFromHistory() {
            when(historyUtilMock.latestBar(bidBarAUDUSDParams))
                    .thenReturn(bidBarAUDUSD);

            final BarQuote receivedQuoteAUDUSD = barQuoteRepository.get(bidBarAUDUSDParams);

            assertEqualBarQuotes(receivedQuoteAUDUSD, bidBarQuoteAUDUSD);
            verify(historyUtilMock).latestBar(bidBarAUDUSDParams);
        }

        public class AfterReceivedBars {

            private final IBar newEURUSDBar = mock(IBar.class);
            private final BarQuote newEURUSDQuote = new BarQuote(askBarEURUSDParams,
                                                                 newEURUSDBar);

            private final IBar newAUDUSDBar = mock(IBar.class);
            private final BarQuote newAUDUSDQuote = new BarQuote(askBarAUDUSDParams,
                                                                 newAUDUSDBar);

            @Before
            public void setUp() {
                quoteObservable.onNext(newEURUSDQuote);
                quoteObservable.onNext(newAUDUSDQuote);
            }

            @Test
            public void quoteForEURUSDComesFromObservable() {
                final BarQuote receivedQuoteEURUSD = barQuoteRepository.get(askBarEURUSDParams);

                assertEqualBarQuotes(receivedQuoteEURUSD, newEURUSDQuote);
                verifyNoMoreInteractions(historyMock);
            }

            @Test
            public void quoteForAUDUSDComesFromObservable() {
                final BarQuote receivedQuoteAUDUSD = barQuoteRepository.get(askBarAUDUSDParams);

                assertEqualBarQuotes(receivedQuoteAUDUSD, newAUDUSDQuote);
                verifyNoMoreInteractions(historyMock);
            }
        }
    }
}
