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
import com.jforex.programming.order.command.CommandUtil;
import com.jforex.programming.order.command.OrderUtilCommand;
import com.jforex.programming.test.common.CommonUtilForTest;

import rx.Completable;

public class CommandUtilTest extends CommonUtilForTest {

    @Mock
    private OrderUtilCommand commandOne;
    @Mock
    private OrderUtilCommand commandTwo;
    @Mock
    private Callable<Double> callableOne;
    @Mock
    private Callable<Double> callableTwo;
    @Mock
    private Function<IOrder, OrderUtilCommand> commandFactoryMock;
    private Completable completableOne;
    private Completable completableTwo;
    private List<OrderUtilCommand> commands;

    @Before
    public void setUp() {
        completableOne = Completable.fromCallable(callableOne);
        completableTwo = Completable.fromCallable(callableTwo);
        commands = Lists.newArrayList(commandOne, commandTwo);

        setUpMocks();
    }

    private void setUpMocks() {
        when(commandOne.completable()).thenReturn(completableOne);
        when(commandTwo.completable()).thenReturn(completableTwo);
    }

    private void verifyInOrderCall() throws Exception {
        final InOrder inOrder = inOrder(callableOne, callableTwo);

        inOrder.verify(callableOne).call();
        inOrder.verify(callableTwo).call();
    }

    @Test
    public void testConstructorIsPrivate() throws Exception {
        assertPrivateConstructor(CommandUtil.class);
    }

    @Test
    public void commandsToCompletablesReturnsCorrectList() {
        final List<Completable> completables = CommandUtil.commandsToCompletables(commands);

        assertThat(completables.size(), equalTo(2));
        assertTrue(completables.contains(completableOne));
        assertTrue(completables.contains(completableTwo));
    }

    @Test
    public void runCommandsMergesCompletables() throws Exception {
        CommandUtil
            .runCommands(commands)
            .subscribe();

        verify(callableOne).call();
        verify(callableTwo).call();
    }

    @Test
    public void runCommandsConcatenatedIsCorrect() throws Exception {
        CommandUtil
            .runCommandsConcatenated(commands)
            .subscribe();

        verifyInOrderCall();
    }

    @Test
    public void runCommandsConcatenatedWithVarargsIsCorrect() throws Exception {
        CommandUtil
            .runCommandsConcatenated(commandOne, commandTwo)
            .subscribe();

        verifyInOrderCall();
    }

    @Test
    public void createBatchCommandsIsCorrect() {
        when(commandFactoryMock.apply(buyOrderEURUSD)).thenReturn(commandOne);
        when(commandFactoryMock.apply(sellOrderEURUSD)).thenReturn(commandTwo);
        final Set<IOrder> batchOrders = Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD);

        final List<OrderUtilCommand> commands = CommandUtil.createBatchCommands(batchOrders, commandFactoryMock);

        assertThat(commands.size(), equalTo(2));
        assertTrue(commands.contains(commandOne));
        assertTrue(commands.contains(commandTwo));
    }
}
