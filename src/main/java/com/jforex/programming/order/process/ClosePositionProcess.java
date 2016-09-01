package com.jforex.programming.order.process;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.process.option.ClosePositionOption;

public class ClosePositionProcess extends CommonProcess {

    private final Instrument instrument;

    private ClosePositionProcess(final Builder builder) {
        super(builder);
        instrument = builder.instrument;
    }

    public final Instrument instrument() {
        return instrument;
    }

    public static final ClosePositionOption forInstrument(final Instrument instrument) {
        return new Builder(checkNotNull(instrument));
    }

    private static class Builder extends CommonBuilder<ClosePositionOption>
                                 implements ClosePositionOption {

        private final Instrument instrument;

        private Builder(final Instrument instrument) {
            this.instrument = instrument;
        }

        public ClosePositionOption onCloseReject(final Consumer<IOrder> closeRejectAction) {
            eventHandlerForType.put(OrderEventType.CLOSE_REJECTED, checkNotNull(closeRejectAction));
            return this;
        }

        public ClosePositionOption onClose(final Consumer<IOrder> closedAction) {
            eventHandlerForType.put(OrderEventType.CLOSE_OK, checkNotNull(closedAction));
            return this;
        }

        public ClosePositionOption onPartialClose(final Consumer<IOrder> partialClosedAction) {
            eventHandlerForType.put(OrderEventType.PARTIAL_CLOSE_OK, checkNotNull(partialClosedAction));
            return this;
        }

        @Override
        public ClosePositionProcess build() {
            return new ClosePositionProcess(this);
        }
    }
}
