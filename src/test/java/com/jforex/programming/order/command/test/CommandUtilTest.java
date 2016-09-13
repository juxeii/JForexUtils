package com.jforex.programming.order.command.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.google.common.collect.Lists;
import com.jforex.programming.order.OrderUtilCompletable;
import com.jforex.programming.order.command.CommandUtil;
import com.jforex.programming.order.command.CommonCommand;
import com.jforex.programming.test.common.CommonUtilForTest;

import io.reactivex.Completable;
import io.reactivex.subscribers.TestSubscriber;

public class CommandUtilTest extends CommonUtilForTest {

    private CommandUtil commandUtil;

    @Mock
    private OrderUtilCompletable orderUtilCompletableMock;
    @Mock
    private CommonCommand commandOne;
    @Mock
    private CommonCommand commandTwo;
    @Mock
    private Callable<Double> callableOne;
    @Mock
    private Callable<Double> callableTwo;
    @Mock
    private Function<IOrder, CommonCommand> commandFactoryMock;
    private Completable completableOne;
    private Completable completableTwo;
    private List<CommonCommand> commands;
    private final List<IOrder> orders = Lists.newArrayList(buyOrderEURUSD, sellOrderEURUSD);
    private TestSubscriber<Void> testSubscriber;

    @Before
    public void setUp() {
        completableOne = Completable.fromCallable(callableOne);
        completableTwo = Completable.fromCallable(callableTwo);
        commands = Lists.newArrayList(commandOne, commandTwo);

        commandUtil = new CommandUtil(orderUtilCompletableMock);
    }

    private void setUpCommandToCompletable() {
        when(orderUtilCompletableMock.commandToCompletable(commandOne)).thenReturn(completableOne);
        when(orderUtilCompletableMock.commandToCompletable(commandTwo)).thenReturn(completableTwo);
    }

    private void setUpCompletables(final Completable one,
                                   final Completable two) {
        completableOne = one;
        completableTwo = two;
        setUpCommandToCompletable();
    }

    @Test
    public void mergeCallForEmptyListCompletesImmediately() throws Exception {
        testSubscriber = commandUtil
            .merge(Lists.newArrayList())
            .test();

        testSubscriber.assertComplete();
    }

    @Test
    public void mergeCallForListDoesNotConcat() throws Exception {
        setUpCompletables(neverCompletable(), Completable.fromCallable(callableTwo));

        commandUtil
            .merge(commands)
            .subscribe();

        verify(callableTwo).call();
    }

    @Test
    public void mergeCallForArrayDoesNotConcat() throws Exception {
        setUpCompletables(neverCompletable(), Completable.fromCallable(callableTwo));

        commandUtil
            .merge(commandOne, commandTwo)
            .subscribe();

        verify(callableTwo).call();
    }

    @Test
    public void concatCallForEmptyListCompletesImmediately() throws Exception {
        testSubscriber = commandUtil
            .concat(Lists.newArrayList())
            .test();

        testSubscriber.assertComplete();
    }

    @Test
    public void concatCallForListDoesNotMerge() throws Exception {
        setUpCompletables(neverCompletable(), Completable.fromCallable(callableTwo));

        commandUtil
            .concat(commands)
            .subscribe();

        verifyZeroInteractions(callableTwo);
    }

    @Test
    public void concatCallForArrayDoesNotMerge() throws Exception {
        setUpCompletables(neverCompletable(), Completable.fromCallable(callableTwo));

        commandUtil
            .concat(commandOne, commandTwo)
            .subscribe();

        verifyZeroInteractions(callableTwo);
    }

    @Test
    public void mergeFromFactoryDoesNotConcat() throws Exception {
        final List<IOrder> orders = Lists.newArrayList(buyOrderEURUSD, sellOrderEURUSD);
        when(commandFactoryMock.apply(buyOrderEURUSD)).thenReturn(commandOne);
        when(commandFactoryMock.apply(sellOrderEURUSD)).thenReturn(commandTwo);
        setUpCompletables(neverCompletable(), Completable.fromCallable(callableTwo));

        commandUtil
            .mergeFromFactory(orders, commandFactoryMock)
            .subscribe();

        verify(callableTwo).call();
    }

    @Test
    public void concatFromFactoryDoesNotMerge() throws Exception {
        when(commandFactoryMock.apply(buyOrderEURUSD)).thenReturn(commandOne);
        when(commandFactoryMock.apply(sellOrderEURUSD)).thenReturn(commandTwo);
        setUpCompletables(neverCompletable(), Completable.fromCallable(callableTwo));

        commandUtil
            .concatFromFactory(orders, commandFactoryMock)
            .subscribe();

        verifyZeroInteractions(callableTwo);
    }

    @Test
    public void fromFactoryCommandsAreCorrect() {
        when(commandFactoryMock.apply(buyOrderEURUSD)).thenReturn(commandOne);
        when(commandFactoryMock.apply(sellOrderEURUSD)).thenReturn(commandTwo);
        setUpCompletables(neverCompletable(), Completable.fromCallable(callableTwo));

        final List<CommonCommand> commands = commandUtil.fromFactory(orders, commandFactoryMock);

        assertThat(commands.size(), equalTo(2));
        assertThat(commands.get(0), equalTo(commandOne));
        assertThat(commands.get(1), equalTo(commandTwo));
    }

    @Test
    public void toCompletablesReturnsCorrectList() {
        setUpCompletables(neverCompletable(), Completable.fromCallable(callableTwo));
        final List<Completable> completables = commandUtil.toCompletables(commands);

        assertThat(completables.size(), equalTo(2));
        assertThat(completables.get(0), equalTo(completableOne));
        assertThat(completables.get(1), equalTo(completableTwo));
    }
}
