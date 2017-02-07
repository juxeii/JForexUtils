package com.jforex.programming.test.common;

import static org.junit.Assert.assertTrue;
import static org.mockito.MockitoAnnotations.initMocks;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mockito.BDDMockito;
import org.mockito.Mock;

import com.dukascopy.api.IAccount;
import com.dukascopy.api.IContext;
import com.dukascopy.api.IDataService;
import com.dukascopy.api.IEngine;
import com.dukascopy.api.IHistory;
import com.dukascopy.api.IMessage;
import com.dukascopy.api.IMessage.Reason;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.JFException;
import com.dukascopy.api.system.IClient;
import com.dukascopy.api.system.ISystemListener;
import com.dukascopy.api.system.ITesterClient;
import com.jforex.programming.client.StrategyRunState;
import com.jforex.programming.connection.ConnectionState;
import com.jforex.programming.connection.LoginCredentials;
import com.jforex.programming.connection.LoginState;
import com.jforex.programming.connection.UserConnectionState;
import com.jforex.programming.currency.CurrencyCode;
import com.jforex.programming.misc.HistoryUtil;
import com.jforex.programming.order.OrderDirection;
import com.jforex.programming.order.OrderParams;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.task.BatchMode;
import com.jforex.programming.order.task.CancelSLTPMode;
import com.jforex.programming.order.task.CloseExecutionMode;
import com.jforex.programming.order.task.params.RetryParams;
import com.jforex.programming.order.task.params.SetSLTPMode;
import com.jforex.programming.order.task.params.TaskParamsType;
import com.jforex.programming.position.PositionDirection;
import com.jforex.programming.quote.BarQuoteProvider;
import com.jforex.programming.quote.TickQuoteProvider;
import com.jforex.programming.rx.RetryDelay;
import com.jforex.programming.rx.RetryDelayFunction;
import com.jforex.programming.settings.PlatformSettings;
import com.jforex.programming.settings.UserSettings;
import com.jforex.programming.strategy.StrategyUtil;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;
import nl.jqno.equalsverifier.EqualsVerifier;

public class CommonUtilForTest extends BDDMockito {

    @Mock
    protected StrategyUtil strategyUtilMock;
    @Mock
    protected IClient clientMock;
    @Mock
    protected ITesterClient testerClientMock;
    @Mock
    protected IContext contextMock;
    @Mock
    protected IEngine engineMock;
    @Mock
    protected IHistory historyMock;
    @Mock
    protected IAccount accountMock;
    @Mock
    protected IDataService dataServiceMock;
    @Mock
    protected HistoryUtil historyUtilMock;
    @Mock
    protected TickQuoteProvider tickQuoteHandlerMock;
    @Mock
    protected BarQuoteProvider barQuoteHandlerMock;

    protected JFException jfException = new JFException("JFException for test");

    protected static final String jnlpAddress = "http://jnlp.test.address";
    protected static final String username = "username";
    protected static final String password = "password";
    protected static final String pin = "1234";
    protected static final LoginCredentials loginCredentials = new LoginCredentials(jnlpAddress,
                                                                                    username,
                                                                                    password);
    protected static final LoginCredentials loginCredentialsWithPin = new LoginCredentials(jnlpAddress,
                                                                                           username,
                                                                                           password,
                                                                                           pin);

    protected static final OrderUtilForTest orderUtilForTest = new OrderUtilForTest();
    protected final ClientForTest clientForTest = new ClientForTest();
    protected final OrderParams buyParamsEURUSD = orderUtilForTest.buyParamsEURUSD();
    protected final OrderParams sellParamsEURUSD = orderUtilForTest.sellParamsEURUSD();
    protected final IOrder buyOrderEURUSD = orderUtilForTest.buyOrderEURUSD();
    protected final IOrder buyOrderEURUSD2 = orderUtilForTest.buyOrderEURUSD2();
    protected final IOrder sellOrderEURUSD = orderUtilForTest.sellOrderEURUSD();
    protected final IOrder buyOrderAUDUSD = orderUtilForTest.buyOrderAUDUSD();
    protected final IOrder sellOrderAUDUSD = orderUtilForTest.sellOrderAUDUSD();
    protected OrderEvent submitEvent = createEvent(OrderEventType.SUBMIT_OK);
    protected OrderEvent closeEvent = createEvent(OrderEventType.CLOSE_OK);
    protected OrderEvent closeRejectEvent = createEvent(OrderEventType.CLOSE_REJECTED);
    protected OrderEvent mergeEvent = createEvent(OrderEventType.MERGE_OK);
    protected OrderEvent mergeRejectEvent = createEvent(OrderEventType.MERGE_REJECTED);
    protected OrderEvent changedLabelEvent = createEvent(OrderEventType.CHANGED_LABEL);
    protected OrderEvent changedRejectEvent = createEvent(OrderEventType.CHANGED_REJECTED);
    protected OrderEvent changedSLEvent = createEvent(OrderEventType.CHANGED_SL);
    protected OrderEvent changedTPEvent = createEvent(OrderEventType.CHANGED_TP);

    protected static final RxTestUtil rxTestUtil = RxTestUtil.get();
    protected static final PlatformSettings platformSettings = StrategyUtil.platformSettings;
    protected static final UserSettings userSettings = StrategyUtil.userSettings;
    protected static final double noSL = platformSettings.noSLPrice();
    protected static final double noTP = platformSettings.noTPPrice();
    protected static final long delayInMillis = 1500L;
    protected static final RetryDelayFunction retryDelayFunction =
            attempt -> new RetryDelay(delayInMillis, TimeUnit.MILLISECONDS);
    protected static final RetryParams retryParams = new RetryParams(2, retryDelayFunction);

    protected static final Logger logger = LogManager.getLogger(CommonUtilForTest.class);

    public CommonUtilForTest() {
        initMocks(this);

        when(contextMock.getEngine()).thenReturn(engineMock);
        when(contextMock.getAccount()).thenReturn(accountMock);
        when(contextMock.getHistory()).thenReturn(historyMock);
        when(contextMock.getDataService()).thenReturn(dataServiceMock);

        when(strategyUtilMock.context()).thenReturn(contextMock);
        when(strategyUtilMock.engine()).thenReturn(engineMock);
        when(strategyUtilMock.account()).thenReturn(accountMock);
        when(strategyUtilMock.history()).thenReturn(historyMock);
        when(strategyUtilMock.historyUtil()).thenReturn(historyUtilMock);

        doAnswer(invocation -> {
            final ISystemListener listener = ((ISystemListener) invocation.getArgument(0));
            clientForTest.setSystemListener(listener);
            return listener;
        }).when(clientMock).setSystemListener(any());

        coverageOnEnumsCorrection();
    }

    public static final void setStrategyThread() {
        setThreadName(platformSettings.strategyThreadPrefix());
    }

    public static final void setNotStrategyThread() {
        setThreadName("Not" + platformSettings.strategyThreadPrefix());
    }

    public static final void setThreadName(final String threadName) {
        Thread.currentThread().setName(threadName);
    }

    protected void assertPrivateConstructor(final Class<?> clazz) throws Exception {
        final Constructor<?> constructor = clazz.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        constructor.newInstance();
    }

    protected final IMessage mockForIMessage(final IOrder order,
                                             final IMessage.Type type,
                                             final Set<Reason> reasons) {
        final IMessage messageMock = spy(IMessage.class);
        when(messageMock.getOrder()).thenReturn(order);
        when(messageMock.getType()).thenReturn(type);
        when(messageMock.getReasons()).thenReturn(reasons);

        return messageMock;
    }

    private final void coverageOnEnumsCorrection() {
        CurrencyCode
            .valueOf(CurrencyCode.EUR.toString());
        StrategyRunState
            .valueOf(StrategyRunState.STARTED.toString());
        OrderCallReason
            .valueOf(OrderCallReason.CHANGE_AMOUNT.toString());
        ConnectionState
            .valueOf(ConnectionState.CONNECTED.toString());
        LoginState
            .valueOf(LoginState.LOGGED_IN.toString());
        OrderEventType
            .valueOf(OrderEventType.SUBMIT_OK.toString());
        OrderDirection
            .valueOf(OrderDirection.LONG.toString());
        PositionDirection
            .valueOf(PositionDirection.FLAT.toString());
        CancelSLTPMode
            .valueOf(CancelSLTPMode.ConcatCancelSLAndTP.toString());
        CloseExecutionMode
            .valueOf(CloseExecutionMode.CloseAll.toString());
        BatchMode
            .valueOf(BatchMode.CONCAT.toString());
        SetSLTPMode
            .valueOf(SetSLTPMode.PIPS.toString());
        UserConnectionState
            .valueOf(UserConnectionState.CONNECTED.toString());
        TaskParamsType
            .valueOf(TaskParamsType.SETAMOUNT.toString());
    }

    protected final Completable emptyCompletable() {
        return Completable.complete();
    }

    protected final Observable<OrderEvent> emptyObservable() {
        return Observable.empty();
    }

    protected final Observable<OrderEvent> neverObservable() {
        return Observable.never();
    }

    protected final Observable<OrderEvent> eventObservable(final OrderEvent orderEvent) {
        return Observable.just(orderEvent);
    }

    protected final Observable<OrderEvent> eventObservable(final IOrder order,
                                                           final OrderEventType type) {
        final OrderEvent orderEvent = new OrderEvent(order,
                                                     type,
                                                     true);
        return eventObservable(orderEvent);
    }

    @SuppressWarnings("unchecked")
    protected final <T> T getOnNextEvent(final TestObserver<T> observer,
                                         final int index) {
        return (T) observer.getEvents().get(0).get(index);
    }

    protected final <T> void testEqualsContract(final T instance) {
        EqualsVerifier
            .forClass(instance.getClass())
            .verify();

        logger.info("toString() for " + instance.toString());
    }

    protected OrderEvent createEvent(final OrderEventType type) {
        return new OrderEvent(buyOrderEURUSD,
                              type,
                              true);
    }
}
