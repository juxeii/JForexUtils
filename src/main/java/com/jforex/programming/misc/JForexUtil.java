package com.jforex.programming.misc;

import org.aeonbits.owner.ConfigFactory;
import org.apache.commons.lang3.StringUtils;

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
import com.jforex.programming.order.event.OrderEventMapper;
import com.jforex.programming.position.PositionFactory;
import com.jforex.programming.position.PositionMultiTask;
import com.jforex.programming.position.PositionSingleTask;
import com.jforex.programming.quote.BarQuote;
import com.jforex.programming.quote.BarQuoteHandler;
import com.jforex.programming.quote.BarQuoteProvider;
import com.jforex.programming.quote.BarQuoteRepository;
import com.jforex.programming.quote.TickQuote;
import com.jforex.programming.quote.TickQuoteHandler;
import com.jforex.programming.quote.TickQuoteProvider;
import com.jforex.programming.quote.TickQuoteRepository;
import com.jforex.programming.settings.PlatformSettings;
import com.jforex.programming.settings.UserSettings;

import com.dukascopy.api.IAccount;
import com.dukascopy.api.IBar;
import com.dukascopy.api.IContext;
import com.dukascopy.api.IDataService;
import com.dukascopy.api.IEngine;
import com.dukascopy.api.IHistory;
import com.dukascopy.api.IMessage;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.OfferSide;
import com.dukascopy.api.Period;

import rx.Subscription;

public class JForexUtil {

    private final IContext context;
    private IEngine engine;
    private IAccount account;
    private IHistory history;
    private HistoryUtil historyUtil;
    private IDataService dataService;

    private TickQuoteHandler tickQuoteHandler;
    private TickQuoteRepository tickQuoteRepository;
    private BarQuoteHandler barQuoteHandler;
    private BarQuoteRepository barQuoteRepository;

    private PositionFactory positionFactory;
    private OrderEventGateway orderEventGateway;
    private OrderCallExecutor orderCallExecutor;
    private OrderUtilHandler orderUtilHandler;
    private OrderCreateUtil orderCreateUtil;
    private OrderChangeUtil orderChangeUtil;
    private OrderUtil orderUtil;
    private final OrderEventMapper orderEventMapper = new OrderEventMapper();

    private PositionSingleTask positionSingleTask;
    private PositionMultiTask positionMultiTask;
    private OrderPositionUtil orderPositionUtil;

    private final CalculationUtil calculationUtil;
    private final RiskPercentMM riskPercentMM;

    private final JFHotSubject<TickQuote> tickQuotePublisher = new JFHotSubject<>();
    private final JFHotSubject<BarQuote> barQuotePublisher = new JFHotSubject<>();
    private final JFHotSubject<IMessage> messagePublisher = new JFHotSubject<>();
    private Subscription eventGatewaySubscription;

    private final static PlatformSettings platformSettings = ConfigFactory.create(PlatformSettings.class);
    private final static UserSettings userSettings = ConfigFactory.create(UserSettings.class);

    public JForexUtil(final IContext context) {
        this.context = context;

        initContextRelated();
        initInfrastructure();
        initQuoteProvider();
        initOrderRelated();

        calculationUtil = new CalculationUtil(tickQuoteHandler);
        riskPercentMM = new RiskPercentMM(account, calculationUtil);
    }

    private void initContextRelated() {
        engine = context.getEngine();
        account = context.getAccount();
        history = context.getHistory();
        historyUtil = new HistoryUtil(history);
        dataService = context.getDataService();
    }

    private void initInfrastructure() {
        orderEventGateway = new OrderEventGateway(orderEventMapper);

        eventGatewaySubscription = messagePublisher.observable()
                .filter(message -> message.getOrder() != null)
                .map(OrderMessageData::new)
                .subscribe(orderEventGateway::onOrderMessageData);
    }

    private void initQuoteProvider() {
        tickQuoteRepository = new TickQuoteRepository(tickQuotePublisher.observable(),
                                                      historyUtil,
                                                      context.getSubscribedInstruments());
        tickQuoteHandler = new TickQuoteHandler(tickQuotePublisher.observable(),
                                                tickQuoteRepository);
        barQuoteRepository = new BarQuoteRepository(barQuotePublisher.observable(),
                                                    historyUtil);
        barQuoteHandler = new BarQuoteHandler(this,
                                              barQuotePublisher.observable(),
                                              barQuoteRepository);
    }

    private void initOrderRelated() {
        orderCallExecutor = new OrderCallExecutor(context);
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

    public IHistory history() {
        return history;
    }

    public HistoryUtil historyUtil() {
        return historyUtil;
    }

    public TickQuoteProvider tickQuoteProvider() {
        return tickQuoteHandler;
    }

    public BarQuoteProvider barQuoteProvider() {
        return barQuoteHandler;
    }

    public InstrumentUtil instrumentUtil(final Instrument instrument) {
        return new InstrumentUtil(instrument, tickQuoteHandler, barQuoteHandler);
    }

    public CalculationUtil calculationUtil() {
        return calculationUtil;
    }

    public OrderUtil orderUtil() {
        return orderUtil;
    }

    public void closeAllPositions() {
        positionFactory
                .all()
                .forEach(position -> orderUtil.closePosition(position.instrument()).subscribe());
    }

    public RiskPercentMM riskPercentMM() {
        return riskPercentMM;
    }

    public void onStop() {
        eventGatewaySubscription.unsubscribe();
    }

    public void onMessage(final IMessage message) {
        messagePublisher.onNext(message);
    }

    public void onTick(final Instrument instrument,
                       final ITick tick) {
        if (!userSettings.enableWeekendQuoteFilter() || !isMarketClosed(tick.getTime())) {
            final TickQuote tickQuote = new TickQuote(instrument, tick);
            tickQuotePublisher.onNext(tickQuote);
        }
    }

    public void onBar(final Instrument instrument,
                      final Period period,
                      final IBar askBar,
                      final IBar bidBar) {
        onOfferSidedBar(instrument, period, OfferSide.ASK, askBar);
        onOfferSidedBar(instrument, period, OfferSide.BID, bidBar);
    }

    public void onOfferSidedBar(final Instrument instrument,
                                final Period period,
                                final OfferSide offerside,
                                final IBar askBar) {
        if (!userSettings.enableWeekendQuoteFilter() || !isMarketClosed(askBar.getTime())) {
            final BarQuote askBarQuote = new BarQuote(instrument, period, offerside, askBar);
            barQuotePublisher.onNext(askBarQuote);
        }
    }

    public void subscribeToBarsFeed(final Instrument instrument,
                                    final Period period,
                                    final OfferSide offerside) {
        context.subscribeToBarsFeed(instrument,
                                    period,
                                    offerside,
                                    this::onOfferSidedBar);
    }

    public boolean isMarketClosed() {
        return isMarketClosed(DateTimeUtil.localMillisNow());
    }

    public boolean isMarketClosed(final long time) {
        return dataService.isOfflineTime(time);
    }

    public static boolean isStrategyThread() {
        return StringUtils.startsWith(threadName(), platformSettings.strategyThreadPrefix());
    }

    public static String threadName() {
        return Thread.currentThread().getName();
    }
}
