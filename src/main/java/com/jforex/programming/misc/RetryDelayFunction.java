package com.jforex.programming.misc;

import io.reactivex.functions.Function;

public interface RetryDelayFunction extends Function<Integer, RetryDelay> {
}
