package com.jforex.programming.order.task.params;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Optional;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.OfferSide;
import com.jforex.programming.order.event.OrderEventType;

public class SetSLParams extends BasicTaskParamsBase {

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
        return maybeTrailingStep.orElse(-1.0);
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

    public static class Builder extends ParamsBuilderBase<Builder> {

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

        public Builder doOnChangedTP(final OrderEventConsumer changedSLConsumer) {
            return setEventConsumer(OrderEventType.CHANGED_SL, changedSLConsumer);
        }

        public Builder doOnReject(final OrderEventConsumer changeRejectConsumer) {
            return setEventConsumer(OrderEventType.CHANGE_SL_REJECTED, changeRejectConsumer);
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

        public SetSLParams build() {
            return new SetSLParams(this);
        }
    }
}
