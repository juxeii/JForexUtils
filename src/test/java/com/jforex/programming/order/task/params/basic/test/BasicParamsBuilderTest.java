package com.jforex.programming.order.task.params.basic.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.util.function.Consumer;

import org.junit.Test;
import org.mockito.Mock;

import com.jforex.programming.order.task.params.basic.BasicParamsBuilder;
import com.jforex.programming.test.common.InstrumentUtilForTest;

import io.reactivex.functions.Action;

public class BasicParamsBuilderTest extends InstrumentUtilForTest {

    private class BuilderForTest extends BasicParamsBuilder<BuilderForTest> {

        public Action startAction() {
            return startAction;
        }

        public Action completeAction() {
            return completeAction;
        }

        public Consumer<Throwable> errorConsumer() {
            return errorConsumer;
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
    public void handlersAreNotNull() throws Exception {
        assertNotNull(builderForTest.startAction());
        assertNotNull(builderForTest.completeAction());
        assertNotNull(builderForTest.errorConsumer());

        builderForTest.startAction().run();
        builderForTest.completeAction().run();
        builderForTest.errorConsumer().accept(jfException);
    }

    @Test
    public void setOfHandlersIsCorrect() {
        builderForTest.doOnStart(startActionMock);
        builderForTest.doOnComplete(completeActionMock);
        builderForTest.doOnError(errorConsumerMock);

        assertThat(builderForTest.startAction(), equalTo(startActionMock));
        assertThat(builderForTest.completeAction(), equalTo(completeActionMock));
        assertThat(builderForTest.errorConsumer(), equalTo(errorConsumerMock));
    }
}
