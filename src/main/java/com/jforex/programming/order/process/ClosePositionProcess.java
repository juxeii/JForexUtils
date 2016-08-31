package com.jforex.programming.order.process;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Consumer;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.order.event.OrderEventType;

public class ClosePositionProcess extends OrderProcess {

    private final Instrument instrument;

    public interface CloseOption extends CommonOption<CloseOption> {
        public CloseOption onCloseReject(Consumer<IOrder> closeRejectAction);

        public CloseOption onCloseOK(Consumer<IOrder> closeOKAction);

        public CloseOption onPartialClose(Consumer<IOrder> partialCloseAction);

        public ClosePositionProcess build();
    }

    private ClosePositionProcess(final Builder builder) {
        super(builder);
        instrument = builder.instrument;
    }

    public final Instrument instrument() {
        return instrument;
    }

    public static final CloseOption forInstrument(final Instrument instrument) {
        return new Builder(checkNotNull(instrument));
    }

    private static class Builder extends CommonProcess<Builder> implements CloseOption {

        private final Instrument instrument;

        private Builder(final Instrument instrument) {
            this.instrument = instrument;
        }

        @Override
        public CloseOption onCloseReject(final Consumer<IOrder> closeRejectAction) {
            eventHandlerForType.put(OrderEventType.CLOSE_REJECTED, checkNotNull(closeRejectAction));
            return this;
        }

        @Override
        public CloseOption onCloseOK(final Consumer<IOrder> closeOKAction) {
            eventHandlerForType.put(OrderEventType.CLOSE_OK, checkNotNull(closeOKAction));
            return this;
        }

        @Override
        public CloseOption onPartialClose(final Consumer<IOrder> partialCloseAction) {
            eventHandlerForType.put(OrderEventType.PARTIAL_CLOSE_OK, checkNotNull(partialCloseAction));
            return this;
        }

        @Override
        public ClosePositionProcess build() {
            return new ClosePositionProcess(this);
        }
    }
}
