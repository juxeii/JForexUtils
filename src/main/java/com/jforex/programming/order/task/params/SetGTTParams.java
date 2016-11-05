package com.jforex.programming.order.task.params;

import static com.google.common.base.Preconditions.checkNotNull;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.task.BasicTask;

public class SetGTTParams extends BasicTaskParams {

    private final IOrder order;
    private final long newGTT;

    private SetGTTParams(final Builder builder) {
        super(builder);

        this.order = builder.order;
        this.newGTT = builder.newGTT;
    }

    public void subscribe(final BasicTask basicTask) {
        subscribe(basicTask.setGoodTillTime(order, newGTT));
    }

    public static Builder setOpenPriceWith(final IOrder order,
                                           final long newGTT) {
        checkNotNull(order);

        return new Builder(order, newGTT);
    }

    public static class Builder extends ParamsBuilderBase<Builder> {

        private final IOrder order;
        private final long newGTT;

        public Builder(final IOrder order,
                       final long newGTT) {
            this.order = order;
            this.newGTT = newGTT;
        }

        public Builder doOnChangedGTT(final OrderEventConsumer changedGTTConsumer) {
            return setEventConsumer(OrderEventType.CHANGED_GTT, changedGTTConsumer);
        }

        public Builder doOnReject(final OrderEventConsumer changeRejectConsumer) {
            return setEventConsumer(OrderEventType.CHANGE_GTT_REJECTED, changeRejectConsumer);
        }

        public SetGTTParams build() {
            return new SetGTTParams(this);
        }
    }
}
