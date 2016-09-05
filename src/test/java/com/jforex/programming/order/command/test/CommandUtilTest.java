package com.jforex.programming.order.command.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.google.common.collect.Lists;
import com.jforex.programming.order.command.CommandUtil;
import com.jforex.programming.order.command.OrderUtilCommand;
import com.jforex.programming.test.common.CommonUtilForTest;

import rx.Completable;

public class CommandUtilTest extends CommonUtilForTest {

    @Mock
    private OrderUtilCommand commandOne;
    @Mock
    private OrderUtilCommand commandTwo;
    private final List<OrderUtilCommand> commands = Lists.newArrayList(commandOne, commandTwo);
    private final Completable completableOne = Completable.complete();
    private final Completable completableTwo = Completable.never();

    @Before
    public void setUp() {
        setUpMocks();
    }

    public void setUpMocks() {
        when(commandOne.completable()).thenReturn(completableOne);
        when(commandTwo.completable()).thenReturn(completableTwo);
    }

    @Test
    public void commandsToCompletablesReturnsCorrectList() {
        final List<Completable> completables = CommandUtil.commandsToCompletables(commands);

        assertThat(completables.size(), equalTo(2));
        assertThat(completables.get(0), equalTo(completableOne));
        assertThat(completables.get(1), equalTo(completableTwo));
    }
}
