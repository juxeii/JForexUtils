package com.jforex.programming.order.call;

/**
 * These values represent the reasons for an order related server call.
 */
public enum OrderCallReason {

    /** A submit request for creating an order. */
    SUBMIT,
    /** A submit request for creating a conditional order. */
    SUBMIT_CONDITIONAL,
    /** A submit request for creating a merge order. */
    MERGE,
    /** A request for closing an order. */
    CLOSE,
    /** A request for partial closing an order. */
    PARTIAL_CLOSE,
    /** A request for changing the stop loss price of an order. */
    CHANGE_SL,
    /** A request for changing the take profit price of an order. */
    CHANGE_TP,
    /** A request for changing the good till time of an order. */
    CHANGE_GTT,
    /** A request for changing the label of a conditional order. */
    CHANGE_LABEL,
    /** A request for changing the requested amounf of a conditional order. */
    CHANGE_AMOUNT,
    /** A request for changing the open price of a conditional order. */
    CHANGE_PRICE,
}
