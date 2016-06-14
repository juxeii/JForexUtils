package com.jforex.programming.quote.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.IBar;
import com.dukascopy.api.JFException;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;
import com.google.common.collect.Sets;
import com.jforex.programming.quote.BarQuote;
import com.jforex.programming.quote.BarQuoteProvider;
import com.jforex.programming.quote.QuoteProviderException;
import com.jforex.programming.test.common.CurrencyUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

@RunWith(HierarchicalContextRunner.class)
public class BarQuoteProviderTest extends CurrencyUtilForTest {

    private BarQuoteProvider barQuoteProvider;

    @Mock
    private IBar askBarEURUSDOfHistory;
    @Mock
    private IBar bidBarEURUSDOfHistory;
    @Mock
    private IBar firstAskBarEURUSD;
    @Mock
    private IBar firstBidBarEURUSD;
    @Mock
    private IBar secondAskBarEURUSD;
    @Mock
    private IBar secondBidBarEURUSD;
    @Mock
    private IBar firstAskBarAUDUSD;
    @Mock
    private IBar firstBidBarAUDUSD;
    private final Period testPeriod = Period.ONE_MIN;
    private Subject<BarQuote, BarQuote> barObservable;
    private BarQuote firstEURUSDBarQuote;
    private BarQuote secondEURUSDBarQuote;
    private BarQuote firstAUDUSDBarQuote;

    @Before
    public void setUp() {
        initCommonTestFramework();
        barObservable = PublishSubject.create();

        barQuoteProvider = new BarQuoteProvider(barObservable, historyMock);
        barQuoteProvider.subscribe(Sets.newHashSet(instrumentEURUSD),
                                   testPeriod,
                                   OfferSide.ASK)
                .subscribe();
    }

    private void verifyBarValues(final IBar askBar,
                                 final IBar bidBar) {
        assertThat(barQuoteProvider.askBar(instrumentEURUSD, testPeriod),
                   equalTo(askBar));

        assertThat(barQuoteProvider.bidBar(instrumentEURUSD, testPeriod),
                   equalTo(bidBar));

        assertThat(barQuoteProvider.forOfferSide(instrumentEURUSD, testPeriod, OfferSide.ASK),
                   equalTo(askBar));

        assertThat(barQuoteProvider.forOfferSide(instrumentEURUSD, testPeriod, OfferSide.BID),
                   equalTo(bidBar));
    }

    @Test
    public void testObservableIsCorrectReturned() {
        assertThat(barQuoteProvider.observable(), equalTo(barObservable));
    }

    public class HistoryThrowsBeforeFirstBarQuoteIsReceived {

        @Before
        public void setUp() throws JFException {
            when(historyMock.getBar(eq(instrumentEURUSD), eq(testPeriod), any(), eq(1)))
                    .thenThrow(jfException);
        }

        @Test(expected = QuoteProviderException.class)
        public void testAskBarThrows() {
            barQuoteProvider.askBar(instrumentEURUSD, testPeriod);
        }

        @Test(expected = QuoteProviderException.class)
        public void testBidBarThrows() {
            barQuoteProvider.bidBar(instrumentEURUSD, testPeriod);
        }

        @Test(expected = QuoteProviderException.class)
        public void testForOfferSideAskThrows() {
            barQuoteProvider.forOfferSide(instrumentEURUSD, testPeriod, OfferSide.ASK);
        }

        @Test(expected = QuoteProviderException.class)
        public void testForOfferSideBidThrows() {
            barQuoteProvider.forOfferSide(instrumentEURUSD, testPeriod, OfferSide.BID);
        }
    }

    public class HistoryReturnsNullBeforeFirstBarQuoteIsReceived {

        private void verifyHistoryRetry() throws JFException {
            verify(historyMock, times(2))
                    .getBar(eq(instrumentEURUSD), eq(testPeriod), any(), eq(1));
        }

        @Before
        public void setUp() throws JFException {
            when(historyMock.getBar(eq(instrumentEURUSD), eq(testPeriod), any(), eq(1)))
                    .thenReturn(null)
                    .thenReturn(askBarEURUSDOfHistory);
        }

        @Test
        public void testAskBarRetries() throws JFException {
            barQuoteProvider.askBar(instrumentEURUSD, testPeriod);

            verifyHistoryRetry();
        }

        @Test
        public void testBidBarRetries() throws JFException {
            barQuoteProvider.bidBar(instrumentEURUSD, testPeriod);

            verifyHistoryRetry();
        }

        @Test
        public void testForOfferSideAskRetries() throws JFException {
            barQuoteProvider.forOfferSide(instrumentEURUSD, testPeriod, OfferSide.ASK);

            verifyHistoryRetry();
        }

        @Test
        public void testForOfferSideBidRetries() throws JFException {
            barQuoteProvider.forOfferSide(instrumentEURUSD, testPeriod, OfferSide.BID);

            verifyHistoryRetry();
        }

        @Test(expected = QuoteProviderException.class)
        public void testAfterAllTriesExceptionIsThrown() throws JFException {
            when(historyMock.getBar(eq(instrumentEURUSD), eq(testPeriod), any(), eq(1)))
                    .thenReturn(null);

            barQuoteProvider.forOfferSide(instrumentEURUSD, testPeriod, OfferSide.BID);
        }
    }

    public class HistoryReturnsBarBeforeFirstBarQuoteIsReceived {

        @Before
        public void setUp() throws JFException {
            when(historyMock.getBar(eq(instrumentEURUSD), eq(testPeriod), eq(OfferSide.ASK),
                                    eq(1))).thenReturn(askBarEURUSDOfHistory);
            when(historyMock.getBar(eq(instrumentEURUSD), eq(testPeriod), eq(OfferSide.BID),
                                    eq(1))).thenReturn(bidBarEURUSDOfHistory);
        }

        @Test
        public void testBarReturnsHistoryBar() {
            verifyBarValues(askBarEURUSDOfHistory, bidBarEURUSDOfHistory);
        }
    }

    public class AfterFirstBarQuoteIsConsumed {

        @Before
        public void setUp() {
            firstEURUSDBarQuote = new BarQuote(firstAskBarEURUSD, instrumentEURUSD, testPeriod, OfferSide.ASK);

            barObservable.onNext(firstEURUSDBarQuote);
        }

        @Test
        public void testNoInteractionsWithHistory() {
            verifyZeroInteractions(historyMock);
        }

//        @Test
//        public void testBarReturnsFirstBar() {
//            verifyBarValues(firstAskBarEURUSD, firstBidBarEURUSD);
//        }

        public class AfterRegisterDifferentPeriod {

            private final Period otherPeriod = Period.FIFTEEN_MINS;

            @Before
            public void setUp() {
                barQuoteProvider.subscribe(Sets.newHashSet(instrumentEURUSD),
                                           otherPeriod,
                                           OfferSide.ASK)
                        .subscribe();
                firstEURUSDBarQuote = new BarQuote(firstAskBarEURUSD, instrumentEURUSD, otherPeriod, OfferSide.ASK);

                barObservable.onNext(firstEURUSDBarQuote);
            }

            @Test
            public void testNoInteractionsWithHistory() {
                verifyZeroInteractions(historyMock);
            }

//            @Test
//            public void testBarReturnsFirstBarForOtherPeriod() {
//                verifyBarValues(firstAskBarEURUSD, firstBidBarEURUSD);
//            }
        }

        public class AfterSecondBarQuoteIsConsumed {

            @Before
            public void setUp() {
                secondEURUSDBarQuote =
                        new BarQuote(secondAskBarEURUSD,
                                     instrumentEURUSD,
                                     testPeriod,
                                     OfferSide.ASK);

                barObservable.onNext(secondEURUSDBarQuote);
            }

            @Test
            public void testNoInteractionsWithHistory() {
                verifyZeroInteractions(historyMock);
            }

//            @Test
//            public void testBarReturnsSecondBar() {
//                verifyBarValues(secondAskBarEURUSD, secondBidBarEURUSD);
//            }
        }
    }
}
