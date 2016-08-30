package com.jforex.programming.order.builder;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;
import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.jforex.programming.order.OrderParams;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;

public class SubmitBuilder extends OrderBuilder {

    private final OrderParams orderParams;
    private final Map<OrderEventType, Consumer<IOrder>> eventHandlerForType;

    public interface SubmitOption extends CommonOption<SubmitOption> {
        public SubmitOption onSubmitReject(Consumer<IOrder> submitRejectAction);

        public SubmitOption onFillReject(Consumer<IOrder> fillRejectAction);

        public SubmitOption onSubmitOK(Consumer<IOrder> submitOKAction);

        public SubmitOption onPartialFill(Consumer<IOrder> partialFillAction);

        public SubmitOption onFill(Consumer<IOrder> fillAction);

        public SubmitBuilder build();
    }

    private SubmitBuilder(final Builder builder) {
        super(builder);
        orderParams = builder.orderParams;
        eventHandlerForType =
                Maps.immutableEnumMap(ImmutableMap.<OrderEventType, Consumer<IOrder>> builder()
                    .put(OrderEventType.FULLY_FILLED, builder.fillAction)
                    .put(OrderEventType.PARTIAL_FILL_OK, builder.partialFillAction)
                    .put(OrderEventType.SUBMIT_CONDITIONAL_OK, builder.submitOKAction)
                    .put(OrderEventType.SUBMIT_OK, builder.submitOKAction)
                    .put(OrderEventType.SUBMIT_REJECTED, builder.submitRejectAction)
                    .put(OrderEventType.FILL_REJECTED, builder.fillRejectAction)
                    .build());
    }

    public final OrderParams orderParams() {
        return orderParams;
    }

    public static final SubmitOption forOrderParams(final OrderParams orderParams) {
        return new Builder(checkNotNull(orderParams));
    }

    private static class Builder extends CommonBuilder<Builder> implements SubmitOption {

        private final OrderParams orderParams;
        private Consumer<IOrder> submitRejectAction = o -> {};
        private Consumer<IOrder> fillRejectAction = o -> {};
        private Consumer<IOrder> submitOKAction = o -> {};
        private Consumer<IOrder> partialFillAction = o -> {};
        private Consumer<IOrder> fillAction = o -> {};

        private Builder(final OrderParams orderParams) {
            this.orderParams = orderParams;
        }

        @Override
        public SubmitOption onSubmitReject(final Consumer<IOrder> submitRejectAction) {
            this.submitRejectAction = checkNotNull(submitRejectAction);
            return this;
        }

        @Override
        public SubmitOption onFillReject(final Consumer<IOrder> fillRejectAction) {
            this.fillRejectAction = checkNotNull(fillRejectAction);
            return this;
        }

        @Override
        public SubmitOption onSubmitOK(final Consumer<IOrder> submitOKAction) {
            this.submitOKAction = checkNotNull(submitOKAction);
            return this;
        }

        @Override
        public SubmitOption onPartialFill(final Consumer<IOrder> partialFillAction) {
            this.partialFillAction = checkNotNull(partialFillAction);
            return this;
        }

        @Override
        public SubmitOption onFill(final Consumer<IOrder> fillAction) {
            this.fillAction = checkNotNull(fillAction);
            return this;
        }

        @Override
        public SubmitBuilder build() {
            return new SubmitBuilder(this);
        }
    }

    @Override
    protected void callEventHandler(final OrderEvent orderEvent) {
        eventHandlerForType
            .get(orderEvent.type())
            .accept(orderEvent.order());
    }
}
