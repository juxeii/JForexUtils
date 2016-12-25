package com.jforex.programming.rx;

import java.util.function.Function;

public interface RetryDelayFunction extends Function<Integer, RetryDelay> {
}
