package com.jforex.programming.rx;

import org.reactivestreams.Publisher;

import io.reactivex.Flowable;
import io.reactivex.functions.Function;

public interface RetryWhenFunctionForSingle extends Function<Flowable<Throwable>,
                                                             Publisher<Object>> {
}
