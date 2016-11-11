package com.jforex.programming.order.task.params.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.jforex.programming.order.task.params.ComposeParams;
import com.jforex.programming.order.task.params.ComposeParamsForOrder;
import com.jforex.programming.order.task.params.RetryParams;
import com.jforex.programming.test.common.CommonUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.functions.Action;

@RunWith(HierarchicalContextRunner.class)
public class ComposeParamsForOrderTest extends CommonUtilForTest {

    private ComposeParamsForOrder composeParamsForOrder;

    @Mock
    private Action startActionMock;
    @Mock
    private Action completeActionMock;
    @Mock
    private Consumer<Throwable> errorConsumerMock;
    @Mock
    private Function<IOrder, Action> startConsumerMock;
    @Mock
    private Function<IOrder, Action> completeConsumerMock;
    @Mock
    private BiConsumer<Throwable, IOrder> biErrorConsumerMock;
    @Mock
    private RetryParams retryParamsMock;
    private final IOrder orderForTest = buyOrderEURUSD;

    @Before
    public void setUp() {
        when(startConsumerMock.apply(orderForTest)).thenReturn(startActionMock);
        when(completeConsumerMock.apply(orderForTest)).thenReturn(completeActionMock);

        composeParamsForOrder = new ComposeParamsForOrder();
    }

    @Test
    public void defaultValuesAreCorrect() throws Exception {
        composeParamsForOrder.startAction(orderForTest).run();
        composeParamsForOrder.completeAction(orderForTest).run();
        composeParamsForOrder.errorConsumer(orderForTest).accept(jfException);

        final RetryParams retryParams = composeParamsForOrder.retryParams();
        assertThat(retryParams.noOfRetries(), equalTo(0));
        assertThat(retryParams.delayInMillis(), equalTo(0L));
    }

    public class FillParams {

        @Before
        public void setUp() {
            composeParamsForOrder.setStartAction(startConsumerMock);
            composeParamsForOrder.setCompleteAction(completeConsumerMock);
            composeParamsForOrder.setErrorConsumer(biErrorConsumerMock);
            composeParamsForOrder.setRetryParams(retryParamsMock);
        }

        @Test
        public void settersAreCorrect() throws Exception {
            assertThat(composeParamsForOrder.startAction(orderForTest), equalTo(startActionMock));
            assertThat(composeParamsForOrder.completeAction(orderForTest), equalTo(completeActionMock));
            composeParamsForOrder.errorConsumer(orderForTest).accept(jfException);
            verify(biErrorConsumerMock).accept(jfException, orderForTest);
            assertThat(composeParamsForOrder.retryParams(), equalTo(retryParamsMock));
        }

        @Test
        public void convertToComposeParamsIsCorrect() throws Exception {
            final ComposeParams composeParams = composeParamsForOrder.convertWithOrder(orderForTest);

            assertThat(composeParams.startAction(), equalTo(startActionMock));
            assertThat(composeParams.completeAction(), equalTo(completeActionMock));
            composeParams.errorConsumer().accept(jfException);
            verify(biErrorConsumerMock).accept(jfException, orderForTest);
            assertThat(composeParams.retryParams(), equalTo(composeParamsForOrder.retryParams()));
        }
    }
}
