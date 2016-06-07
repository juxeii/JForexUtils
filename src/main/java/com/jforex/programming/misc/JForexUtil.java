package com.jforex.programming.misc;

import org.aeonbits.owner.ConfigFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IAccount;
import com.dukascopy.api.IBar;
import com.dukascopy.api.IContext;
import com.dukascopy.api.IDataService;
import com.dukascopy.api.IEngine;
import com.dukascopy.api.IHistory;
import com.dukascopy.api.IMessage;
import com.dukascopy.api.ITick;
import com.dukascopy.api.ITimeDomain;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.Period;
import com.jforex.programming.instrument.InstrumentUtil;
import com.jforex.programming.mm.RiskPercentMM;
import com.jforex.programming.order.OrderChangeUtil;
import com.jforex.programming.order.OrderCreateUtil;
import com.jforex.programming.order.OrderMessageData;
import com.jforex.programming.order.OrderPositionUtil;
import com.jforex.programming.order.OrderUtil;
import com.jforex.programming.order.OrderUtilHandler;
import com.jforex.programming.order.call.OrderCallExecutor;
import com.jforex.programming.order.event.OrderEventGateway;
import com.jforex.programming.position.PositionFactory;
import com.jforex.programming.position.PositionMultiTask;
import com.jforex.programming.position.PositionSingleTask;
import com.jforex.programming.quote.BarQuote;
import com.jforex.programming.quote.BarQuoteProvider;
import com.jforex.programming.quote.TickQuote;
import com.jforex.programming.quote.TickQuoteProvider;
import com.jforex.programming.settings.UserSettings;

import rx.Subscription;

public class JForexUtil implements IMessageConsumer {

    private final IContext context;
    private IEngine engine;
    private IAccount account;
    private IHistory history;
    private IDataService dataService;

    private ConcurrentUtil concurrentUtil;

    private TickQuoteProvider tickQuoteProvider;
    private BarQuoteProvider barQuoteProvider;

    private PositionFactory positionFactory;
    private OrderEventGateway orderEventGateway;
    private OrderCallExecutor orderCallExecutor;
    private OrderUtilHandler orderUtilHandler;
    private OrderCreateUtil orderCreateUtil;
    private OrderChangeUtil orderChangeUtil;
    private OrderUtil orderUtil;

    private PositionSingleTask positionSingleTask;
    private PositionMultiTask positionMultiTask;
    private OrderPositionUtil orderPositionUtil;

    private final CalculationUtil calculationUtil;
    private final RiskPercentMM riskPercentMM;

    private final JFHotObservable<IMessage> messagePublisher = new JFHotObservable<>();
    private final JFHotObservable<TickQuote> tickQuotePublisher = new JFHotObservable<>();
    private final JFHotObservable<BarQuote> barQuotePublisher = new JFHotObservable<>();

    private Subscription eventGatewaySubscription;

    private final static UserSettings userSettings = ConfigFactory.create(UserSettings.class);
    private static final Logger logger = LogManager.getLogger(JForexUtil.class);

    public JForexUtil(final IContext context) {
        this.context = context;

        initContextRelated();
        initInfrastructure();
        initQuoteProvider();
        initOrderRelated();

        calculationUtil = new CalculationUtil(tickQuoteProvider);
        riskPercentMM = new RiskPercentMM(account, calculationUtil);
    }

    private void initContextRelated() {
        engine = context.getEngine();
        account = context.getAccount();
        history = context.getHistory();
        dataService = context.getDataService();
        concurrentUtil = new ConcurrentUtil(context);
    }

    private void initInfrastructure() {
        orderEventGateway = new OrderEventGateway();

        eventGatewaySubscription = messagePublisher.get()
                .filter(message -> message.getOrder() != null)
                .map(OrderMessageData::new)
                .subscribe(orderEventGateway::onOrderMessageData);
    }

    private void initQuoteProvider() {
        tickQuoteProvider = new TickQuoteProvider(tickQuotePublisher.get(),
                                                  context.getSubscribedInstruments(),
                                                  history);
        barQuoteProvider = new BarQuoteProvider(barQuotePublisher.get(), history);
    }

    private void initOrderRelated() {
        orderCallExecutor = new OrderCallExecutor(concurrentUtil);
        positionFactory = new PositionFactory(orderEventGateway.observable());
        orderUtilHandler = new OrderUtilHandler(orderCallExecutor, orderEventGateway);
        orderCreateUtil = new OrderCreateUtil(context.getEngine(), orderUtilHandler);
        orderChangeUtil = new OrderChangeUtil(orderUtilHandler);
        positionSingleTask = new PositionSingleTask(orderCreateUtil,
                                                    orderChangeUtil);
        positionMultiTask = new PositionMultiTask(positionSingleTask);
        orderPositionUtil = new OrderPositionUtil(orderCreateUtil,
                                                  positionSingleTask,
                                                  positionMultiTask,
                                                  positionFactory);
        orderUtil = new OrderUtil(orderChangeUtil, orderPositionUtil);
    }

    public IContext context() {
        return context;
    }

    public IEngine engine() {
        return engine;
    }

    public IAccount account() {
        return account;
    }

    public TickQuoteProvider tickQuoteProvider() {
        return tickQuoteProvider;
    }

    public BarQuoteProvider barQuoteProvider() {
        return barQuoteProvider;
    }

    public InstrumentUtil instrumentUtil(final Instrument instrument) {
        return new InstrumentUtil(instrument, tickQuoteProvider, barQuoteProvider);
    }

    public CalculationUtil calculationUtil() {
        return calculationUtil;
    }

    public ConcurrentUtil concurrentUtil() {
        return concurrentUtil;
    }

    public OrderUtil orderUtil() {
        return orderUtil;
    }

    public void closeAllPositions() {
        positionFactory
                .all()
                .forEach(position -> orderUtil.closePosition(position.instrument()));
    }

    public RiskPercentMM riskPercentMM() {
        return riskPercentMM;
    }

    public void onStop() {
        eventGatewaySubscription.unsubscribe();
    }

    @Override
    public void onMessage(final IMessage message) {
        messagePublisher.onNext(message);
    }

    public void onTick(final Instrument instrument,
                       final ITick tick) {
        if (userSettings.enableWeekendQuoteFilter() && !isMarketClosed(tick.getTime()))
            tickQuotePublisher.onNext(new TickQuote(instrument, tick));
    }

    public void onBar(final Instrument instrument,
                      final Period period,
                      final IBar askBar,
                      final IBar bidBar) {
        if (userSettings.enableWeekendQuoteFilter() && !isMarketClosed(askBar.getTime()))
            barQuotePublisher.onNext(new BarQuote(instrument, period, askBar, bidBar));
    }

    public boolean isMarketClosed() {
        return isMarketClosed(DateTimeUtil.localMillisNow());
    }

    private boolean isMarketClosed(final long time) {
        try {
            final ITimeDomain offlineDuration = dataService.getOfflineTimeDomain();
            return time >= offlineDuration.getStart() && time <= offlineDuration.getEnd();
        } catch (final JFException e) {
            logger.error("Error retreiving offline time domain! " + e.getMessage());
            return false;
        }
    }
}
