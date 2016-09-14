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
import com.jforex.programming.order.command.Command;
import com.jforex.programming.order.command.CommandUtil;
import com.jforex.programming.order.command.option.MergeOption;
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
    private Command commandOne;
    @Mock
    private Command commandTwo;
    @Mock
    private Callable<Double> callableOne;
    @Mock
    private Callable<Double> callableTwo;
    @Mock
    private MergeOption mergeOptionEURUSD;
    @Mock
    private MergeOption mergeOptionAUDUSD;
    @Mock
    private Function<IOrder, MergeOption> optionFactoryMock;
    private TestSubscriber<Void> testSubscriber;
    private final Completable completableOne = Completable.fromCallable(callableOne);
    private final Completable completableTwo = Completable.fromCallable(callableTwo);
    private final List<IOrder> orders = Lists.newArrayList(buyOrderEURUSD, sellOrderEURUSD);
    private final List<Command> commands = Lists.newArrayList(commandOne, commandTwo);

    @Before
    public void setUp() {
        setUpMocks();

        commandUtil = new CommandUtil(orderUtilCompletableMock);
    }

    private void setUpMocks() {
        when(optionFactoryMock.apply(buyOrderEURUSD)).thenReturn(mergeOptionEURUSD);
        when(optionFactoryMock.apply(sellOrderEURUSD)).thenReturn(mergeOptionAUDUSD);
        when(mergeOptionEURUSD.build()).thenReturn(commandOne);
        when(mergeOptionAUDUSD.build()).thenReturn(commandTwo);
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
    public void fromOptionCommandsAreCorrect() {
        final List<Command> commands = commandUtil.fromOption(orders, optionFactoryMock);

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
            public void mergeFromOptionDoesNotConcat() throws Exception {
                commandUtil
                    .mergeFromOption(orders, optionFactoryMock)
                    .subscribe();

                verify(callableTwo).call();
            }

            @Test
            public void concatFromFactoryDoesNotMerge() {
                commandUtil
                    .concatFromOption(orders, optionFactoryMock)
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
