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
import com.jforex.programming.quote.BarQuoteRepository;
import com.jforex.programming.test.common.QuoteProviderForTest;
import com.jforex.programming.test.fakes.IBarForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

@RunWith(HierarchicalContextRunner.class)
public class BarQuoteRepositoryTest extends QuoteProviderForTest {

    private BarQuoteRepository barQuoteRepository;

    private final Subject<BarQuote, BarQuote> quoteObservable = PublishSubject.create();
    private final Period testPeriod = Period.ONE_MIN;

    @Before
    public void setUp() {
        initCommonTestFramework();

        barQuoteRepository = new BarQuoteRepository(quoteObservable, historyUtilMock);
    }

    public class BeforeBarsReceived {

        @Before
        public void setUp() {
            when(historyUtilMock.latestBar(instrumentEURUSD, testPeriod, OfferSide.ASK))
                    .thenReturn(askBarEURUSD);
            when(historyUtilMock.latestBar(instrumentAUDUSD, testPeriod, OfferSide.BID))
                    .thenReturn(bidBarAUDUSD);
        }

        @Test
        public void askBarForEURUSDComesFromHistory() {
            assertThat(barQuoteRepository.get(instrumentEURUSD, testPeriod, OfferSide.ASK),
                       equalTo(askBarEURUSD));

            verify(historyUtilMock).latestBar(instrumentEURUSD, testPeriod, OfferSide.ASK);
        }

        @Test
        public void bidBarForAUDUSDComesFromHistory() {
            assertThat(barQuoteRepository.get(instrumentAUDUSD, testPeriod, OfferSide.BID),
                       equalTo(bidBarAUDUSD));

            verify(historyUtilMock).latestBar(instrumentAUDUSD, testPeriod, OfferSide.BID);
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
            public void barForEURUSDComesFromObservable() {
                verifyNoMoreInteractions(historyMock);

                assertThat(barQuoteRepository.get(instrumentEURUSD, testPeriod, OfferSide.ASK),
                           equalTo(newEURUSDBar));
            }

            @Test
            public void barForAUDUSDComesFromObservable() {
                verifyNoMoreInteractions(historyMock);

                assertThat(barQuoteRepository.get(instrumentAUDUSD, testPeriod, OfferSide.BID),
                           equalTo(newAUDUSDBar));
            }
        }
    }
}
