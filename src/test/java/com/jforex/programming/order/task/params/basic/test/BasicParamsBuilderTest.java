package com.jforex.programming.order.task.params.basic.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.util.function.Consumer;

import org.junit.Test;
import org.mockito.Mock;

import com.jforex.programming.order.task.params.ComposeParams;
import com.jforex.programming.order.task.params.basic.BasicParamsBuilder;
import com.jforex.programming.test.common.InstrumentUtilForTest;

import io.reactivex.functions.Action;

public class BasicParamsBuilderTest extends InstrumentUtilForTest {

    private class BuilderForTest extends BasicParamsBuilder<BuilderForTest> {

        public ComposeParams composeParams() {
            return composeParams;
        }
    }

    private final BuilderForTest builderForTest = new BuilderForTest();

    @Mock
    private Action startActionMock;
    @Mock
    private Action completeActionMock;
    @Mock
    private Consumer<Throwable> errorConsumerMock;

    @Test
    public void composeParamsAreNotNull() {
        assertNotNull(builderForTest.composeParams());
    }

    @Test
    public void setOfHandlersIsCorrect() {
        builderForTest.doOnStart(startActionMock);
        builderForTest.doOnComplete(completeActionMock);
        builderForTest.doOnError(errorConsumerMock);

        final ComposeParams composeParams = builderForTest.composeParams();

        assertThat(composeParams.startAction(), equalTo(startActionMock));
        assertThat(composeParams.completeAction(), equalTo(completeActionMock));
        assertThat(composeParams.errorConsumer(), equalTo(errorConsumerMock));
    }
}
