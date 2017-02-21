package com.jforex.programming.misc;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IEngine;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.math.MathUtil;
import com.jforex.programming.order.OrderStaticUtil;
import com.jforex.programming.strategy.StrategyUtil;

import io.reactivex.Observable;

public class Exposure {

    private final IEngine engine;
    private final double maxExposure;

    private final static Logger logger = LogManager.getLogger(Exposure.class);

    public Exposure(final IEngine engine) {
        this.engine = engine;

        maxExposure = StrategyUtil.platformSettings.maxExposure();
    }

    public double get(final Instrument instrument) {
        final double exposure = getFromOrders(instrument);
        return exposure;
    }

    public boolean wouldExceed(final Instrument instrument,
                               final double signedAmount) {
        final double signedExposure = get(instrument);
        final double resultingExposure = Math.abs(signedExposure + signedAmount);
        logger.debug("signedExposure " + signedExposure
                + " maxExposure " + maxExposure
                + " resultingExposure " + resultingExposure);

        return resultingExposure > maxExposure;
    }

    private double getFromOrders(final Instrument instrument) {
        final List<IOrder> orders = Observable
            .fromCallable(() -> engine.getOrders(instrument))
            .onErrorResumeNext(err -> {
                logger.error("Error while engine.getOrders! " + err.getMessage());
                return Observable.just(new ArrayList<>());
            })
            .blockingFirst();

        final double rawAmount = orders
            .stream()
            .mapToDouble(order -> OrderStaticUtil.signedAmount(order))
            .sum();

        return MathUtil.roundAmount(rawAmount);
    }
}
