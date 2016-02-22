package com.jforex.programming.article;

import com.dukascopy.api.IAccount;
import com.dukascopy.api.IBar;
import com.dukascopy.api.IContext;
import com.dukascopy.api.IEngine.OrderCommand;
import com.dukascopy.api.IMessage;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.IStrategy;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.Library;
import com.dukascopy.api.Period;
import com.dukascopy.api.RequiresFullAccess;
import com.jforex.programming.misc.JForexUtil;
import com.jforex.programming.order.OrderCreateResult;
import com.jforex.programming.order.OrderParams;
import com.jforex.programming.order.OrderUtil;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventConsumer;
import com.jforex.programming.order.event.OrderEventType;

/* Remove both annotations if you develop a standalone app */
@RequiresFullAccess
/* Change the path to the jar to your path! */
@Library("D:/programs/JForex/libs/JForexUtils-0.9.35.jar")
public class RunningExamplePart3 implements IStrategy {

    private JForexUtil jForexUtil;
    private OrderUtil orderUtil;

    @Override
    public void onStart(final IContext context) throws JFException {
        jForexUtil = new JForexUtil(context);

        // Prepare order parameters for EUR/USD with fluent interface
        final OrderParams orderParamsEURUSD =
                OrderParams.forInstrument(Instrument.EURUSD)
                           .withOrderCommand(OrderCommand.BUY)
                           .withAmount(0.002)
                           .withLabel("TestLabel1")
                           .build();

        final OrderParams orderParamsEURUSDFull =
                OrderParams.forInstrument(Instrument.EURUSD)
                           .withOrderCommand(OrderCommand.BUY)
                           .withAmount(0.002)
                           .withLabel("TestLabel2")
                           .price(0)
                           .goodTillTime(0L)
                           .slippage(2.0)
                           .stopLossPrice(0)
                           .takeProfitPrice(0)
                           .comment("Test Comment")
                           .build();

        final OrderParams adaptedEURUSDParams =
                orderParamsEURUSDFull.clone()
                                     .withAmount(0.003)
                                     .build();

        // Create orderUtil instance and submit order to server
        orderUtil = jForexUtil.orderUtil();
        orderUtil.submit(orderParamsEURUSD);

        // Submit order to server with return result
        final OrderCreateResult result = orderUtil.submit(orderParamsEURUSDFull);

        if (result.exceptionOpt().isPresent()) {
            final Exception e = result.exceptionOpt().get();
            System.out.println("Ouch! An excpetion occured: " + e.getMessage());
            // ... handle the exception somehow
        } else {
            // No exception, so the new order was created, but not yet accepted
            // by the server!
            final IOrder order = result.orderOpt().get();
        }

        // Shorter alternative
//        final OrderCreateResult result = orderUtil.submit(orderParamsEURUSDFull);
//        final IOrder order = result.orderOpt().orElse(null);

        // Close order
        final OrderCreateResult result =
                orderUtil.submit(orderParamsEURUSDFull, new MyEventConsumer());
        final IOrder order = result.orderOpt().orElse(null);
    }

    private class MyEventConsumer implements OrderEventConsumer {
        @Override
        public void onOrderEvent(final OrderEvent orderEvent) {
            final IOrder order = orderEvent.order();
            final OrderEventType type = orderEvent.type();
            System.out.println("Received order event for order "
                    + order.getLabel() + " with type " + type);
            // do your handling here
        }
    }

    @Override
    public void onStop() throws JFException {
        jForexUtil.onStop();
    }

    @Override
    public void onMessage(final IMessage message) throws JFException {
        jForexUtil.onMessage(message);
    }

    @Override
    public void onTick(final Instrument instrument,
                       final ITick tick) throws JFException {
        jForexUtil.onTick(instrument, tick);
    }

    @Override
    public void onBar(final Instrument instrument,
                      final Period period,
                      final IBar askBar,
                      final IBar bidBar) throws JFException {
        jForexUtil.onBar(instrument, period, askBar, bidBar);
    }

    @Override
    public void onAccount(final IAccount account) throws JFException {

    }
}
