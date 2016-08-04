package com.jforex.programming.misc.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aeonbits.owner.ConfigFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.JFException;
import com.jforex.programming.misc.JFRunnable;
import com.jforex.programming.misc.StreamUtil;
import com.jforex.programming.order.call.OrderCallRejectException;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.settings.PlatformSettings;
import com.jforex.programming.test.common.CommonUtilForTest;
import com.jforex.programming.test.common.OrderUtilForTest;
import com.jforex.programming.test.common.RxTestUtil;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import rx.Observable;
import rx.observers.TestSubscriber;

@RunWith(HierarchicalContextRunner.class)
public class StreamUtilTest extends CommonUtilForTest {

    @Mock
    private Callable<IOrder> callableMock;

    @Test
    public void testConstructorIsPrivate() throws Exception {
        assertPrivateConstructor(StreamUtil.class);
    }

    public class RetryObservableSetup {

        private final OrderUtilForTest orderForTest = OrderUtilForTest.buyOrderEURUSD();
        private final OrderEvent rejectEvent = new OrderEvent(orderForTest, OrderEventType.CLOSE_REJECTED);
        private final TestSubscriber<IOrder> orderSubscriber = new TestSubscriber<>();
        private final OrderCallRejectException rejectException =
                new OrderCallRejectException("Reject exception for test", rejectEvent);
        private Runnable retryCall;

        private final PlatformSettings platformSettings = ConfigFactory.create(PlatformSettings.class);
        private final long delayOnOrderFailRetry = platformSettings.delayOnOrderFailRetry();
        private final int maxRetriesOnOrderFail = platformSettings.maxRetriesOnOrderFail();

        @Before
        public void setUp() {
            retryCall = () -> Observable
                    .fromCallable(callableMock)
                    .retryWhen(StreamUtil::retryObservable)
                    .subscribe(orderSubscriber);
        }

        @Test
        public void retryObservableErrorsForNonRejectException() throws Exception {
            when(callableMock.call()).thenThrow(jfException);

            retryCall.run();

            orderSubscriber.assertError(JFException.class);
        }

        public class SequenceSetup {

            private void expectAllRetriesThenSuccess() throws Exception {
                final Throwable throwables[] = new Throwable[maxRetriesOnOrderFail];
                for (int i = 0; i < maxRetriesOnOrderFail; ++i) {
                    throwables[i] = rejectException;
                }

                when(callableMock.call())
                        .thenThrow(throwables)
                        .thenReturn(orderForTest);
            }

            private void exceeAllRetries() throws Exception {
                final Throwable throwables[] = new Throwable[maxRetriesOnOrderFail + 1];
                for (int i = 0; i <= maxRetriesOnOrderFail; ++i) {
                    throwables[i] = rejectException;
                }

                when(callableMock.call())
                        .thenThrow(throwables);
            }

            @Test
            public void testThatAllRetriesAreDone() throws Exception {
                expectAllRetriesThenSuccess();

                retryCall.run();

                RxTestUtil.advanceTimeBy(maxRetriesOnOrderFail * delayOnOrderFailRetry,
                                         TimeUnit.MILLISECONDS);

                verify(callableMock, times(maxRetriesOnOrderFail + 1)).call();

                orderSubscriber.assertNoErrors();
                orderSubscriber.assertValueCount(1);
                orderSubscriber.assertCompleted();
            }

            @Test
            public void testThatAfterAllRetriesTheSubscriberErrors() throws Exception {
                exceeAllRetries();

                retryCall.run();

                RxTestUtil.advanceTimeBy(maxRetriesOnOrderFail * delayOnOrderFailRetry,
                                         TimeUnit.MILLISECONDS);

                verify(callableMock, times(maxRetriesOnOrderFail + 1)).call();

                orderSubscriber.assertError(OrderCallRejectException.class);
            }
        }
    }

    @Test
    public void retryCounterObservableCountsCorrect() {
        final int maxRetries = 3;
        final TestSubscriber<Integer> subscriber = new TestSubscriber<Integer>();

        StreamUtil.retryCounterObservable(maxRetries).subscribe(subscriber);

        subscriber.assertCompleted();
        subscriber.assertNoErrors();
        subscriber.assertValueCount(maxRetries + 1);

        assertThat(subscriber.getOnNextEvents().get(0), equalTo(1));
        assertThat(subscriber.getOnNextEvents().get(1), equalTo(2));
        assertThat(subscriber.getOnNextEvents().get(2), equalTo(3));
        assertThat(subscriber.getOnNextEvents().get(3), equalTo(4));
    }

    @Test
    public void waitObservableIsCorrect() {
        final TestSubscriber<Long> subscriber = new TestSubscriber<Long>();

        StreamUtil.waitObservable(1000L, TimeUnit.MILLISECONDS).subscribe(subscriber);

        RxTestUtil.advanceTimeBy(900L, TimeUnit.MILLISECONDS);
        subscriber.assertNotCompleted();
        RxTestUtil.advanceTimeBy(100L, TimeUnit.MILLISECONDS);
        subscriber.assertCompleted();
    }

    @Test
    public void completableFromJFRunnableIsCorrect() throws Exception {
        final JFRunnable jfRunnableMock = mock(JFRunnable.class);

        StreamUtil.CompletableFromJFRunnable(jfRunnableMock).subscribe();

        verify(jfRunnableMock).run();
    }

    @Test
    public void streamOptionalReturnsEmptyStreamWhenOptionalIsEmpty() {
        final Optional<?> emptyOptional = Optional.empty();

        final Stream<?> streamOptional = StreamUtil.optionalStream(emptyOptional);

        assertThat(streamOptional.count(), equalTo(0L));
    }

    @Test
    public void streamOptionalReturnsStreamOfOptionalInstance() {
        final Optional<Long> optional = Optional.of(1L);

        final List<Long> streamList = StreamUtil
                .optionalStream(optional)
                .collect(Collectors.toList());

        assertThat(streamList.size(), equalTo(1));
        assertThat(streamList.get(0), equalTo(1L));
    }
}
