package com.jforex.programming.order.event;

import java.util.EnumSet;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

public final class OrderEventTypeData {

    private final ImmutableSet<OrderEventType> doneEventTypes;
    private final ImmutableSet<OrderEventType> rejectEventTypes;
    private final ImmutableSet<OrderEventType> allEventTypes;

    public OrderEventTypeData(final EnumSet<OrderEventType> doneEventTypes,
                              final EnumSet<OrderEventType> rejectEventTypes,
                              final EnumSet<OrderEventType> infoEventTypes) {
        this.doneEventTypes = Sets.immutableEnumSet(doneEventTypes);
        this.rejectEventTypes = Sets.immutableEnumSet(rejectEventTypes);
        allEventTypes = Sets.immutableEnumSet(Sets.union(infoEventTypes,
                                                         Sets.union(doneEventTypes, rejectEventTypes)));
    }

    public final Set<OrderEventType> allEventTypes() {
        return allEventTypes;
    }

    public final Set<OrderEventType> doneEventTypes() {
        return doneEventTypes;
    }

    public final Set<OrderEventType> rejectEventTypes() {
        return rejectEventTypes;
    }
}
