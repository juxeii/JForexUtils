package com.jforex.programming.misc.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import com.jforex.programming.misc.JFRunnable;
import com.jforex.programming.misc.StreamUtil;
import com.jforex.programming.test.common.CommonUtilForTest;

import rx.observers.TestSubscriber;

public class StreamUtilTest extends CommonUtilForTest {

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

        rxTestUtil.advanceTimeBy(900L, TimeUnit.MILLISECONDS);
        subscriber.assertNotCompleted();
        rxTestUtil.advanceTimeBy(100L, TimeUnit.MILLISECONDS);
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

        final Stream<?> streamOptional = StreamUtil.streamOptional(emptyOptional);

        assertThat(streamOptional.count(), equalTo(0L));
    }

    @Test
    public void streamOptionalReturnsStreamOfOptionalInstance() {
        final Optional<Long> optional = Optional.of(1L);

        final List<Long> streamList = StreamUtil
                .streamOptional(optional)
                .collect(Collectors.toList());

        assertThat(streamList.size(), equalTo(1));
        assertThat(streamList.get(0), equalTo(1L));
    }
}
