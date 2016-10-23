package com.jforex.programming.order.command;

import static com.google.common.base.Preconditions.checkNotNull;

import com.dukascopy.api.OfferSide;

public class SetSLCommand {

    private final double newSL;
    private final OfferSide offerSide;
    private final double trailingStep;

    public interface SLOption {

        public SLOption withOfferSide(OfferSide offerSide);

        public SLOption withTrailingStep(double trailingStep);

        public SetSLCommand build();
    }

    private SetSLCommand(final Builder builder) {
        newSL = builder.newSL;
        offerSide = builder.offerSide;
        trailingStep = builder.trailingStep;
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

    public static SLOption newBuilder(final double newSL) {
        return new Builder(newSL);
    }

    public static class Builder implements SLOption {

        private final double newSL;
        private OfferSide offerSide;
        private double trailingStep = -1;

        public Builder(final double newSL) {
            this.newSL = newSL;
        }

        @Override
        public SLOption withOfferSide(final OfferSide offerSide) {
            checkNotNull(offerSide);

            this.offerSide = offerSide;
            return this;
        }

        @Override
        public SLOption withTrailingStep(final double trailingStep) {
            this.trailingStep = trailingStep;
            return this;
        }

        @Override
        public SetSLCommand build() {
            return new SetSLCommand(this);
        }
    }
}
