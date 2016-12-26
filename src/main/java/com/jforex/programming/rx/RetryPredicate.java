package com.jforex.programming.rx;

import java.util.function.BiFunction;

public interface RetryPredicate extends BiFunction<Throwable, Integer, Boolean> {
}
