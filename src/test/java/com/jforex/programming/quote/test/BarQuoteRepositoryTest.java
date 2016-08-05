package com.jforex.programming.quote.test;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.jforex.programming.quote.BarQuote;
import com.jforex.programming.quote.BarQuoteRepository;
import com.jforex.programming.test.common.QuoteProviderForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

@RunWith(HierarchicalContextRunner.class)
public class BarQuoteRepositoryTest extends QuoteProviderForTest {

    private BarQuoteRepository barQuoteRepository;

    private final Subject<BarQuote, BarQuote> quoteObservable = PublishSubject.create();

    @Before
    public void setUp() {
        barQuoteRepository = new BarQuoteRepository(quoteObservable, historyUtilMock);
    }

    public class BeforeBarsReceived {

        @Test
        public void askQuoteForEURUSDComesFromHistory() {
            when(historyUtilMock.barQuote(askBarEURUSDParams))
                    .thenReturn(askBarEURUSD);

            final BarQuote receivedQuoteEURUSD = barQuoteRepository.get(askBarEURUSDParams);

            assertEqualBarQuotes(receivedQuoteEURUSD, askBarQuoteEURUSD);
            verify(historyUtilMock).barQuote(askBarEURUSDParams);
        }

        @Test
        public void bidQuoteForAUDUSDComesFromHistory() {
            when(historyUtilMock.barQuote(bidBarAUDUSDParams))
                    .thenReturn(bidBarAUDUSD);

            final BarQuote receivedQuoteAUDUSD = barQuoteRepository.get(bidBarAUDUSDParams);

            assertEqualBarQuotes(receivedQuoteAUDUSD, bidBarQuoteAUDUSD);
            verify(historyUtilMock).barQuote(bidBarAUDUSDParams);
        }

        public class AfterReceivedBars {

            @Before
            public void setUp() {
                quoteObservable.onNext(askBarQuoteEURUSD);
                quoteObservable.onNext(askBarQuoteAUDUSD);
            }

            @Test
            public void quoteForEURUSDComesFromObservable() {
                final BarQuote receivedQuoteEURUSD = barQuoteRepository.get(askBarEURUSDParams);

                assertEqualBarQuotes(receivedQuoteEURUSD, askBarQuoteEURUSD);
                verifyNoMoreInteractions(historyMock);
            }

            @Test
            public void quoteForAUDUSDComesFromObservable() {
                final BarQuote receivedQuoteAUDUSD = barQuoteRepository.get(askBarAUDUSDParams);

                assertEqualBarQuotes(receivedQuoteAUDUSD, askBarQuoteAUDUSD);
                verifyNoMoreInteractions(historyMock);
            }
        }
    }
}
