package com.jforex.programming.order.event;

/**
 * All order related event types.
 */
public enum OrderEventType {

    /** Indicates a notification event. */
    NOTIFICATION,
    /** An order has been submitted successfully. */
    SUBMIT_OK,
    /** An order has been filled partially. */
    PARTIAL_FILL_OK,
    /** An order has been fully filled. */
    FULLY_FILLED,
    /** An order has changed its stop loss price successfully. */
    CHANGED_SL,
    /** An order has changed its take profit price successfully. */
    CHANGED_TP,
    /** A merge order has been created successfully. */
    MERGE_OK,
    /** A merge order has been closed because of a zero amount merge. */
    MERGE_CLOSE_OK,
    /** An order has been closed successfully. */
    CLOSE_OK,
    /** An order has been closed partially. */
    PARTIAL_CLOSE_OK,
    /** An order has been closed because it was in set of to merge orders. */
    CLOSED_BY_MERGE,
    /** An order has been closed because the stop loss price was reached. */
    CLOSED_BY_SL,
    /** An order has been closed because the take profit price was reached. */
    CLOSED_BY_TP,
    /** An order has changed its label successfully. */
    CHANGED_LABEL,
    /** An order has changed its requested amount successfully. */
    CHANGED_AMOUNT,
    /** An order has changed its open price successfully. */
    CHANGED_PRICE,
    /** An order has changed its good till time successfully. */
    CHANGED_GTT,
    /** The creation of an order has been rejected. */
    SUBMIT_REJECTED,
    /** The fill of an order has been rejected. */
    FILL_REJECTED,
    /** The change of an order property has been rejected. */
    CHANGED_REJECTED,
    /** The merge of orders has been rejected. */
    MERGE_REJECTED,
    /** The close of an order has been rejected. */
    CLOSE_REJECTED,
    /** The change of the stop loss price of an order has been rejected. */
    CHANGE_SL_REJECTED,
    /** The change of the take profit price of an order has been rejected. */
    CHANGE_TP_REJECTED,
    /** The change of the good till time of an order has been rejected. */
    CHANGE_GTT_REJECTED,
    /** The change of the label of an order has been rejected. */
    CHANGE_LABEL_REJECTED,
    /** The change of the requested amount of an order has been rejected. */
    CHANGE_AMOUNT_REJECTED,
    /** The change of the open price of an order has been rejected. */
    CHANGE_PRICE_REJECTED,
}
