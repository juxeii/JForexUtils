package com.jforex.programming.misc;

import com.dukascopy.api.IAccount;
import com.dukascopy.api.IBar;
import com.dukascopy.api.IContext;
import com.dukascopy.api.IMessage;
import com.dukascopy.api.IStrategy;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.Period;
import com.jforex.programming.strategy.StrategyUtil;

public class JFSkeletonStrategy implements IStrategy {

    private StrategyUtil strategyUtil;

    @Override
    public void onStart(final IContext context) throws JFException {
        strategyUtil = new StrategyUtil(context);
    }

    @Override
    public void onTick(final Instrument instrument,
                       final ITick tick) throws JFException {
        strategyUtil.onTick(instrument, tick);
    }

    @Override
    public void onBar(final Instrument instrument,
                      final Period period,
                      final IBar askBar,
                      final IBar bidBar) throws JFException {
        strategyUtil.onBar(instrument,
                           period,
                           askBar,
                           bidBar);
    }

    @Override
    public void onMessage(final IMessage message) throws JFException {
        strategyUtil.onMessage(message);
    }

    @Override
    public void onStop() throws JFException {
        strategyUtil.onStop();
    }

    @Override
    public void onAccount(final IAccount account) throws JFException {
    }
}
