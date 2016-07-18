package com.jforex.programming.order.event.test;

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
        assertTrue(orderEventTypeData.all().contains(OrderEventType.NOTIFICATION));
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

        assertDoneTypes(OrderEventType.FULLY_FILLED,
                        OrderEventType.SUBMIT_CONDITIONAL_OK);

        assertRejectTypes(OrderEventType.FILL_REJECTED,
                          OrderEventType.SUBMIT_REJECTED);

        assertAllTypes(OrderEventType.FULLY_FILLED,
                       OrderEventType.SUBMIT_CONDITIONAL_OK,
                       OrderEventType.FILL_REJECTED,
                       OrderEventType.SUBMIT_REJECTED,
                       OrderEventType.SUBMIT_OK,
                       OrderEventType.PARTIAL_FILL_OK);

        assertCallReason(OrderCallReason.SUBMIT);
    }

    @Test
    public void typeDataForMergeIsCorrect() {
        orderEventTypeData = OrderEventTypeData.mergeData;

        assertDoneTypes(OrderEventType.MERGE_OK,
                        OrderEventType.MERGE_CLOSE_OK);

        assertRejectTypes(OrderEventType.MERGE_REJECTED);

        assertAllTypes(OrderEventType.MERGE_OK,
                       OrderEventType.MERGE_CLOSE_OK,
                       OrderEventType.MERGE_REJECTED);

        assertCallReason(OrderCallReason.MERGE);
    }

    @Test
    public void typeDataForCloseIsCorrect() {
        orderEventTypeData = OrderEventTypeData.closeData;

        assertDoneTypes(OrderEventType.CLOSE_OK);

        assertRejectTypes(OrderEventType.CLOSE_REJECTED);

        assertAllTypes(OrderEventType.CLOSE_OK,
                       OrderEventType.CLOSE_REJECTED,
                       OrderEventType.PARTIAL_CLOSE_OK);

        assertCallReason(OrderCallReason.CLOSE);
    }

    @Test
    public void typeDataForChangeLabelIsCorrect() {
        orderEventTypeData = OrderEventTypeData.changeLabelData;

        assertDoneTypes(OrderEventType.CHANGED_LABEL);

        assertRejectTypes(OrderEventType.CHANGE_LABEL_REJECTED);

        assertAllTypes(OrderEventType.CHANGED_LABEL,
                       OrderEventType.CHANGE_LABEL_REJECTED);

        assertCallReason(OrderCallReason.CHANGE_LABEL);
    }

    @Test
    public void typeDataForChangeGTTIsCorrect() {
        orderEventTypeData = OrderEventTypeData.changeGTTData;

        assertDoneTypes(OrderEventType.CHANGED_GTT);

        assertRejectTypes(OrderEventType.CHANGE_GTT_REJECTED);

        assertAllTypes(OrderEventType.CHANGED_GTT,
                       OrderEventType.CHANGE_GTT_REJECTED);

        assertCallReason(OrderCallReason.CHANGE_GTT);
    }

    @Test
    public void typeDataForChangeOpenPriceIsCorrect() {
        orderEventTypeData = OrderEventTypeData.changeOpenPriceData;

        assertDoneTypes(OrderEventType.CHANGED_PRICE);

        assertRejectTypes(OrderEventType.CHANGE_PRICE_REJECTED);

        assertAllTypes(OrderEventType.CHANGED_PRICE,
                       OrderEventType.CHANGE_PRICE_REJECTED);

        assertCallReason(OrderCallReason.CHANGE_PRICE);
    }

    @Test
    public void typeDataForChangeAmountIsCorrect() {
        orderEventTypeData = OrderEventTypeData.changeAmountData;

        assertDoneTypes(OrderEventType.CHANGED_AMOUNT);

        assertRejectTypes(OrderEventType.CHANGE_AMOUNT_REJECTED);

        assertAllTypes(OrderEventType.CHANGED_AMOUNT,
                       OrderEventType.CHANGE_AMOUNT_REJECTED);

        assertCallReason(OrderCallReason.CHANGE_AMOUNT);
    }

    @Test
    public void typeDataForChangeSLIsCorrect() {
        orderEventTypeData = OrderEventTypeData.changeSLData;

        assertDoneTypes(OrderEventType.CHANGED_SL);

        assertRejectTypes(OrderEventType.CHANGE_SL_REJECTED);

        assertAllTypes(OrderEventType.CHANGED_SL,
                       OrderEventType.CHANGE_SL_REJECTED);

        assertCallReason(OrderCallReason.CHANGE_SL);
    }

    @Test
    public void typeDataForChangeTPIsCorrect() {
        orderEventTypeData = OrderEventTypeData.changeTPData;

        assertDoneTypes(OrderEventType.CHANGED_TP);

        assertRejectTypes(OrderEventType.CHANGE_TP_REJECTED);

        assertAllTypes(OrderEventType.CHANGED_TP,
                       OrderEventType.CHANGE_TP_REJECTED);

        assertCallReason(OrderCallReason.CHANGE_TP);
    }
}
