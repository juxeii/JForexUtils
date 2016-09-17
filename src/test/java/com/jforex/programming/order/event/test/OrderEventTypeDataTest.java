package com.jforex.programming.order.event.test;

import static com.jforex.programming.order.event.OrderEventType.CLOSE_OK;
import static com.jforex.programming.order.event.OrderEventType.CLOSE_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.NOTIFICATION;
import static com.jforex.programming.order.event.OrderEventType.PARTIAL_CLOSE_OK;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.Assert.assertThat;

import java.util.EnumSet;

import org.junit.Before;
import org.junit.Test;

import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.event.OrderEventTypeData;

public class OrderEventTypeDataTest {

    private OrderEventTypeData orderEventTypeData;
    private final EnumSet<OrderEventType> doneEvents = EnumSet.of(CLOSE_OK);
    private final EnumSet<OrderEventType> rejectEvents = EnumSet.of(CLOSE_REJECTED);
    private final EnumSet<OrderEventType> infoEventTypes = EnumSet.of(NOTIFICATION, PARTIAL_CLOSE_OK);

    @Before
    public void setUp() {
        orderEventTypeData = new OrderEventTypeData(doneEvents,
                                                    rejectEvents,
                                                    infoEventTypes);
    }

    @Test
    public void doneTypeSetIsCorrect() {
        assertThat(orderEventTypeData.doneEventTypes(), equalTo(doneEvents));
    }

    @Test
    public void rejectTypeSetIsCorrect() {
        assertThat(orderEventTypeData.rejectEventTypes(), equalTo(rejectEvents));
    }

    @Test
    public void infoTypeSetIsCorrect() {
        assertThat(orderEventTypeData.infoEventTypes(), equalTo(infoEventTypes));
    }

    @Test
    public void allTypesSetIsCorrect() {
        assertThat(orderEventTypeData.allEventTypes(),
                   containsInAnyOrder(CLOSE_OK,
                                      CLOSE_REJECTED,
                                      PARTIAL_CLOSE_OK,
                                      NOTIFICATION));
    }

    @Test
    public void finishTypesSetIsCorrect() {
        assertThat(orderEventTypeData.finishEventTypes(),
                   containsInAnyOrder(CLOSE_OK,
                                      CLOSE_REJECTED));
    }
}
