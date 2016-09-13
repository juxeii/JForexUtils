package com.jforex.programming.quote.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.jforex.programming.quote.BarQuote;
import com.jforex.programming.quote.BarQuoteRepository;
import com.jforex.programming.test.common.QuoteProviderForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

@RunWith(HierarchicalContextRunner.class)
public class BarQuoteRepositoryTest extends QuoteProviderForTest {

    private BarQuoteRepository barQuoteRepository;

    private final Subject<BarQuote> quoteObservable = PublishSubject.create();

    @Before
    public void setUp() {
        barQuoteRepository = new BarQuoteRepository(quoteObservable, historyUtilMock);
    }

    public class BeforeBarsReceived {

        @Test
        public void askQuoteForEURUSDComesFromHistory() {
            when(historyUtilMock.latestBarObservable(askBarEURUSDParams))
                .thenReturn(Observable.just(askBarEURUSD));

            final BarQuote receivedQuoteEURUSD = barQuoteRepository.get(askBarEURUSDParams);

            assertThat(receivedQuoteEURUSD, equalTo(askBarQuoteEURUSD));
            verify(historyUtilMock).latestBarObservable(askBarEURUSDParams);
        }

        @Test
        public void bidQuoteForAUDUSDComesFromHistory() {
            when(historyUtilMock.latestBarObservable(bidBarAUDUSDParams))
                .thenReturn(Observable.just(bidBarAUDUSD));

            final BarQuote receivedQuoteAUDUSD = barQuoteRepository.get(bidBarAUDUSDParams);

            assertThat(receivedQuoteAUDUSD, equalTo(bidBarQuoteAUDUSD));
            verify(historyUtilMock).latestBarObservable(bidBarAUDUSDParams);
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

                assertThat(receivedQuoteEURUSD, equalTo(askBarQuoteEURUSD));
                verifyNoMoreInteractions(historyMock);
            }

            @Test
            public void quoteForAUDUSDComesFromObservable() {
                final BarQuote receivedQuoteAUDUSD = barQuoteRepository.get(askBarAUDUSDParams);

                assertThat(receivedQuoteAUDUSD, equalTo(askBarQuoteAUDUSD));
                verifyNoMoreInteractions(historyMock);
            }
        }
    }
}
