package com.jforex.programming.strategy;

import static com.google.common.base.Preconditions.checkNotNull;

import org.aeonbits.owner.ConfigFactory;
import org.apache.commons.lang3.StringUtils;

import com.dukascopy.api.IAccount;
import com.dukascopy.api.IBar;
import com.dukascopy.api.IContext;
import com.dukascopy.api.IEngine;
import com.dukascopy.api.IHistory;
import com.dukascopy.api.IMessage;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.Period;
import com.jforex.programming.instrument.InstrumentUtil;
import com.jforex.programming.math.CalculationUtil;
import com.jforex.programming.misc.HistoryUtil;
import com.jforex.programming.misc.StrategyThreadRunner;
import com.jforex.programming.order.OrderUtil;
import com.jforex.programming.position.PositionUtil;
import com.jforex.programming.quote.BarQuoteProvider;
import com.jforex.programming.quote.TickQuoteProvider;
import com.jforex.programming.rx.JFHotPublisher;
import com.jforex.programming.settings.PlatformSettings;
import com.jforex.programming.settings.UserSettings;

import io.reactivex.Completable;

public class StrategyUtil {

    private final ContextUtil contextUtil;
    private final QuoteUtil quoteUtil;
    private final OrderInitUtil orderInitUtil;
    private final CalculationUtil calculationUtil;
    private final JFHotPublisher<IMessage> messagePublisher = new JFHotPublisher<>();

    public static final PlatformSettings platformSettings = ConfigFactory.create(PlatformSettings.class);
    public static final UserSettings userSettings = ConfigFactory.create(UserSettings.class);

    public StrategyUtil(final IContext context) {
        checkNotNull(context);

        contextUtil = new ContextUtil(context);
        quoteUtil = new QuoteUtil(contextUtil, userSettings.enableWeekendQuoteFilter());
        calculationUtil = new CalculationUtil(tickQuoteProvider());
        orderInitUtil = new OrderInitUtil(contextUtil,
                                          messagePublisher.observable(),
                                          calculationUtil);
    }

    public IContext context() {
        return contextUtil.context();
    }

    public IEngine engine() {
        return contextUtil.engine();
    }

    public IAccount account() {
        return contextUtil.account();
    }

    public IHistory history() {
        return contextUtil.history();
    }

    public HistoryUtil historyUtil() {
        return contextUtil.historyUtil();
    }

    public TickQuoteProvider tickQuoteProvider() {
        return quoteUtil.tickQuoteProvider();
    }

    public BarQuoteProvider barQuoteProvider() {
        return quoteUtil.barQuoteProvider();
    }

    public CalculationUtil calculationUtil() {
        return calculationUtil;
    }

    public OrderUtil orderUtil() {
        return orderInitUtil.orderUtil();
    }

    public PositionUtil positionUtil() {
        return orderInitUtil.positionUtil();
    }

    public StrategyThreadRunner strategyThreadRunner() {
        return orderInitUtil.strategyThreadRunner();
    }

    public InstrumentUtil instrumentUtil(final Instrument instrument) {
        checkNotNull(instrument);

        return new InstrumentUtil(instrument,
                                  tickQuoteProvider(),
                                  barQuoteProvider(),
                                  calculationUtil);
    }

    public void onMessage(final IMessage message) {
        checkNotNull(message);

        messagePublisher.onNext(message);
    }

    public void onTick(final Instrument instrument,
                       final ITick tick) {
        checkNotNull(instrument);
        checkNotNull(tick);

        quoteUtil.onTick(instrument, tick);
    }

    public void onBar(final Instrument instrument,
                      final Period period,
                      final IBar askBar,
                      final IBar bidBar) {
        checkNotNull(instrument);
        checkNotNull(period);
        checkNotNull(askBar);
        checkNotNull(bidBar);

        quoteUtil.onBar(instrument,
                        period,
                        askBar,
                        bidBar);
    }

    public void onStop() {
        quoteUtil.onStop();
        orderInitUtil.onStop();
        messagePublisher.unsubscribe();
    }

    public Completable importOrders() {
        return orderInitUtil.importOrders();
    }

    public boolean isMarketClosed() {
        return contextUtil.isMarketNowClosed();
    }

    public boolean isMarketClosed(final long time) {
        return contextUtil.isMarketClosedAtTime(time);
    }

    public static final boolean isStrategyThread() {
        return StringUtils.startsWith(threadName(), platformSettings.strategyThreadPrefix());
    }

    public static final String threadName() {
        return Thread
            .currentThread()
            .getName();
    }
}
