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
        final OrderParams orderParamsEURUSD = OrderParams.forInstrument(Instrument.EURUSD)
                                                         .withOrderCommand(OrderCommand.BUY)
                                                         .withAmount(0.002)
                                                         .withLabel("TestLabel")
                                                         .build();

        // Create orderUtil instance and submit order to server
        orderUtil = jForexUtil.orderUtil();

        final OrderCreateResult submitResult = orderUtil.submit(orderParamsEURUSD);
        final IOrder orderEURUSD = submitResult.orderOpt().get();

        // Change SL and TP
        orderUtil.setSL(orderEURUSD, 1.12345);
        orderUtil.setTP(orderEURUSD, 1.12366);

        // Close order
        orderUtil.close(orderEURUSD);
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
