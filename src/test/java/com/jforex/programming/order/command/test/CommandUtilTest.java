package com.jforex.programming.order.command.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.jforex.programming.order.OrderUtilCompletable;
import com.jforex.programming.order.command.CommandUtil;
import com.jforex.programming.order.command.CommonCommand;
import com.jforex.programming.test.common.CommonUtilForTest;

import io.reactivex.Completable;

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

    @Before
    public void setUp() {
        completableOne = Completable.fromCallable(callableOne);
        completableTwo = Completable.fromCallable(callableTwo);
        commands = Lists.newArrayList(commandOne, commandTwo);

        setUpMocks();

        commandUtil = spy(new CommandUtil(orderUtilCompletableMock));
    }

    private void setUpMocks() {
        when(orderUtilCompletableMock.commandToCompletable(commandOne)).thenReturn(completableOne);
        when(orderUtilCompletableMock.commandToCompletable(commandTwo)).thenReturn(completableTwo);
    }

    private void verifyInOrderCall() throws Exception {
        final InOrder inOrder = inOrder(callableOne, callableTwo);

        inOrder.verify(callableOne).call();
        inOrder.verify(callableTwo).call();
    }

    @Test
    public void commandsToCompletablesReturnsCorrectList() {
        final List<Completable> completables = commandUtil.commandsToCompletables(commands);

        assertThat(completables.size(), equalTo(2));
        assertTrue(completables.contains(completableOne));
        assertTrue(completables.contains(completableTwo));
    }

    @Test
    public void runCommandsMergesCompletables() throws Exception {
        commandUtil
            .runCommands(commands)
            .subscribe();

        verify(callableOne).call();
        verify(callableTwo).call();
    }

    @Test
    public void runCommandsOfFactoryMergesCompletables() throws Exception {
        final Set<IOrder> orders = Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD);
        when(commandFactoryMock.apply(buyOrderEURUSD)).thenReturn(commandOne);
        when(commandFactoryMock.apply(sellOrderEURUSD)).thenReturn(commandTwo);

        commandUtil
            .runCommandsOfFactory(orders, commandFactoryMock)
            .subscribe();

        verify(commandUtil).batchCommands(orders, commandFactoryMock);
        verify(commandUtil).runCommands(any());
    }

    @Test
    public void runCommandsConcatenatedIsCorrect() throws Exception {
        commandUtil
            .runCommandsConcatenated(commands)
            .subscribe();

        verifyInOrderCall();
    }

    @Test
    public void runCommandsConcatenatedWithVarargsIsCorrect() throws Exception {
        commandUtil
            .runCommandsConcatenated(commandOne, commandTwo)
            .subscribe();

        verifyInOrderCall();
    }

    @Test
    public void createBatchCommandsIsCorrect() {
        when(commandFactoryMock.apply(buyOrderEURUSD)).thenReturn(commandOne);
        when(commandFactoryMock.apply(sellOrderEURUSD)).thenReturn(commandTwo);
        final Set<IOrder> batchOrders = Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD);

        final List<CommonCommand> commands = commandUtil.batchCommands(batchOrders, commandFactoryMock);

        assertThat(commands.size(), equalTo(2));
        assertTrue(commands.contains(commandOne));
        assertTrue(commands.contains(commandTwo));
    }
}
