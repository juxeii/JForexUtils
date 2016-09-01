package com.jforex.programming.order.event.test;

import static com.jforex.programming.order.event.OrderEventType.CHANGED_PRICE;
import static com.jforex.programming.order.event.OrderEventType.CLOSE_OK;
import static com.jforex.programming.order.event.OrderEventType.CLOSE_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.MERGE_OK;
import static com.jforex.programming.order.event.OrderEventType.NOTIFICATION;
import static com.jforex.programming.order.event.OrderEventType.PARTIAL_CLOSE_OK;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.Assert.assertThat;

import java.util.EnumSet;

import org.junit.Before;
import org.junit.Test;

import com.jforex.programming.order.event.OrderEventTypeData;

public class OrderEventTypeDataTest {

    private OrderEventTypeData orderEventTypeData;

    @Before
    public void setUp() {
        orderEventTypeData = new OrderEventTypeData(EnumSet.of(CLOSE_OK, MERGE_OK),
                                                    EnumSet.of(CLOSE_REJECTED, CHANGED_PRICE),
                                                    EnumSet.of(NOTIFICATION, PARTIAL_CLOSE_OK));
    }

    @Test
    public void testDoneTypesAreCorrect() {
        assertThat(orderEventTypeData.doneEventTypes(),
                   containsInAnyOrder(CLOSE_OK, MERGE_OK));
    }

    @Test
    public void testAllTypesAreCorrect() {
        assertThat(orderEventTypeData.allEventTypes(),
                   containsInAnyOrder(NOTIFICATION,
                                      PARTIAL_CLOSE_OK,
                                      CLOSE_OK,
                                      MERGE_OK,
                                      CLOSE_REJECTED,
                                      CHANGED_PRICE));
    }
}
