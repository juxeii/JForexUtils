package com.jforex.programming.misc;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Optional;
import java.util.stream.Stream;

public final class StreamUtil {

    private StreamUtil() {
    }

    public static final <T> Stream<T> optionalToStream(final Optional<T> optional) {
        checkNotNull(optional);

        return optional.isPresent()
                ? Stream.of(optional.get())
                : Stream.empty();
    }
}
