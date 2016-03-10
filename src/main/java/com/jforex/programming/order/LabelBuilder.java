package com.jforex.programming.order;

import com.dukascopy.api.Instrument;

@FunctionalInterface
public interface LabelBuilder {

    abstract String create(Instrument instrument);
}
