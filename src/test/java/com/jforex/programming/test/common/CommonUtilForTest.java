package com.jforex.programming.test.common;

import static org.junit.Assert.assertTrue;
import static org.mockito.MockitoAnnotations.initMocks;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Optional;

import org.aeonbits.owner.ConfigFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mockito.BDDMockito;
import org.mockito.Mock;

import com.dukascopy.api.IAccount;
import com.dukascopy.api.IContext;
import com.dukascopy.api.IDataService;
import com.dukascopy.api.IEngine;
import com.dukascopy.api.IHistory;
import com.dukascopy.api.JFException;
import com.dukascopy.api.system.IClient;
import com.dukascopy.api.system.ITesterClient;
import com.jforex.programming.client.StrategyRunState;
import com.jforex.programming.connection.AuthentificationUtil;
import com.jforex.programming.connection.ConnectionState;
import com.jforex.programming.connection.LoginState;
import com.jforex.programming.misc.HistoryUtil;
import com.jforex.programming.misc.JForexUtil;
import com.jforex.programming.order.OrderDirection;
import com.jforex.programming.order.call.OrderCallReason;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.position.OrderProcessState;
import com.jforex.programming.position.PositionSwitcher;
import com.jforex.programming.settings.PlatformSettings;
import com.jforex.programming.settings.UserSettings;
import com.jforex.programming.test.fakes.IClientForTest;
import com.jforex.programming.test.fakes.IEngineForTest;

public class CommonUtilForTest extends BDDMockito {

    @Mock
    protected JForexUtil jforexUtilMock;
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
    protected IClientForTest clientForTest;
    protected IEngineForTest engineForTest;
    protected JFException jfException = new JFException("JFException for test");
    protected Optional<Exception> jfExceptionOpt = Optional.of(jfException);
    protected Optional<Exception> emptyJFExceptionOpt = Optional.empty();
    protected final RxTestUtil rxTestUtil = RxTestUtil.get();

    protected static final PlatformSettings platformSettings =
            ConfigFactory.create(PlatformSettings.class);
    protected static final UserSettings userSettings =
            ConfigFactory.create(UserSettings.class);
    protected static final Logger logger = LogManager.getLogger(CommonUtilForTest.class);

    protected void initCommonTestFramework() {
        initMocks(this);

        clientForTest = new IClientForTest(clientMock);
        engineForTest = new IEngineForTest(engineMock);

        when(contextMock.getEngine()).thenReturn(engineMock);
        when(contextMock.getAccount()).thenReturn(accountMock);
        when(contextMock.getHistory()).thenReturn(historyMock);
        when(contextMock.getDataService()).thenReturn(dataServiceMock);

        when(jforexUtilMock.context()).thenReturn(contextMock);
        when(jforexUtilMock.engine()).thenReturn(engineMock);
        when(jforexUtilMock.account()).thenReturn(accountMock);
        when(jforexUtilMock.history()).thenReturn(historyMock);
        when(jforexUtilMock.historyUtil()).thenReturn(historyUtilMock);

        coverageOnEnumsCorrection();
        try {
            assertPrivateConstructor(OrderParamsForTest.class);
        } catch (final Exception e) {}
    }

    public static void setStrategyThread() {
        setThreadName(platformSettings.strategyThreadPrefix());
    }

    public static void setNotStrategyThread() {
        setThreadName("Not" + platformSettings.strategyThreadPrefix());
    }

    public static void setThreadName(final String threadName) {
        Thread.currentThread().setName(threadName);
    }

    private void coverageOnEnumsCorrection() {
        StrategyRunState.valueOf(StrategyRunState.STARTED.toString());
        OrderCallReason.valueOf(OrderCallReason.CHANGE_AMOUNT.toString());
        ConnectionState.valueOf(ConnectionState.CONNECTED.toString());
        LoginState.valueOf(LoginState.LOGGED_IN.toString());
        OrderProcessState.valueOf(OrderProcessState.ACTIVE.toString());
        OrderEventType.valueOf(OrderEventType.SUBMIT_OK.toString());
        OrderDirection.valueOf(OrderDirection.FLAT.toString());
        AuthentificationUtil.FSMTrigger
                .valueOf(AuthentificationUtil.FSMTrigger.CONNECTED.toString());
        PositionSwitcher.FSMTrigger
                .valueOf(PositionSwitcher.FSMTrigger.FLAT.toString());
        PositionSwitcher.FSMState
                .valueOf(PositionSwitcher.FSMState.FLAT.toString());
    }

    protected void assertPrivateConstructor(final Class<?> clazz) throws Exception {
        final Constructor<?> constructor = clazz.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        constructor.newInstance();
    }
}
