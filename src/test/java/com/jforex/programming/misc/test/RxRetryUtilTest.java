package com.jforex.programming.misc.test;

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

import com.jforex.programming.misc.RxRetryUtil;
import com.jforex.programming.test.common.CommonUtilForTest;
import com.jforex.programming.test.common.RxTestUtil;

import io.reactivex.observers.TestObserver;

public class RxRetryUtilTest extends CommonUtilForTest {

    private static final int maxRetries = 3;
    private static final long retryDelay = 1500L;
    private static TimeUnit timeUnit = TimeUnit.MILLISECONDS;

    private TestObserver<Long> subscribeCheckRetries(final int currentRetryNumber) {
        final TestObserver<Long> subscriber = TestObserver.create();

        RxRetryUtil
            .checkRetriesObservable(Pair.of(jfException, currentRetryNumber),
                                    retryDelay,
                                    timeUnit,
                                    maxRetries)
            .subscribe(subscriber);

        return subscriber;
    }

    @Test
    public void testConstructorIsPrivate() throws Exception {
        assertPrivateConstructor(RxRetryUtil.class);
    }

    @Test
    public void counterObservableCountsCorrect() {
        RxRetryUtil
            .counterObservable(3)
            .test()
            .assertComplete()
            .assertNoErrors()
            .assertValues(1, 2, 3, 4);
    }

    @Test
    public void waitObservableIsCorrect() {
        final TestObserver<Long> subscriber = TestObserver.create();

        RxRetryUtil
            .waitObservable(1000L, timeUnit)
            .subscribe(subscriber);

        RxTestUtil.advanceTimeBy(900L, timeUnit);
        subscriber.assertNotComplete();
        RxTestUtil.advanceTimeBy(100L, timeUnit);
        subscriber.assertComplete();
    }

    @Test
    public void checkRetriesReturnsWaitObservableWhenNotAllRetriesDone() {
        final TestObserver<Long> subscriber = subscribeCheckRetries(1);
        RxTestUtil.advanceTimeBy(retryDelay, timeUnit);

        subscriber.assertComplete();
    }

    @Test
    public void checkRetriesReturnsErrorWhenNotAllRetriesFailed() {
        final TestObserver<Long> subscriber = subscribeCheckRetries(maxRetries + 1);

        subscriber.assertError(jfException);
    }
}
