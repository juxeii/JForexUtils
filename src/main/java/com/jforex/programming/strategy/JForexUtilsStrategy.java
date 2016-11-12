package com.jforex.programming.strategy;

import com.dukascopy.api.IAccount;
import com.dukascopy.api.IBar;
import com.dukascopy.api.IContext;
import com.dukascopy.api.IMessage;
import com.dukascopy.api.IStrategy;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.Period;
import com.jforex.programming.math.CalculationUtil;
import com.jforex.programming.order.OrderUtil;
import com.jforex.programming.position.PositionUtil;

public abstract class JForexUtilsStrategy implements IStrategy {

    protected StrategyUtil strategyUtil;
    protected OrderUtil orderUtil;
    protected PositionUtil positionUtil;
    protected CalculationUtil calculationUtil;

    protected abstract void onJFStart(final IContext context) throws JFException;

    @Override
    public void onStart(final IContext context) throws JFException {
        strategyUtil = new StrategyUtil(context);
        orderUtil = strategyUtil.orderUtil();
        positionUtil = strategyUtil.positionUtil();
        calculationUtil = strategyUtil.calculationUtil();

        onJFStart(context);
    }

    protected abstract void onJFTick(final Instrument instrument,
                                     final ITick tick) throws JFException;

    @Override
    public void onTick(final Instrument instrument,
                       final ITick tick) throws JFException {
        strategyUtil.onTick(instrument, tick);

        onJFTick(instrument, tick);
    }

    protected abstract void onJFBar(final Instrument instrument,
                                    final Period period,
                                    final IBar askBar,
                                    final IBar bidBar) throws JFException;

    @Override
    public void onBar(final Instrument instrument,
                      final Period period,
                      final IBar askBar,
                      final IBar bidBar) throws JFException {
        strategyUtil.onBar(instrument,
                           period,
                           askBar,
                           bidBar);

        onJFBar(instrument,
                period,
                askBar,
                bidBar);
    }

    protected abstract void onJFMessage(final IMessage message) throws JFException;

    @Override
    public void onMessage(final IMessage message) throws JFException {
        strategyUtil.onMessage(message);

        onJFMessage(message);
    }

    protected abstract void onJFStop() throws JFException;

    @Override
    public void onStop() throws JFException {
        strategyUtil.onStop();

        onJFStop();
    }

    protected abstract void onJFAccount(final IAccount account) throws JFException;

    @Override
    public void onAccount(final IAccount account) throws JFException {
        onJFAccount(account);
    }
}
