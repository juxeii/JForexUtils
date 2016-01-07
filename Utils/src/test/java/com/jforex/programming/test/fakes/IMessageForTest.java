package com.jforex.programming.test.fakes;

import static com.jforex.programming.misc.JForexUtil.pfs;

import java.util.Collections;
import java.util.Set;

import com.dukascopy.api.IMessage;
import com.dukascopy.api.IOrder;

public class IMessageForTest implements IMessage {

    private final IMessage.Type type;
    private final Set<Reason> reasons;
    private IOrder order;
    private String content;

    public IMessageForTest(final IMessage.Type type,
                           final String content) {
        this.type = type;
        this.content = content;
        reasons = Collections.emptySet();
        order = null;
    }

    public IMessageForTest(final IOrder order,
                           final IMessage.Type type,
                           final Set<Reason> reasons) {
        this.type = type;
        this.reasons = reasons;
        this.order = order;
        content = "";
    }

    public void setOrder(final IOrder order) {
        this.order = order;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public Set<Reason> getReasons() {
        return reasons;
    }

    @Override
    public String getContent() {
        return content;
    }

    public void setContent(final String content) {
        this.content = content;
    }

    @Override
    public IOrder getOrder() {
        return order;
    }

    @Override
    public long getCreationTime() {
        return 0;
    }

    public static IMessage connectedMessage() {
        return new IMessageForTest(IMessage.Type.CONNECTION_STATUS, pfs.CONNECTED_STRING());
    }

    public static IMessage disconnectedMessage() {
        return new IMessageForTest(IMessage.Type.CONNECTION_STATUS, pfs.DISCONNECTED_STRING());
    }

    public static IMessage calendarMessage() {
        return new IMessageForTest(IMessage.Type.CALENDAR, "Calendar");
    }
}
