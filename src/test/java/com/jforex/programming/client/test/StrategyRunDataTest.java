package com.jforex.programming.client.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.jforex.programming.client.StrategyRunData;
import com.jforex.programming.client.StrategyRunState;
import com.jforex.programming.test.common.QuoteProviderForTest;

public class StrategyRunDataTest extends QuoteProviderForTest {

    private final StrategyRunData strategyRunData = new StrategyRunData(1L, StrategyRunState.STARTED);

    @Test
    public void allAccessorsAreCorrect() {
        assertThat(strategyRunData.processID(), equalTo(1L));
        assertThat(strategyRunData.state(), equalTo(StrategyRunState.STARTED));
    }

    @Test
    public void isEqualsContractOK() {
        testEqualsContract(strategyRunData);
    }
}