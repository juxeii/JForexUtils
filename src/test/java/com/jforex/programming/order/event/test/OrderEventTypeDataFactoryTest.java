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
import static com.jforex.programming.order.event.OrderEventType.SUBMIT_OK;
import static com.jforex.programming.order.event.OrderEventType.SUBMIT_REJECTED;
import static org.junit.Assert.assertEquals;

import java.util.EnumSet;
import java.util.Set;
import java.util.function.Supplier;

import org.junit.Test;

import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.event.OrderEventTypeData;
import com.jforex.programming.order.event.OrderEventTypeDataFactory;
import com.jforex.programming.test.common.CommonUtilForTest;

public class OrderEventTypeDataFactoryTest extends CommonUtilForTest {

    private final OrderEventTypeDataFactory orderEventTypeDataFactory = new OrderEventTypeDataFactory();

    private OrderEventTypeData typeData;

    private void assertDoneEventTypes(final EnumSet<OrderEventType> expectedSet) {
        assertEventTypes(expectedSet, typeData::doneEventTypes);
    }

    private void assertRejectEventTypes(final EnumSet<OrderEventType> expectedSet) {
        assertEventTypes(expectedSet, typeData::rejectEventTypes);
    }

    private void assertInfoEventTypes(final EnumSet<OrderEventType> expectedSet) {
        assertEventTypes(expectedSet, typeData::infoEventTypes);
    }

    private void assertAllEventTypes(final EnumSet<OrderEventType> expectedSet) {
        assertEventTypes(expectedSet, typeData::allEventTypes);
    }

    private void assertFinishEventTypes(final EnumSet<OrderEventType> expectedSet) {
        assertEventTypes(expectedSet, typeData::finishEventTypes);
    }

    private void assertEventTypes(final EnumSet<OrderEventType> expectedSet,
                                  final Supplier<Set<OrderEventType>> actualSet) {
        assertEquals(expectedSet, actualSet.get());
    }

    @Test
    public void submitEventTypeDataIsCorrect() {
        typeData = orderEventTypeDataFactory.forCallReason(OrderCallReason.SUBMIT);

        assertDoneEventTypes(EnumSet.of(FULLY_FILLED));
        assertRejectEventTypes(EnumSet.of(FILL_REJECTED,
                                          SUBMIT_REJECTED));
        assertInfoEventTypes(EnumSet.of(NOTIFICATION,
                                        SUBMIT_OK,
                                        PARTIAL_FILL_OK));
        assertFinishEventTypes(EnumSet.of(FULLY_FILLED,
                                          FILL_REJECTED,
                                          SUBMIT_REJECTED));
        assertAllEventTypes(EnumSet.of(FULLY_FILLED,
                                       SUBMIT_OK,
                                       FILL_REJECTED,
                                       SUBMIT_REJECTED,
                                       NOTIFICATION,
                                       PARTIAL_FILL_OK));
    }

    @Test
    public void conditionalSubmitEventTypeDataIsCorrect() {
        typeData = orderEventTypeDataFactory.forCallReason(OrderCallReason.SUBMIT_CONDITIONAL);

        assertDoneEventTypes(EnumSet.of(SUBMIT_OK));
        assertRejectEventTypes(EnumSet.of(SUBMIT_REJECTED));
        assertInfoEventTypes(EnumSet.of(NOTIFICATION));
        assertFinishEventTypes(EnumSet.of(SUBMIT_OK, SUBMIT_REJECTED));
        assertAllEventTypes(EnumSet.of(SUBMIT_OK,
                                       SUBMIT_REJECTED,
                                       NOTIFICATION));
    }

    @Test
    public void mergeEventTypeDataIsCorrect() {
        typeData = orderEventTypeDataFactory.forCallReason(OrderCallReason.MERGE);

        assertDoneEventTypes(EnumSet.of(MERGE_OK,
                                        MERGE_CLOSE_OK));
        assertRejectEventTypes(EnumSet.of(MERGE_REJECTED));
        assertInfoEventTypes(EnumSet.of(NOTIFICATION));
        assertFinishEventTypes(EnumSet.of(MERGE_OK,
                                          MERGE_CLOSE_OK,
                                          MERGE_REJECTED));
        assertAllEventTypes(EnumSet.of(NOTIFICATION,
                                       MERGE_OK,
                                       MERGE_CLOSE_OK,
                                       MERGE_REJECTED));
    }

    @Test
    public void closeEventTypeDataIsCorrect() {
        typeData = orderEventTypeDataFactory.forCallReason(OrderCallReason.CLOSE);

        assertDoneEventTypes(EnumSet.of(CLOSE_OK));
        assertRejectEventTypes(EnumSet.of(CLOSE_REJECTED));
        assertInfoEventTypes(EnumSet.of(NOTIFICATION,
                                        PARTIAL_CLOSE_OK));
        assertFinishEventTypes(EnumSet.of(CLOSE_OK,
                                          CLOSE_REJECTED));
        assertAllEventTypes(EnumSet.of(NOTIFICATION,
                                       CLOSE_OK,
                                       CLOSE_REJECTED,
                                       PARTIAL_CLOSE_OK));
    }

    @Test
    public void partialCloseEventTypeDataIsCorrect() {
        typeData = orderEventTypeDataFactory.forCallReason(OrderCallReason.PARTIAL_CLOSE);

        assertDoneEventTypes(EnumSet.of(PARTIAL_CLOSE_OK, CLOSE_OK));
        assertRejectEventTypes(EnumSet.of(CLOSE_REJECTED));
        assertInfoEventTypes(EnumSet.of(NOTIFICATION));
        assertFinishEventTypes(EnumSet.of(PARTIAL_CLOSE_OK,
                                          CLOSE_OK,
                                          CLOSE_REJECTED));
        assertAllEventTypes(EnumSet.of(NOTIFICATION,
                                       CLOSE_OK,
                                       CLOSE_REJECTED,
                                       PARTIAL_CLOSE_OK));
    }

    @Test
    public void setLabelEventTypeDataIsCorrect() {
        typeData = orderEventTypeDataFactory.forCallReason(OrderCallReason.CHANGE_LABEL);

        assertDoneEventTypes(EnumSet.of(CHANGED_LABEL));
        assertRejectEventTypes(EnumSet.of(CHANGE_LABEL_REJECTED));
        assertInfoEventTypes(EnumSet.of(NOTIFICATION));
        assertFinishEventTypes(EnumSet.of(CHANGED_LABEL,
                                          CHANGE_LABEL_REJECTED));
        assertAllEventTypes(EnumSet.of(NOTIFICATION,
                                       CHANGED_LABEL,
                                       CHANGE_LABEL_REJECTED));
    }

    @Test
    public void setGTTEventTypeDataIsCorrect() {
        typeData = orderEventTypeDataFactory.forCallReason(OrderCallReason.CHANGE_GTT);

        assertDoneEventTypes(EnumSet.of(CHANGED_GTT));
        assertRejectEventTypes(EnumSet.of(CHANGE_GTT_REJECTED));
        assertInfoEventTypes(EnumSet.of(NOTIFICATION));
        assertFinishEventTypes(EnumSet.of(CHANGED_GTT,
                                          CHANGE_GTT_REJECTED));
        assertAllEventTypes(EnumSet.of(NOTIFICATION,
                                       CHANGED_GTT,
                                       CHANGE_GTT_REJECTED));
    }

    @Test
    public void setAmountEventTypeDataIsCorrect() {
        typeData = orderEventTypeDataFactory.forCallReason(OrderCallReason.CHANGE_AMOUNT);

        assertDoneEventTypes(EnumSet.of(CHANGED_AMOUNT));
        assertRejectEventTypes(EnumSet.of(CHANGE_AMOUNT_REJECTED));
        assertInfoEventTypes(EnumSet.of(NOTIFICATION));
        assertFinishEventTypes(EnumSet.of(CHANGED_AMOUNT,
                                          CHANGE_AMOUNT_REJECTED));
        assertAllEventTypes(EnumSet.of(NOTIFICATION,
                                       CHANGED_AMOUNT,
                                       CHANGE_AMOUNT_REJECTED));
    }

    @Test
    public void setOpenPriceEventTypeDataIsCorrect() {
        typeData = orderEventTypeDataFactory.forCallReason(OrderCallReason.CHANGE_PRICE);

        assertDoneEventTypes(EnumSet.of(CHANGED_PRICE));
        assertRejectEventTypes(EnumSet.of(CHANGE_PRICE_REJECTED));
        assertInfoEventTypes(EnumSet.of(NOTIFICATION));
        assertFinishEventTypes(EnumSet.of(CHANGED_PRICE,
                                          CHANGE_PRICE_REJECTED));
        assertAllEventTypes(EnumSet.of(NOTIFICATION,
                                       CHANGED_PRICE,
                                       CHANGE_PRICE_REJECTED));
    }

    @Test
    public void setSLEventTypeDataIsCorrect() {
        typeData = orderEventTypeDataFactory.forCallReason(OrderCallReason.CHANGE_SL);

        assertDoneEventTypes(EnumSet.of(CHANGED_SL));
        assertRejectEventTypes(EnumSet.of(CHANGE_SL_REJECTED));
        assertInfoEventTypes(EnumSet.of(NOTIFICATION));
        assertFinishEventTypes(EnumSet.of(CHANGED_SL,
                                          CHANGE_SL_REJECTED));
        assertAllEventTypes(EnumSet.of(NOTIFICATION,
                                       CHANGED_SL,
                                       CHANGE_SL_REJECTED));
    }

    @Test
    public void setTPEventTypeDataIsCorrect() {
        typeData = orderEventTypeDataFactory.forCallReason(OrderCallReason.CHANGE_TP);

        assertDoneEventTypes(EnumSet.of(CHANGED_TP));
        assertRejectEventTypes(EnumSet.of(CHANGE_TP_REJECTED));
        assertInfoEventTypes(EnumSet.of(NOTIFICATION));
        assertFinishEventTypes(EnumSet.of(CHANGED_TP,
                                          CHANGE_TP_REJECTED));
        assertAllEventTypes(EnumSet.of(NOTIFICATION,
                                       CHANGED_TP,
                                       CHANGE_TP_REJECTED));
    }
}
