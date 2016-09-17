package com.jforex.programming.order.event;

import java.util.EnumSet;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

public class OrderEventTypeData {

    private final ImmutableSet<OrderEventType> doneEventTypes;
    private final ImmutableSet<OrderEventType> rejectEventTypes;
    private final ImmutableSet<OrderEventType> infoEventTypes;
    private final ImmutableSet<OrderEventType> allEventTypes;
    private final ImmutableSet<OrderEventType> finishEventTypes;

    public OrderEventTypeData(final EnumSet<OrderEventType> doneEventTypes,
                              final EnumSet<OrderEventType> rejectEventTypes,
                              final EnumSet<OrderEventType> infoEventTypes) {
        this.doneEventTypes = Sets.immutableEnumSet(doneEventTypes);
        this.rejectEventTypes = Sets.immutableEnumSet(rejectEventTypes);
        this.infoEventTypes = Sets.immutableEnumSet(infoEventTypes);
        allEventTypes = Sets.immutableEnumSet(Sets.union(infoEventTypes,
                                                         Sets.union(doneEventTypes, rejectEventTypes)));
        finishEventTypes = Sets.immutableEnumSet(Sets.union(doneEventTypes, rejectEventTypes));
    }

    public Set<OrderEventType> doneEventTypes() {
        return doneEventTypes;
    }

    public Set<OrderEventType> rejectEventTypes() {
        return rejectEventTypes;
    }

    public Set<OrderEventType> infoEventTypes() {
        return infoEventTypes;
    }

    public Set<OrderEventType> allEventTypes() {
        return allEventTypes;
    }

    public Set<OrderEventType> finishEventTypes() {
        return finishEventTypes;
    }
}
