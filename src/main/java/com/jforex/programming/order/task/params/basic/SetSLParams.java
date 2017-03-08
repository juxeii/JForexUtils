package com.jforex.programming.order.task.params.basic;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Optional;
import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.OfferSide;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.task.params.SetSLTPMode;
import com.jforex.programming.order.task.params.TaskParamsBase;
import com.jforex.programming.order.task.params.TaskParamsType;
import com.jforex.programming.order.task.params.TaskParamsWithType;

public class SetSLParams extends TaskParamsWithType {

    private final IOrder order;
    private final double priceOrPips;
    private final SetSLTPMode setSLTPMode;
    private final OfferSide offerSide;
    private final double trailingStep;

    private SetSLParams(final Builder builder) {
        super(builder);

        order = builder.order;
        priceOrPips = builder.priceOrPips;
        setSLTPMode = builder.setSLTPMode;
        offerSide = evalOfferSide(builder.maybeOfferSide);
        trailingStep = evalTrailingStep(builder.maybeTrailingStep);
    }

    private OfferSide evalOfferSide(final Optional<OfferSide> maybeOfferSide) {
        return maybeOfferSide.orElse(order.isLong()
                ? OfferSide.BID
                : OfferSide.ASK);
    }

    private double evalTrailingStep(final Optional<Double> maybeTrailingStep) {
        return maybeTrailingStep.orElse(0.0);
    }

    public final IOrder order() {
        return order;
    }

    public double priceOrPips() {
        return priceOrPips;
    }

    public SetSLTPMode setSLTPMode() {
        return setSLTPMode;
    }

    public OfferSide offerSide() {
        return offerSide;
    }

    public double trailingStep() {
        return trailingStep;
    }

    @Override
    public TaskParamsType type() {
        return TaskParamsType.SETSL;
    }

    public static Builder setSLAtPrice(final IOrder order,
                                       final double newSL) {
        checkNotNull(order);

        return new Builder(order,
                           newSL,
                           SetSLTPMode.PRICE);
    }

    public static Builder setSLWithPips(final IOrder order,
                                        final double pips) {
        checkNotNull(order);

        return new Builder(order,
                           pips,
                           SetSLTPMode.PIPS);
    }

    public static class Builder extends TaskParamsBase.Builder<Builder> {

        private final IOrder order;
        private final double priceOrPips;
        private final SetSLTPMode setSLTPMode;
        private Optional<OfferSide> maybeOfferSide = Optional.empty();
        private Optional<Double> maybeTrailingStep = Optional.empty();

        public Builder(final IOrder order,
                       final double priceOrPips,
                       final SetSLTPMode setSLTPMode) {
            this.order = order;
            this.priceOrPips = priceOrPips;
            this.setSLTPMode = setSLTPMode;
        }

        public Builder doOnChangedSL(final Consumer<OrderEvent> changedSLConsumer) {
            setEventConsumer(OrderEventType.CHANGED_SL, changedSLConsumer);
            return this;
        }

        public Builder doOnReject(final Consumer<OrderEvent> changeRejectConsumer) {
            setEventConsumer(OrderEventType.CHANGE_SL_REJECTED, changeRejectConsumer);
            return this;
        }

        public Builder withOfferSide(final OfferSide offerSide) {
            checkNotNull(offerSide);

            maybeOfferSide = Optional.of(offerSide);
            return this;
        }

        public Builder withTrailingStep(final double trailingStep) {
            maybeTrailingStep = Optional.of(trailingStep);
            return this;
        }

        @Override
        public SetSLParams build() {
            return new SetSLParams(this);
        }
    }
}
