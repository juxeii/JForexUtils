package com.jforex.programming.order.command.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.google.common.collect.Lists;
import com.jforex.programming.order.OrderUtilCompletable;
import com.jforex.programming.order.command.CommandUtil;
import com.jforex.programming.order.command.CommonCommand;
import com.jforex.programming.test.common.CommonUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Completable;
import io.reactivex.subscribers.TestSubscriber;

@RunWith(HierarchicalContextRunner.class)
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
    private TestSubscriber<Void> testSubscriber;
    private final Completable completableOne = Completable.fromCallable(callableOne);
    private final Completable completableTwo = Completable.fromCallable(callableTwo);
    private final List<IOrder> orders = Lists.newArrayList(buyOrderEURUSD, sellOrderEURUSD);
    private final List<CommonCommand> commands = Lists.newArrayList(commandOne, commandTwo);

    @Before
    public void setUp() {
        setUpMocks();

        commandUtil = new CommandUtil(orderUtilCompletableMock);
    }

    private void setUpMocks() {
        when(commandFactoryMock.apply(buyOrderEURUSD)).thenReturn(commandOne);
        when(commandFactoryMock.apply(sellOrderEURUSD)).thenReturn(commandTwo);
    }

    @Test
    public void mergeCallForEmptyListCompletesImmediately() {
        testSubscriber = commandUtil
            .merge(Lists.newArrayList())
            .test();

        testSubscriber.assertComplete();
    }

    @Test
    public void concatCallForEmptyListCompletesImmediately() {
        testSubscriber = commandUtil
            .concat(Lists.newArrayList())
            .test();

        testSubscriber.assertComplete();
    }

    @Test
    public void fromFactoryCommandsAreCorrect() {
        final List<CommonCommand> commands = commandUtil.fromFactory(orders, commandFactoryMock);

        assertThat(commands.size(), equalTo(2));
        assertThat(commands.get(0), equalTo(commandOne));
        assertThat(commands.get(1), equalTo(commandTwo));
    }

    public class CombineCommandsTests {

        private void setUpCompletabless(final Completable firstCompletable,
                                        final Completable secondCompletable) {
            when(orderUtilCompletableMock.forCommand(commandOne)).thenReturn(firstCompletable);
            when(orderUtilCompletableMock.forCommand(commandTwo)).thenReturn(secondCompletable);
        }

        public class FirstCompletableBlocks {

            @Before
            public void setUp() {
                setUpCompletabless(neverCompletable(), completableTwo);
            }

            @Test
            public void mergeCallForListDoesNotConcat() throws Exception {
                commandUtil
                    .merge(commands)
                    .subscribe();

                verify(callableTwo).call();
            }

            @Test
            public void concatCallForListDoesNotMerge() {
                commandUtil
                    .concat(commands)
                    .subscribe();

                verifyZeroInteractions(callableTwo);
            }

            @Test
            public void mergeCallForArrayDoesNotConcat() throws Exception {
                commandUtil
                    .merge(commandOne, commandTwo)
                    .subscribe();

                verify(callableTwo).call();
            }

            @Test
            public void concatCallForArrayDoesNotMerge() throws Exception {
                commandUtil
                    .concat(commandOne, commandTwo)
                    .subscribe();

                verifyZeroInteractions(callableTwo);
            }

            @Test
            public void mergeFromFactoryDoesNotConcat() throws Exception {
                commandUtil
                    .mergeFromFactory(orders, commandFactoryMock)
                    .subscribe();

                verify(callableTwo).call();
            }

            @Test
            public void concatFromFactoryDoesNotMerge() {
                commandUtil
                    .concatFromFactory(orders, commandFactoryMock)
                    .subscribe();

                verifyZeroInteractions(callableTwo);
            }
        }

        public class BothCommandsComplete {

            @Before
            public void setUp() {
                setUpCompletabless(completableOne, completableTwo);
            }

            private void verifyBothCommandsCompleted() throws Exception {
                verify(callableOne).call();
                verify(callableTwo).call();
                testSubscriber.assertComplete();
            }

            @Test
            public void mergeCallCompletesAndBothCommandsCompleted() throws Exception {
                testSubscriber = commandUtil
                    .merge(commands)
                    .test();

                verifyBothCommandsCompleted();
            }

            @Test
            public void concatCallCompletesAndBothCommandsCompleted() throws Exception {
                testSubscriber = commandUtil
                    .concat(commands)
                    .test();

                verifyBothCommandsCompleted();
            }
        }
    }
}
