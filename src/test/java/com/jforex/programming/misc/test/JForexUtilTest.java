package com.jforex.programming.misc.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.jforex.programming.misc.JForexUtil;
import com.jforex.programming.test.common.CommonUtilForTest;

public class JForexUtilTest extends CommonUtilForTest {

    private JForexUtil jForexUtil;

    @Before
    public void setUp() {
        initCommonTestFramework();

        jForexUtil = new JForexUtil(contextMock);
    }

    @Test
    public void returnedContextIsCorrectInstance() {
        assertThat(jForexUtil.context(), equalTo(contextMock));
    }

    @Test
    public void returnedEngineIsCorrectInstance() {
        assertThat(jForexUtil.engine(), equalTo(engineMock));
    }

    @Test
    public void returnedAccountIsCorrectInstance() {
        assertThat(jForexUtil.account(), equalTo(accountMock));
    }

    @Test
    public void returnedHistoryIsCorrectInstance() {
        assertThat(jForexUtil.history(), equalTo(historyMock));
    }
}
