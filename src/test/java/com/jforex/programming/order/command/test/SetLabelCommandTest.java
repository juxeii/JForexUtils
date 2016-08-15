package com.jforex.programming.order.command.test;

import static com.jforex.programming.order.event.OrderEventType.CHANGED_LABEL;
import static com.jforex.programming.order.event.OrderEventType.CHANGE_LABEL_REJECTED;

import org.junit.Before;
import org.junit.Test;

import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.command.SetLabelCommand;

public class SetLabelCommandTest extends CommonCommandForTest {

    private final String newLabel = "newLabel";

    @Before
    public void setUp() {
        command = new SetLabelCommand(orderForTest, newLabel);
    }

    @Test
    public void orderEventTestAreCorrect() {
        assertIsDoneEvent(CHANGED_LABEL);

        assertIsRejectEvent(CHANGE_LABEL_REJECTED);

        assertEventIsForCommand(CHANGED_LABEL,
                                CHANGE_LABEL_REJECTED);
    }

    @Test
    public void orderCallReasonIsCorrect() {
        assertCallReason(OrderCallReason.CHANGE_LABEL);
    }

    @Test
    public void callableIsCorrect() throws Exception {
        assertCallableOrder();

        verify(orderForTest).setLabel(newLabel);
    }

    @Test
    public void filterIsFalseWhenNewLabelAlreadySet() {
        orderUtilForTest.setLabel(orderForTest, newLabel);

        assertFilterNotSet();
    }

    @Test
    public void filterIsTrueWhenNewLabelDiffers() {
        orderUtilForTest.setLabel(orderForTest, "Other" + newLabel);

        assertFilterIsSet();
    }
}
