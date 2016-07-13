package com.jforex.programming.quote.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.dukascopy.api.IBar;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;
import com.jforex.programming.quote.BarQuote;
import com.jforex.programming.quote.BarQuoteParams;
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
        initCommonTestFramework();

        barQuoteRepository = new BarQuoteRepository(quoteObservable, historyUtilMock);
    }

    private void assertQuote(final BarQuote receivedQuote,
                             final BarQuote expectedQuote) {
        assertThat(receivedQuote.instrument(), equalTo(expectedQuote.instrument()));
        assertThat(receivedQuote.offerSide(), equalTo(expectedQuote.offerSide()));
        assertThat(receivedQuote.period(), equalTo(expectedQuote.period()));
        assertThat(receivedQuote.bar(), equalTo(expectedQuote.bar()));
    }

    public class BeforeBarsReceived {

        private final Period testPeriod = Period.ONE_MIN;
        private final BarQuoteParams quoteEURUSDParams = BarQuoteParams
                .forInstrument(instrumentEURUSD)
                .period(testPeriod)
                .offerSide(OfferSide.ASK);
        private final BarQuoteParams quoteAUDUSDParams = BarQuoteParams
                .forInstrument(instrumentAUDUSD)
                .period(testPeriod)
                .offerSide(OfferSide.BID);
        private final BarQuote askQuoteEURUSD = new BarQuote(quoteEURUSDParams,
                                                             askBarEURUSD);
        private final BarQuote askQuoteAUDUSD = new BarQuote(quoteAUDUSDParams,
                                                             bidBarAUDUSD);

        @Test
        public void askQuoteForEURUSDComesFromHistory() {
            when(historyUtilMock.latestBar(quoteEURUSDParams))
                    .thenReturn(askBarEURUSD);

            final BarQuote receivedQuoteEURUSD = barQuoteRepository.get(quoteEURUSDParams);

            assertQuote(receivedQuoteEURUSD, askQuoteEURUSD);
            verify(historyUtilMock).latestBar(quoteEURUSDParams);
        }

        @Test
        public void bidQuoteForAUDUSDComesFromHistory() {
            when(historyUtilMock.latestBar(quoteAUDUSDParams))
                    .thenReturn(bidBarAUDUSD);

            final BarQuote receivedQuoteAUDUSD = barQuoteRepository.get(quoteAUDUSDParams);

            assertQuote(receivedQuoteAUDUSD, askQuoteAUDUSD);
            verify(historyUtilMock).latestBar(quoteAUDUSDParams);
        }

        public class AfterReceivedBars {

            private final IBar newEURUSDBar = mock(IBar.class);
            private final BarQuote newEURUSDQuote = new BarQuote(quoteEURUSDParams,
                                                                 newEURUSDBar);

            private final IBar newAUDUSDBar = mock(IBar.class);
            private final BarQuote newAUDUSDQuote = new BarQuote(quoteAUDUSDParams,
                                                                 newAUDUSDBar);

            @Before
            public void setUp() {
                quoteObservable.onNext(newEURUSDQuote);
                quoteObservable.onNext(newAUDUSDQuote);
            }

            @Test
            public void quoteForEURUSDComesFromObservable() {
                final BarQuote receivedQuoteEURUSD = barQuoteRepository.get(quoteEURUSDParams);

                assertQuote(receivedQuoteEURUSD, newEURUSDQuote);
                verifyNoMoreInteractions(historyMock);
            }

            @Test
            public void quoteForAUDUSDComesFromObservable() {
                final BarQuote receivedQuoteAUDUSD = barQuoteRepository.get(quoteAUDUSDParams);

                assertQuote(receivedQuoteAUDUSD, newAUDUSDQuote);
                verifyNoMoreInteractions(historyMock);
            }
        }
    }
}
