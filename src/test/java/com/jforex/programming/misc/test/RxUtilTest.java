package com.jforex.programming.misc.test;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.jforex.programming.misc.RxUtil;
import com.jforex.programming.test.common.CommonUtilForTest;
import com.jforex.programming.test.common.RxTestUtil;

import io.reactivex.observers.TestObserver;

public class RxUtilTest extends CommonUtilForTest {

    private static TimeUnit timeUnit = TimeUnit.MILLISECONDS;

    @Test
    public void testConstructorIsPrivate() throws Exception {
        assertPrivateConstructor(RxUtil.class);
    }

    @Test
    public void counterObservableCountsCorrect() {
        RxUtil
            .counterObservable(3)
            .test()
            .assertComplete()
            .assertNoErrors()
            .assertValues(1, 2, 3, 4);
    }

    @Test
    public void waitObservableIsCorrect() {
        final TestObserver<Long> subscriber = TestObserver.create();

        RxUtil
            .waitObservable(1000L, timeUnit)
            .subscribe(subscriber);

        RxTestUtil.advanceTimeInMillisBy(900L);
        subscriber.assertNotComplete();
        RxTestUtil.advanceTimeInMillisBy(100L);
        subscriber.assertComplete();
    }
}
