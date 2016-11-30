package com.jforex.programming.order.task.params.position.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.task.params.ComposeData;
import com.jforex.programming.order.task.params.RetryParams;
import com.jforex.programming.order.task.params.position.CloseAllPositionsParams;
import com.jforex.programming.order.task.params.position.ClosePositionParams;
import com.jforex.programming.order.task.params.test.CommonParamsForTest;

import io.reactivex.functions.Action;

public class CloseAllPositionsParamsTest extends CommonParamsForTest {

    private CloseAllPositionsParams closeAllPositionsParams;

    @Mock
    private ClosePositionParams closePositionParamsMock;
    @Mock
    private Action actionMock;
    @Mock
    private Function<IOrder, Action> actionConsumerMock;
    @Mock
    private Consumer<Throwable> errorConsumerMock;
    @Mock
    private BiConsumer<Throwable, IOrder> biErrorConsumerMock;
    @Mock
    private Consumer<OrderEvent> eventConsumerMock;
    @Mock
    private Function<Instrument, ClosePositionParams> paramsFactoryMock;
    private final IOrder orderForTest = buyOrderEURUSD;
    private static final int noOfRetries = 3;
    private static final long delayInMillis = 1500L;

    @Before
    public void setUp() {
        when(actionConsumerMock.apply(orderForTest)).thenReturn(actionMock);
        when(paramsFactoryMock.apply(instrumentEURUSD)).thenReturn(closePositionParamsMock);

        closeAllPositionsParams = CloseAllPositionsParams
            .newBuilder(paramsFactoryMock)

            .doOnCloseAllPositionsStart(actionMock)
            .doOnCloseAllPositionsComplete(actionMock)
            .doOnCloseAllPositionsError(errorConsumerMock)
            .retryOnCloseAllPositionsReject(noOfRetries, delayInMillis)

            .build();
    }

    private void assertComposeParams(final ComposeData composeData) {
        assertActions(composeData);
        assertErrorConsumer(composeData.errorConsumer());
        assertRetries(composeData.retryParams());
    }

    private void assertActions(final ComposeData composeData) {
        assertThat(composeData.startAction(), equalTo(actionMock));
        assertThat(composeData.completeAction(), equalTo(actionMock));
    }

    private void assertErrorConsumer(final Consumer<Throwable> errorConsumer) {
        assertThat(errorConsumer, equalTo(errorConsumerMock));
    }

    private void assertRetries(final RetryParams retryParams) {
        assertThat(retryParams.noOfRetries(), equalTo(noOfRetries));
        assertThat(retryParams.delayInMillis(), equalTo(delayInMillis));
    }

    @Test
    public void defaultValuesAreCorrect() {
        closeAllPositionsParams = CloseAllPositionsParams
            .newBuilder(paramsFactoryMock)
            .build();

        assertThat(closeAllPositionsParams.paramsForInstrument(instrumentEURUSD), equalTo(closePositionParamsMock));
    }

    @Test
    public void assertSpecifiedValues() {
        assertThat(closeAllPositionsParams.paramsForInstrument(instrumentEURUSD), equalTo(closePositionParamsMock));
    }

    @Test
    public void assertMergeAllPositionsValues() {
        assertComposeParams(closeAllPositionsParams.closeAllPositionsComposeData());
    }
}
