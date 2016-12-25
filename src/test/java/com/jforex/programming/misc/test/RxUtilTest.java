package com.jforex.programming.misc.test;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.mockito.Mock;

import com.jforex.programming.misc.RetryDelay;
import com.jforex.programming.misc.RxUtil;
import com.jforex.programming.test.common.CommonUtilForTest;
import com.jforex.programming.test.common.RxTestUtil;

import io.reactivex.functions.Action;
import io.reactivex.observers.TestObserver;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

public class RxUtilTest extends CommonUtilForTest {

    private final Subject<Throwable> throwableSubject = PublishSubject.create();
    private static TimeUnit timeUnit = TimeUnit.MILLISECONDS;
    private static long delay = 1500L;

    @Mock
    private Action actionMock;

    private void advanceTime(final long delay) {
        RxTestUtil.advanceTimeInMillisBy(delay);
    }

    private void emitThrowableAndAdvanceTime() {
        throwableSubject.onNext(jfException);
        advanceTime(delay);
    }

    @Test
    public void testConstructorIsPrivate() throws Exception {
        assertPrivateConstructor(RxUtil.class);
    }

    @Test
    public void retryComposerIsCorrect() {
        final TestObserver<Long> subscriber = throwableSubject
            .compose(RxUtil.retryWhenComposer(2,
                                              attempt -> new RetryDelay(delay, timeUnit)))
            .test();

        emitThrowableAndAdvanceTime();
        subscriber.assertValue(0L);

        emitThrowableAndAdvanceTime();
        subscriber.assertValues(0L, 0L);

        emitThrowableAndAdvanceTime();
        subscriber.assertError(jfException);
    }

    @Test
    public void counterObservableCountsCorrect() {
        RxUtil
            .retryCounter(3)
            .test()
            .assertComplete()
            .assertNoErrors()
            .assertValues(1, 2, 3, 4);
    }

    @Test
    public void waitObservableIsCorrect() {
        final TestObserver<Long> subscriber = RxUtil
            .wait(delay, timeUnit)
            .test();

        advanceTime(1300L);
        subscriber.assertNotComplete();
        advanceTime(200L);
        subscriber.assertComplete();
    }

    @Test
    public void actionToCallabeIsCorrect() throws Exception {
        final Callable<Boolean> callable = RxUtil.actionToCallable(actionMock);

        callable.call();

        verify(actionMock).run();
    }
}
