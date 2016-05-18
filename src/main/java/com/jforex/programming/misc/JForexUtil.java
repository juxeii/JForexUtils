package com.jforex.programming.misc;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.aeonbits.owner.ConfigFactory;

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
import com.jforex.programming.mm.RiskPercentMM;
import com.jforex.programming.order.OrderMessageData;
import com.jforex.programming.order.OrderUtil;
import com.jforex.programming.order.call.OrderCallExecutor;
import com.jforex.programming.order.event.OrderEventGateway;
import com.jforex.programming.position.PositionFactory;
import com.jforex.programming.position.PositionTask;
import com.jforex.programming.quote.BarQuote;
import com.jforex.programming.quote.BarQuoteProvider;
import com.jforex.programming.quote.TickQuote;
import com.jforex.programming.quote.TickQuoteProvider;
import com.jforex.programming.settings.UserSettings;

import rx.Observable;
import rx.Subscription;

public class JForexUtil implements MessageConsumer {

    private final IContext context;
    private IEngine engine;
    private IAccount account;
    private IHistory history;

    private ConcurrentUtil concurrentUtil;

    private TickQuoteProvider tickQuoteProvider;
    private BarQuoteProvider barQuoteProvider;

    private OrderUtil orderUtil;
    private PositionTask positionTask;
    private PositionFactory positionFactory;
    private OrderEventGateway orderEventGateway;
    private OrderCallExecutor orderCallExecutor;

    private final CalculationUtil calculationUtil;
    private final RiskPercentMM riskPercentMM;
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    private final JFObservable<IMessage> messagePublisher = new JFObservable<IMessage>();
    private Observable<IMessage> messageObservable;

    private final JFObservable<TickQuote> tickQuotePublisher = new JFObservable<TickQuote>();
    private Observable<TickQuote> tickObservable;

    private final JFObservable<BarQuote> barQuotePublisher = new JFObservable<BarQuote>();
    private Observable<BarQuote> barObservable;

    private Subscription eventGatewaySubscription;

    private final static UserSettings userSettings = ConfigFactory.create(UserSettings.class);

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
        concurrentUtil = new ConcurrentUtil(context, executorService);
    }

    private void initInfrastructure() {
        orderEventGateway = new OrderEventGateway();

        messageObservable = messagePublisher.get();
        eventGatewaySubscription = messageObservable.filter(message -> message.getOrder() != null)
                .map(OrderMessageData::new)
                .subscribe(orderEventGateway::onOrderMessageData);
    }

    private void initQuoteProvider() {
        tickObservable = tickQuotePublisher.get();
        tickQuoteProvider = new TickQuoteProvider(tickObservable, context.getSubscribedInstruments(), history);

        barObservable = barQuotePublisher.get();
        barQuoteProvider = new BarQuoteProvider(barObservable, history);

    }

    private void initOrderRelated() {
        orderCallExecutor = new OrderCallExecutor(concurrentUtil);
        positionTask = new PositionTask(orderUtil);
        positionFactory = new PositionFactory(positionTask, orderEventGateway.observable());
        orderUtil = new OrderUtil(context.getEngine(),
                                  orderCallExecutor,
                                  orderEventGateway,
                                  positionFactory);
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
        positionFactory.all().forEach(position -> position.close().subscribe());
    }

    public RiskPercentMM riskPercentMM() {
        return riskPercentMM;
    }

    public void onStop() {
        eventGatewaySubscription.unsubscribe();
        concurrentUtil.onStop();
    }

    @Override
    public void onMessage(final IMessage message) {
        messagePublisher.onNext(message);
    }

    public void onTick(final Instrument instrument,
                       final ITick tick) {
        if (userSettings.enableWeekendQuoteFilter() && !DateTimeUtil.isWeekendMillis(tick.getTime()))
            tickQuotePublisher.onNext(new TickQuote(instrument, tick));
    }

    public void onBar(final Instrument instrument,
                      final Period period,
                      final IBar askBar,
                      final IBar bidBar) {
        if (userSettings.enableWeekendQuoteFilter() && !DateTimeUtil.isWeekendMillis(askBar.getTime()))
            barQuotePublisher.onNext(new BarQuote(instrument, period, askBar, bidBar));
    }
}
