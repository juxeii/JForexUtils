package com.jforex.programming.order.event.test;

import static com.jforex.programming.order.event.OrderEventType.CHANGED_AMOUNT;
import static com.jforex.programming.order.event.OrderEventType.CHANGED_GTT;
import static com.jforex.programming.order.event.OrderEventType.CHANGED_LABEL;
import static com.jforex.programming.order.event.OrderEventType.CHANGED_PRICE;
import static com.jforex.programming.order.event.OrderEventType.CHANGED_SL;
import static com.jforex.programming.order.event.OrderEventType.CHANGED_TP;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_AMOUNT_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_GTT_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_LABEL_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_PRICE_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_SL_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_TP_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.CLOSE_OK;
import static com.jforex.programming.order.event.OrderEventType.CLOSE_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.FILL_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.FULLY_FILLED;
import static com.jforex.programming.order.event.OrderEventType.MERGE_CLOSE_OK;
import static com.jforex.programming.order.event.OrderEventType.MERGE_OK;
import static com.jforex.programming.order.event.OrderEventType.MERGE_REJECTED;
import static com.jforex.programming.order.event.OrderEventType.NOTIFICATION;
import static com.jforex.programming.order.event.OrderEventType.PARTIAL_CLOSE_OK;
import static com.jforex.programming.order.event.OrderEventType.PARTIAL_FILL_OK;
import static com.jforex.programming.order.event.OrderEventType.SUBMIT_CONDITIONAL_OK;
import static com.jforex.programming.order.event.OrderEventType.SUBMIT_OK;
import static com.jforex.programming.order.event.OrderEventType.SUBMIT_REJECTED;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Test;

import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.event.OrderEventTypeData;

public class OrderEventTypeDataTest {

    private OrderEventTypeData orderEventTypeData;

    private void assertAllTypes(final OrderEventType... orderEventTypes) {
        assertTrue(orderEventTypeData.allEventTypes().contains(NOTIFICATION));
        new ArrayList<>(Arrays.asList(orderEventTypes))
            .forEach(type -> assertTrue(orderEventTypeData.allEventTypes().contains(type)));
    }

    private void assertDoneTypes(final OrderEventType... orderEventTypes) {
        new ArrayList<>(Arrays.asList(orderEventTypes))
            .forEach(type -> assertTrue(orderEventTypeData.doneEventTypes().contains(type)));
    }

    private void assertRejectTypes(final OrderEventType... orderEventTypes) {
        new ArrayList<>(Arrays.asList(orderEventTypes))
            .forEach(type -> assertTrue(orderEventTypeData.rejectEventTypes().contains(type)));
    }

    @Test
    public void typeDataForSubmitIsCorrect() {
        orderEventTypeData = OrderEventTypeData.submitEventTypeData;

        assertDoneTypes(FULLY_FILLED,
                        SUBMIT_CONDITIONAL_OK);

        assertRejectTypes(FILL_REJECTED,
                          SUBMIT_REJECTED);

        assertAllTypes(FULLY_FILLED,
                       SUBMIT_CONDITIONAL_OK,
                       FILL_REJECTED,
                       SUBMIT_REJECTED,
                       SUBMIT_OK,
                       PARTIAL_FILL_OK);
    }

    @Test
    public void typeDataForMergeIsCorrect() {
        orderEventTypeData = OrderEventTypeData.mergeEventTypeData;

        assertDoneTypes(MERGE_OK,
                        MERGE_CLOSE_OK);

        assertRejectTypes(MERGE_REJECTED);

        assertAllTypes(MERGE_OK,
                       MERGE_CLOSE_OK,
                       MERGE_REJECTED);
    }

    @Test
    public void typeDataForCloseIsCorrect() {
        orderEventTypeData = OrderEventTypeData.closeEventTypeData;

        assertDoneTypes(CLOSE_OK);

        assertRejectTypes(CLOSE_REJECTED);

        assertAllTypes(CLOSE_OK,
                       CLOSE_REJECTED,
                       PARTIAL_CLOSE_OK);
    }

    @Test
    public void typeDataForChangeLabelIsCorrect() {
        orderEventTypeData = OrderEventTypeData.changeLabelEventTypeData;

        assertDoneTypes(CHANGED_LABEL);

        assertRejectTypes(CHANGE_LABEL_REJECTED);

        assertAllTypes(CHANGED_LABEL,
                       CHANGE_LABEL_REJECTED);
    }

    @Test
    public void typeDataForChangeGTTIsCorrect() {
        orderEventTypeData = OrderEventTypeData.changeGTTEventTypeData;

        assertDoneTypes(CHANGED_GTT);

        assertRejectTypes(CHANGE_GTT_REJECTED);

        assertAllTypes(CHANGED_GTT,
                       CHANGE_GTT_REJECTED);
    }

    @Test
    public void typeDataForChangeOpenPriceIsCorrect() {
        orderEventTypeData = OrderEventTypeData.changeOpenPriceEventTypeData;

        assertDoneTypes(CHANGED_PRICE);

        assertRejectTypes(CHANGE_PRICE_REJECTED);

        assertAllTypes(CHANGED_PRICE,
                       CHANGE_PRICE_REJECTED);
    }

    @Test
    public void typeDataForChangeAmountIsCorrect() {
        orderEventTypeData = OrderEventTypeData.changeAmountEventTypeData;

        assertDoneTypes(CHANGED_AMOUNT);

        assertRejectTypes(CHANGE_AMOUNT_REJECTED);

        assertAllTypes(CHANGED_AMOUNT,
                       CHANGE_AMOUNT_REJECTED);
    }

    @Test
    public void typeDataForChangeSLIsCorrect() {
        orderEventTypeData = OrderEventTypeData.changeSLEventTypeData;

        assertDoneTypes(CHANGED_SL);

        assertRejectTypes(CHANGE_SL_REJECTED);

        assertAllTypes(CHANGED_SL,
                       CHANGE_SL_REJECTED);
    }

    @Test
    public void typeDataForChangeTPIsCorrect() {
        orderEventTypeData = OrderEventTypeData.changeTPEventTypeData;

        assertDoneTypes(CHANGED_TP);

        assertRejectTypes(CHANGE_TP_REJECTED);

        assertAllTypes(CHANGED_TP,
                       CHANGE_TP_REJECTED);
    }
}
