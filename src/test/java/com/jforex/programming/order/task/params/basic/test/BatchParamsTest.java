package com.jforex.programming.order.task.params.basic.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.jforex.programming.order.task.params.TaskParams;
import com.jforex.programming.order.task.params.basic.BatchParams;
import com.jforex.programming.order.task.params.test.CommonParamsForTest;

public class BatchParamsTest extends CommonParamsForTest {

    private BatchParams batchParams;

    private final List<TaskParams> paramsList = new ArrayList<>();

    @Before
    public void setUp() {
        batchParams = BatchParams
            .setBatchWith(paramsList)
            .build();
    }

    @Test
    public void valuesAreCorrect() {
        assertThat(batchParams.taskParams(), equalTo(paramsList));
    }
}
