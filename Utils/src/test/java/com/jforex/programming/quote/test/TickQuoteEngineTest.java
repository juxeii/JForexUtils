package com.jforex.programming.quote.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.jforex.programming.quote.QuoteProviderException;
import com.jforex.programming.quote.TickQuote;
import com.jforex.programming.quote.TickQuoteConsumer;
import com.jforex.programming.quote.TickQuoteProvider;
import com.jforex.programming.test.common.CurrencyUtilForTest;
import com.jforex.programming.test.fakes.ITickForTest;

import com.dukascopy.api.ITick;
import com.dukascopy.api.JFException;
import com.dukascopy.api.OfferSide;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

@RunWith(HierarchicalContextRunner.class)
public class TickQuoteEngineTest extends CurrencyUtilForTest {

    private TickQuoteProvider tickQuoteEngine;

    @Mock private TickQuoteConsumer tickQuoteConsumerEURUSDMock;
    @Mock private TickQuoteConsumer tickQuoteConsumerAUDUSDMock;
    private Subject<TickQuote, TickQuote> tickObservable;
    private final ITick tickEURUSDOfHistory = new ITickForTest(1.23413, 1.23488);
    private final ITick firstTickEURUSD = new ITickForTest(1.23456, 1.23451);
    private final ITick secondTickEURUSD = new ITickForTest(1.32123, 1.32143);
    private final TickQuote firstEURUSDTickQuote = new TickQuote(instrumentEURUSD, firstTickEURUSD);
    private final TickQuote secondEURUSDTickQuote = new TickQuote(instrumentEURUSD, secondTickEURUSD);

    private final ITick firstTickAUDUSD = new ITickForTest(1.10345, 1.10348);
    private final TickQuote firstAUDUSDTickQuote = new TickQuote(instrumentAUDUSD, firstTickAUDUSD);

    @Before
    public void setUp() {
        initCommonTestFramework();
        tickObservable = PublishSubject.create();

        tickQuoteEngine = new TickQuoteProvider(tickObservable, historyMock);
        tickQuoteEngine.subscribe(instrumentEURUSD, tickQuoteConsumerEURUSDMock::onTickQuote);
    }

    private void verifyTickValues(final ITick tick) {
        assertThat(tickQuoteEngine.tick(instrumentEURUSD),
                   equalTo(tick));

        assertThat(tickQuoteEngine.ask(instrumentEURUSD),
                   equalTo(tick.getAsk()));

        assertThat(tickQuoteEngine.bid(instrumentEURUSD),
                   equalTo(tick.getBid()));

        assertThat(tickQuoteEngine.forOfferSide(instrumentEURUSD, OfferSide.ASK),
                   equalTo(tick.getAsk()));

        assertThat(tickQuoteEngine.forOfferSide(instrumentEURUSD, OfferSide.BID),
                   equalTo(tick.getBid()));
    }

    @Test
    public void testObservableIsCorrectReturned() {
        assertThat(tickQuoteEngine.observable(), equalTo(tickObservable));
    }

    public class HistoryThrowsBeforeFirstTickQuoteIsReceived {

        @Before
        public void setUp() throws JFException {
            when(historyMock.getLastTick(instrumentEURUSD)).thenThrow(jfException);
        }

        @Test(expected = QuoteProviderException.class)
        public void testTickThrows() {
            tickQuoteEngine.tick(instrumentEURUSD);
        }

        @Test(expected = QuoteProviderException.class)
        public void testAskThrows() {
            tickQuoteEngine.ask(instrumentEURUSD);
        }

        @Test(expected = QuoteProviderException.class)
        public void testBidThrows() {
            tickQuoteEngine.bid(instrumentEURUSD);
        }

        @Test(expected = QuoteProviderException.class)
        public void testForOfferSideAskThrows() {
            tickQuoteEngine.forOfferSide(instrumentEURUSD, OfferSide.ASK);
        }

        @Test(expected = QuoteProviderException.class)
        public void testForOfferSideBidThrows() {
            tickQuoteEngine.forOfferSide(instrumentEURUSD, OfferSide.BID);
        }
    }

    public class HistoryReturnsTickBeforeFirstTickQuoteIsReceived {

        @Before
        public void setUp() throws JFException {
            when(historyMock.getLastTick(instrumentEURUSD)).thenReturn(tickEURUSDOfHistory);
        }

        @Test
        public void testTickReturnsHistoryTick() {
            verifyTickValues(tickEURUSDOfHistory);
        }
    }

    public class AfterFirstTickQuoteIsConsumed {

        @Before
        public void setUp() {
            tickObservable.onNext(firstEURUSDTickQuote);
        }

        @Test
        public void testNoInteractionsWithHistory() {
            verifyZeroInteractions(historyMock);
        }

        @Test
        public void testTickReturnsFirstTick() {
            verifyTickValues(firstTickEURUSD);
        }

        @Test
        public void testTickQuoteConsumerReceivesFirstTickQuote() {
            verify(tickQuoteConsumerEURUSDMock).onTickQuote(firstEURUSDTickQuote);

        }

        @Test
        public void testNoInteractionWithAUDUSDConsumer() {
            verifyZeroInteractions(tickQuoteConsumerAUDUSDMock);
        }

        public class AfterSecondTickQuoteIsConsumed {

            @Before
            public void setUp() {
                tickObservable.onNext(secondEURUSDTickQuote);
            }

            @Test
            public void testNoInteractionsWithHistory() {
                verifyZeroInteractions(historyMock);
            }

            @Test
            public void testTickReturnsSecondTick() {
                verifyTickValues(secondTickEURUSD);
            }

            @Test
            public void testTickQuoteConsumerReceivesSecondTickQuote() {
                verify(tickQuoteConsumerEURUSDMock).onTickQuote(secondEURUSDTickQuote);
            }

            @Test
            public void testTickQuoteConsumerForAUDUSDReceivesTickQuote() {
                tickQuoteEngine.subscribe(instrumentAUDUSD, tickQuoteConsumerAUDUSDMock::onTickQuote);

                tickObservable.onNext(firstAUDUSDTickQuote);

                verify(tickQuoteConsumerAUDUSDMock).onTickQuote(firstAUDUSDTickQuote);
            }
        }
    }
}
