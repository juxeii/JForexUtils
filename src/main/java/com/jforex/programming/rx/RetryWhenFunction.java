package com.jforex.programming.rx;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;

public interface RetryWhenFunction extends Function<Observable<Throwable>,
                                                    ObservableSource<Long>> {
}
