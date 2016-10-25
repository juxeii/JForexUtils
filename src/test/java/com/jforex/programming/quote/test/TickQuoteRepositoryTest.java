package com.jforex.programming.quote.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.jforex.programming.quote.TickQuote;
import com.jforex.programming.quote.TickQuoteRepository;
import com.jforex.programming.test.common.QuoteProviderForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

@RunWith(HierarchicalContextRunner.class)
public class TickQuoteRepositoryTest extends QuoteProviderForTest {

    private TickQuoteRepository tickQuoteRepository;

    private final Subject<TickQuote> quoteObservable = PublishSubject.create();

    @Before
    public void setUp() {
        tickQuoteRepository = new TickQuoteRepository(quoteObservable);
    }

    public class ReceivedQuotes {

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
    }
}
