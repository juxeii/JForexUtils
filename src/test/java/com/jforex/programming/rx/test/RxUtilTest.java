package com.jforex.programming.rx.test;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.mockito.Mock;

import com.jforex.programming.rx.RxUtil;
import com.jforex.programming.test.common.CommonUtilForTest;
import com.jforex.programming.test.common.RxTestUtil;

import io.reactivex.functions.Action;
import io.reactivex.observers.TestObserver;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

public class RxUtilTest extends CommonUtilForTest {

    private final Subject<Throwable> throwableSubject = PublishSubject.create();
    private static TimeUnit timeUnit = TimeUnit.MILLISECONDS;

    @Mock
    private Action actionMock;

    private void advanceTime(final long delay) {
        RxTestUtil.advanceTimeInMillisBy(delay);
    }

    private void emitErrorAndAdvanceTime() {
        throwableSubject.onError(jfException);
        advanceTime(delayInMillis);
    }

    private void emitThrowableAndAdvanceTime() {
        throwableSubject.onNext(jfException);
        advanceTime(delayInMillis);
    }

    @Test
    public void testConstructorIsPrivate() throws Exception {
        assertPrivateConstructor(RxUtil.class);
    }

    @Test
    public void retryWithDelayIsCorrect() {
        final TestObserver<Throwable> subscriber = throwableSubject
            .retryWhen(RxUtil.retryWithDelay(retryParams))
            .test();

        emitThrowableAndAdvanceTime();
        subscriber.assertValue(jfException);

        emitErrorAndAdvanceTime();
        subscriber.assertValue(jfException);

        emitErrorAndAdvanceTime();
        subscriber.assertValue(jfException);

        emitErrorAndAdvanceTime();
        subscriber.assertError(jfException);
    }

    @Test
    public void retryWithDelayAndPredicateIsCorrect() {
        final TestObserver<Throwable> subscriber = throwableSubject
            .retryWhen(RxUtil.retryWithDelay(retryParams, (err, attempt) -> attempt < 3))
            .test();

        emitThrowableAndAdvanceTime();
        subscriber.assertValue(jfException);

        emitErrorAndAdvanceTime();
        subscriber.assertValue(jfException);

        emitErrorAndAdvanceTime();
        subscriber.assertError(jfException);
    }

    @Test
    public void retryWithDelayAndFalsePredicateEmitsError() {
        final TestObserver<Throwable> subscriber = throwableSubject
            .retryWhen(RxUtil.retryWithDelay(retryParams, (err, attempt) -> false))
            .test();

        emitThrowableAndAdvanceTime();
        subscriber.assertValue(jfException);

        emitErrorAndAdvanceTime();
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
            .wait(delayInMillis, timeUnit)
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
