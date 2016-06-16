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
import com.dukascopy.api.Unit;
import com.google.common.collect.Sets;
import com.jforex.programming.builder.BarQuoteParams;
import com.jforex.programming.quote.BarQuote;
import com.jforex.programming.quote.BarQuoteHandler;
import com.jforex.programming.quote.BarQuoteRepository;
import com.jforex.programming.test.common.CurrencyUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import rx.observers.TestSubscriber;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

@RunWith(HierarchicalContextRunner.class)
public class BarQuoteHandlerTest extends CurrencyUtilForTest {

    private BarQuoteHandler barQuoteHandler;

    @Mock
    private BarQuoteRepository barQuoteRepositoryMock;
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
    private final Subject<BarQuote, BarQuote> barQuoteSubject = PublishSubject.create();
    private final Period testPeriod = Period.ONE_MIN;

    @Before
    public void setUp() {
        initCommonTestFramework();

        barQuoteHandler = new BarQuoteHandler(jforexUtilMock,
                                              barQuoteSubject,
                                              barQuoteRepositoryMock);
    }

    private void setUpQuoteRepository() throws JFException {
        when(barQuoteRepositoryMock.get(eq(instrumentEURUSD), eq(testPeriod), eq(OfferSide.ASK)))
                .thenReturn(askBarEURUSDOfHistory);
        when(barQuoteRepositoryMock.get(eq(instrumentEURUSD), eq(testPeriod), eq(OfferSide.BID)))
                .thenReturn(bidBarEURUSDOfHistory);
    }

    public class AskBarEURUSDIsConsumed {

        @Before
        public void setUp() throws JFException {
            setUpQuoteRepository();

            when(barQuoteRepositoryMock.get(eq(instrumentEURUSD), eq(testPeriod), eq(OfferSide.ASK)))
                    .thenReturn(firstAskBarEURUSD);
            when(barQuoteRepositoryMock.get(eq(instrumentEURUSD), eq(testPeriod), eq(OfferSide.BID)))
                    .thenReturn(bidBarEURUSDOfHistory);
        }

        @Test
        public void askBarReturnsReceivedAskBar() {
            assertThat(barQuoteHandler.askBar(instrumentEURUSD, testPeriod),
                       equalTo(firstAskBarEURUSD));
        }

        @Test
        public void bidBarReturnsHistoryBidBar() {
            assertThat(barQuoteHandler.bidBar(instrumentEURUSD, testPeriod),
                       equalTo(bidBarEURUSDOfHistory));
        }

        @Test
        public void forAskOfferSideReturnsReceivedAskBar() {
            assertThat(barQuoteHandler.forOfferSide(instrumentEURUSD, testPeriod, OfferSide.ASK),
                       equalTo(firstAskBarEURUSD));
        }

        @Test
        public void forBidOfferSideReturnsReturnsHistoryBidBar() {
            verifyZeroInteractions(historyUtilMock);

            assertThat(barQuoteHandler.forOfferSide(instrumentEURUSD, testPeriod, OfferSide.BID),
                       equalTo(bidBarEURUSDOfHistory));
        }

        public class SubscriptionToAskBar {

            private final TestSubscriber<BarQuote> quoteEURUSDSubscriber = new TestSubscriber<>();
            private final TestSubscriber<BarQuote> customPeriodEURUSDSubscriber = new TestSubscriber<>();
            private final TestSubscriber<BarQuote> quoteAUDUSDSubscriber = new TestSubscriber<>();

            private final Period custom3MinutePeriod = Period.createCustomPeriod(Unit.Minute, 3);

            @Before
            public void setUp() {
                barQuoteHandler.observableForSubscription(BarQuoteParams
                        .forInstruments(Sets.newHashSet(instrumentEURUSD))
                        .period(testPeriod)
                        .offerSide(OfferSide.ASK))
                        .subscribe(quoteEURUSDSubscriber);

                barQuoteHandler.observableForSubscription(BarQuoteParams
                        .forInstruments(Sets.newHashSet(instrumentEURUSD))
                        .period(custom3MinutePeriod)
                        .offerSide(OfferSide.ASK))
                        .subscribe(customPeriodEURUSDSubscriber);

                barQuoteHandler.observableForSubscription(BarQuoteParams
                        .forInstruments(Sets.newHashSet(instrumentAUDUSD))
                        .period(testPeriod)
                        .offerSide(OfferSide.ASK))
                        .subscribe(quoteAUDUSDSubscriber);

                barQuoteSubject.onNext(new BarQuote(instrumentEURUSD,
                                                    testPeriod,
                                                    OfferSide.ASK,
                                                    firstAskBarEURUSD));
                barQuoteSubject.onNext(new BarQuote(instrumentEURUSD,
                                                    custom3MinutePeriod,
                                                    OfferSide.ASK,
                                                    firstAskBarEURUSD));
            }

            @Test
            public void newBarIsEmitted() {
                quoteEURUSDSubscriber.assertNoErrors();
                quoteEURUSDSubscriber.assertValueCount(1);

                final BarQuote barQuote = quoteEURUSDSubscriber.getOnNextEvents().get(0);
                assertThat(barQuote.bar(), equalTo(firstAskBarEURUSD));
                assertThat(barQuote.instrument(), equalTo(instrumentEURUSD));
                assertThat(barQuote.period(), equalTo(testPeriod));
                assertThat(barQuote.offerSide(), equalTo(OfferSide.ASK));
            }

            @Test
            public void newCustomBarIsEmitted() {
                customPeriodEURUSDSubscriber.assertNoErrors();
                customPeriodEURUSDSubscriber.assertValueCount(1);

                final BarQuote barQuote = customPeriodEURUSDSubscriber.getOnNextEvents().get(0);
                assertThat(barQuote.bar(), equalTo(firstAskBarEURUSD));
                assertThat(barQuote.instrument(), equalTo(instrumentEURUSD));
                assertThat(barQuote.period(), equalTo(custom3MinutePeriod));
                assertThat(barQuote.offerSide(), equalTo(OfferSide.ASK));
            }

            @Test
            public void contextSubscribesToCustomBarFeed() {
                verify(jforexUtilMock).subscribeToBarsFeed(eq(instrumentEURUSD),
                                                           eq(custom3MinutePeriod),
                                                           eq(OfferSide.ASK));
            }

            @Test
            public void newBarIsNotEmittedForAUDUSDSubscriber() {
                quoteAUDUSDSubscriber.assertNoErrors();
                quoteAUDUSDSubscriber.assertValueCount(0);
            }
        }

        public class BidkBarEURUSDIsConsumed {

            @Before
            public void setUp() {
                when(barQuoteRepositoryMock.get(eq(instrumentEURUSD), eq(testPeriod), eq(OfferSide.BID)))
                        .thenReturn(firstBidBarEURUSD);
            }

            @Test
            public void askBarReturnsReceivedAskBar() {
                assertThat(barQuoteHandler.askBar(instrumentEURUSD, testPeriod),
                           equalTo(firstAskBarEURUSD));
            }

            @Test
            public void bidBarReturnsReceivedBidBar() {
                assertThat(barQuoteHandler.bidBar(instrumentEURUSD, testPeriod),
                           equalTo(firstBidBarEURUSD));
            }
        }
    }
}
