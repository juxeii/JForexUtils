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
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Test;

import com.google.common.base.Function;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.event.OrderEventTypeData;

public class OrderEventTypeDataTest {

    private OrderEventTypeData orderEventTypeData;

    private void assertAllTypes(final OrderEventType... orderEventTypes) {
        assertTrue(orderEventTypeData.all().contains(NOTIFICATION));
        assertTypes(orderEventTypeData.all()::contains, orderEventTypes);
    }

    private void assertDoneTypes(final OrderEventType... orderEventTypes) {
        assertTypes(orderEventTypeData::isDoneType, orderEventTypes);
    }

    private void assertRejectTypes(final OrderEventType... orderEventTypes) {
        assertTypes(orderEventTypeData::isRejectType, orderEventTypes);
    }

    private void assertTypes(final Function<OrderEventType, Boolean> typeTestFunction,
                             final OrderEventType... orderEventTypes) {
        new ArrayList<OrderEventType>(Arrays.asList(orderEventTypes))
                .forEach(type -> assertTrue(typeTestFunction.apply(type)));
    }

    private void assertCallReason(final OrderCallReason callReason) {
        assertThat(orderEventTypeData.callReason(), equalTo(callReason));
    }

    @Test
    public void typeDataForSubmitIsCorrect() {
        orderEventTypeData = OrderEventTypeData.submitData;

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

        assertCallReason(OrderCallReason.SUBMIT);
    }

    @Test
    public void typeDataForMergeIsCorrect() {
        orderEventTypeData = OrderEventTypeData.mergeData;

        assertDoneTypes(MERGE_OK,
                        MERGE_CLOSE_OK);

        assertRejectTypes(MERGE_REJECTED);

        assertAllTypes(MERGE_OK,
                       MERGE_CLOSE_OK,
                       MERGE_REJECTED);

        assertCallReason(OrderCallReason.MERGE);
    }

    @Test
    public void typeDataForCloseIsCorrect() {
        orderEventTypeData = OrderEventTypeData.closeData;

        assertDoneTypes(CLOSE_OK);

        assertRejectTypes(CLOSE_REJECTED);

        assertAllTypes(CLOSE_OK,
                       CLOSE_REJECTED,
                       PARTIAL_CLOSE_OK);

        assertCallReason(OrderCallReason.CLOSE);
    }

    @Test
    public void typeDataForChangeLabelIsCorrect() {
        orderEventTypeData = OrderEventTypeData.changeLabelData;

        assertDoneTypes(CHANGED_LABEL);

        assertRejectTypes(CHANGE_LABEL_REJECTED);

        assertAllTypes(CHANGED_LABEL,
                       CHANGE_LABEL_REJECTED);

        assertCallReason(OrderCallReason.CHANGE_LABEL);
    }

    @Test
    public void typeDataForChangeGTTIsCorrect() {
        orderEventTypeData = OrderEventTypeData.changeGTTData;

        assertDoneTypes(CHANGED_GTT);

        assertRejectTypes(CHANGE_GTT_REJECTED);

        assertAllTypes(CHANGED_GTT,
                       CHANGE_GTT_REJECTED);

        assertCallReason(OrderCallReason.CHANGE_GTT);
    }

    @Test
    public void typeDataForChangeOpenPriceIsCorrect() {
        orderEventTypeData = OrderEventTypeData.changeOpenPriceData;

        assertDoneTypes(CHANGED_PRICE);

        assertRejectTypes(CHANGE_PRICE_REJECTED);

        assertAllTypes(CHANGED_PRICE,
                       CHANGE_PRICE_REJECTED);

        assertCallReason(OrderCallReason.CHANGE_PRICE);
    }

    @Test
    public void typeDataForChangeAmountIsCorrect() {
        orderEventTypeData = OrderEventTypeData.changeAmountData;

        assertDoneTypes(CHANGED_AMOUNT);

        assertRejectTypes(CHANGE_AMOUNT_REJECTED);

        assertAllTypes(CHANGED_AMOUNT,
                       CHANGE_AMOUNT_REJECTED);

        assertCallReason(OrderCallReason.CHANGE_AMOUNT);
    }

    @Test
    public void typeDataForChangeSLIsCorrect() {
        orderEventTypeData = OrderEventTypeData.changeSLData;

        assertDoneTypes(CHANGED_SL);

        assertRejectTypes(CHANGE_SL_REJECTED);

        assertAllTypes(CHANGED_SL,
                       CHANGE_SL_REJECTED);

        assertCallReason(OrderCallReason.CHANGE_SL);
    }

    @Test
    public void typeDataForChangeTPIsCorrect() {
        orderEventTypeData = OrderEventTypeData.changeTPData;

        assertDoneTypes(CHANGED_TP);

        assertRejectTypes(CHANGE_TP_REJECTED);

        assertAllTypes(CHANGED_TP,
                       CHANGE_TP_REJECTED);

        assertCallReason(OrderCallReason.CHANGE_TP);
    }
}
