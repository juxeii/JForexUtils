package com.jforex.programming.order.task.params;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Optional;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.task.BasicTask;
import com.jforex.programming.strategy.StrategyUtil;

public class CloseParams extends BasicTaskParams {

    private final IOrder order;
    private final double partialCloseAmount;
    private final Optional<Double> maybePrice;
    private final double slippage;

    public interface CloseOption {

        public CloseOption doOnClose(OrderEventConsumer closeConsumer);

        public CloseOption doOnPartialClose(OrderEventConsumer partialCloseConsumer);

        public CloseOption doOnReject(OrderEventConsumer rejectConsumer);

        public CloseOption closePartial(double partialCloseAmount);

        public SlippageOption atPrice(double price);

        public CloseParams build();
    }

    public interface SlippageOption {

        public SlippageOption withSlippage(double slippage);

        public CloseParams build();
    }

    private static final double defaultCloseSlippage = StrategyUtil.platformSettings.defaultCloseSlippage();
    private static final double noCloseSlippageValue = Double.NaN;

    private CloseParams(final Builder builder) {
        super(builder);

        order = builder.order;
        partialCloseAmount = builder.partialCloseAmount;
        maybePrice = builder.maybePrice;
        slippage = evalSlippage(builder.slippage);
    }

    private double evalSlippage(final double builderSlippage) {
        if (builderSlippage == 0.0)
            return noCloseSlippageValue;
        return builderSlippage < 0
                ? defaultCloseSlippage
                : builderSlippage;
    }

    public void subscribe(final BasicTask basicTask) {
        subscribe(basicTask.close(this));
    }

    public IOrder order() {
        return order;
    }

    public double partialCloseAmount() {
        return partialCloseAmount;
    }

    public Optional<Double> maybePrice() {
        return maybePrice;
    }

    public double slippage() {
        return slippage;
    }

    public static CloseOption closeWith(final IOrder order) {
        checkNotNull(order);

        return new Builder(order);
    }

    private static class Builder extends ParamsBuilderBase<Builder>
                                 implements
                                 CloseOption,
                                 SlippageOption {

        private final IOrder order;
        private double partialCloseAmount = 0.0;
        private Optional<Double> maybePrice = Optional.empty();
        private double slippage;

        public Builder(final IOrder order) {
            this.order = order;
        }

        @Override
        public CloseOption closePartial(final double partialCloseAmount) {
            this.partialCloseAmount = partialCloseAmount;
            return this;
        }

        @Override
        public SlippageOption atPrice(final double price) {
            maybePrice = Optional.of(price);
            return this;
        }

        @Override
        public SlippageOption withSlippage(final double slippage) {
            this.slippage = slippage;
            return this;
        }

        @Override
        public Builder doOnClose(final OrderEventConsumer closeConsumer) {
            return setEventConsumer(OrderEventType.CLOSE_OK, closeConsumer);
        }

        @Override
        public Builder doOnPartialClose(final OrderEventConsumer partialCloseConsumer) {
            return setEventConsumer(OrderEventType.PARTIAL_CLOSE_OK, partialCloseConsumer);
        }

        @Override
        public Builder doOnReject(final OrderEventConsumer rejectConsumer) {
            return setEventConsumer(OrderEventType.CLOSE_REJECTED, rejectConsumer);
        }

        @Override
        public CloseParams build() {
            return new CloseParams(this);
        }
    }
}
