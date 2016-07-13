package com.jforex.programming.test.fakes;

import java.util.Set;

import com.dukascopy.api.IMessage;
import com.dukascopy.api.IOrder;

public class IMessageForTest implements IMessage {

    private final IMessage.Type type;
    private final Set<Reason> reasons;
    private final IOrder order;
    private String content;

    public IMessageForTest(final IOrder order,
                           final IMessage.Type type,
                           final Set<Reason> reasons) {
        this.type = type;
        this.reasons = reasons;
        this.order = order;
        content = "";
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
}
