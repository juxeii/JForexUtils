package com.jforex.programming.order.command;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Consumer;
import java.util.function.Function;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.OrderStaticUtil;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.process.option.CloseOption;

import rx.Completable;

public class CloseCommand extends CommonCommand {

    private final IOrder order;

    public interface Option extends CloseOption<Option> {

        public CloseCommand build();
    }

    private CloseCommand(final Builder builder) {
        super(builder);
        order = builder.order;
    }

    public final IOrder order() {
        return order;
    }

    public static final Option create(final IOrder orderToClose,
                                      final Function<CloseCommand, Completable> startFunction) {
        return new Builder(checkNotNull(orderToClose),
                           startFunction);
    }

    public static class Builder extends CommonBuilder<Option>
                                implements Option {

        private final IOrder order;

        private Builder(final IOrder order,
                        final Function<CloseCommand, Completable> startFunction) {
            this.order = order;
            this.callable = OrderStaticUtil.runnableToCallable(() -> order.close(), order);
            this.callReason = OrderCallReason.CLOSE;
            this.startFunction = startFunction;
        }

        @Override
        public Option doOnCloseReject(final Consumer<IOrder> rejectAction) {
            eventHandlerForType.put(OrderEventType.CLOSE_REJECTED, checkNotNull(rejectAction));
            return this;
        }

        @Override
        public Option doOnPartialClose(final Consumer<IOrder> partialDoneAction) {
            eventHandlerForType.put(OrderEventType.PARTIAL_CLOSE_OK, checkNotNull(partialDoneAction));
            return this;
        }

        @Override
        public Option doOnClose(final Consumer<IOrder> doneAction) {
            eventHandlerForType.put(OrderEventType.CLOSE_OK, checkNotNull(doneAction));
            return this;
        }

        @Override
        public CloseCommand build() {
            return new CloseCommand(this);
        }
    }
}
