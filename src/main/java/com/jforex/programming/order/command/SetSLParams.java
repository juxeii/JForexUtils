package com.jforex.programming.order.command;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Optional;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.OfferSide;

public class SetSLParams {

    private final IOrder order;
    private final double newSL;
    private final OfferSide offerSide;
    private final double trailingStep;

    public interface SLOption {

        public SLOption withOfferSide(OfferSide offerSide);

        public SLOption withTrailingStep(double trailingStep);

        public SetSLParams build();
    }

    private SetSLParams(final Builder builder) {
        order = builder.order;
        newSL = builder.newSL;
        offerSide = evalOfferSide(builder.maybeOfferSide);
        trailingStep = builder.trailingStep;
    }

    private OfferSide evalOfferSide(final Optional<OfferSide> maybeOfferSide) {
        return maybeOfferSide.orElse(order.isLong()
                ? OfferSide.BID
                : OfferSide.ASK);
    }

    public final IOrder order() {
        return order;
    }

    public double newSL() {
        return newSL;
    }

    public OfferSide offerSide() {
        return offerSide;
    }

    public double trailingStep() {
        return trailingStep;
    }

    public static SLOption newBuilder(final IOrder order,
                                      final double newSL) {
        checkNotNull(order);

        return new Builder(order, newSL);
    }

    public static class Builder implements SLOption {

        private final IOrder order;
        private final double newSL;
        private Optional<OfferSide> maybeOfferSide = Optional.empty();
        private double trailingStep = -1;

        public Builder(final IOrder order,
                       final double newSL) {
            this.order = order;
            this.newSL = newSL;
        }

        @Override
        public SLOption withOfferSide(final OfferSide offerSide) {
            checkNotNull(offerSide);

            maybeOfferSide = Optional.of(offerSide);
            return this;
        }

        @Override
        public SLOption withTrailingStep(final double trailingStep) {
            this.trailingStep = trailingStep;
            return this;
        }

        @Override
        public SetSLParams build() {
            return new SetSLParams(this);
        }
    }
}
