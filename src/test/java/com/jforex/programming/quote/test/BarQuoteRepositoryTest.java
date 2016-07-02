package com.jforex.programming.quote.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.jforex.programming.quote.BarQuote;
import com.jforex.programming.quote.BarQuoteFilter;
import com.jforex.programming.quote.BarQuoteRepository;
import com.jforex.programming.test.common.QuoteProviderForTest;
import com.jforex.programming.test.fakes.IBarForTest;

import com.dukascopy.api.IBar;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;

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
        private final BarQuote askQuoteEURUSD = new BarQuote(instrumentEURUSD,
                                                             testPeriod,
                                                             OfferSide.ASK,
                                                             askBarEURUSD);
        private final BarQuote askQuoteAUDUSD = new BarQuote(instrumentAUDUSD,
                                                             testPeriod,
                                                             OfferSide.BID,
                                                             bidBarAUDUSD);
        private BarQuoteFilter quoteEURUSDFilter;
        private BarQuoteFilter quoteAUDUSDFilter;

        @Before
        public void setUp() {
            quoteEURUSDFilter = BarQuoteFilter
                    .forInstrument(instrumentEURUSD)
                    .period(testPeriod)
                    .offerSide(OfferSide.ASK);

            quoteAUDUSDFilter = BarQuoteFilter
                    .forInstrument(instrumentAUDUSD)
                    .period(testPeriod)
                    .offerSide(OfferSide.BID);
        }

        @Test
        public void askQuoteForEURUSDComesFromHistory() {
            when(historyUtilMock.latestBar(quoteEURUSDFilter))
                    .thenReturn(askBarEURUSD);

            final BarQuote receivedQuoteEURUSD = barQuoteRepository.get(quoteEURUSDFilter);

            assertQuote(receivedQuoteEURUSD, askQuoteEURUSD);
            verify(historyUtilMock).latestBar(quoteEURUSDFilter);
        }

        @Test
        public void bidQuoteForAUDUSDComesFromHistory() {
            when(historyUtilMock.latestBar(quoteAUDUSDFilter))
                    .thenReturn(bidBarAUDUSD);

            final BarQuote receivedQuoteAUDUSD = barQuoteRepository.get(quoteAUDUSDFilter);

            assertQuote(receivedQuoteAUDUSD, askQuoteAUDUSD);
            verify(historyUtilMock).latestBar(quoteAUDUSDFilter);
        }

        public class AfterReceivedBars {

            private final IBar newEURUSDBar = new IBarForTest();
            private final BarQuote newEURUSDQuote = new BarQuote(instrumentEURUSD,
                                                                 testPeriod,
                                                                 OfferSide.ASK,
                                                                 newEURUSDBar);

            private final IBar newAUDUSDBar = new IBarForTest();
            private final BarQuote newAUDUSDQuote = new BarQuote(instrumentAUDUSD,
                                                                 testPeriod,
                                                                 OfferSide.BID,
                                                                 newAUDUSDBar);

            @Before
            public void setUp() {
                quoteObservable.onNext(newEURUSDQuote);
                quoteObservable.onNext(newAUDUSDQuote);
            }

            @Test
            public void quoteForEURUSDComesFromObservable() {
                final BarQuote receivedQuoteEURUSD = barQuoteRepository.get(quoteEURUSDFilter);

                assertQuote(receivedQuoteEURUSD, newEURUSDQuote);
                verifyNoMoreInteractions(historyMock);
            }

            @Test
            public void quoteForAUDUSDComesFromObservable() {
                final BarQuote receivedQuoteAUDUSD = barQuoteRepository.get(quoteAUDUSDFilter);

                assertQuote(receivedQuoteAUDUSD, newAUDUSDQuote);
                verifyNoMoreInteractions(historyMock);
            }
        }
    }
}
