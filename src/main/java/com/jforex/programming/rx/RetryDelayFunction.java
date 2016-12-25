package com.jforex.programming.rx;

import io.reactivex.functions.Function;

public interface RetryDelayFunction extends Function<Integer, RetryDelay> {
}
