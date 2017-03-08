package com.jforex.programming.order.task.params.basic.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.function.Consumer;

import org.junit.Test;
import org.mockito.Mock;

import com.dukascopy.api.IEngine.OrderCommand;
import com.dukascopy.api.OfferSide;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.task.params.SetSLTPMode;
import com.jforex.programming.order.task.params.TaskParamsType;
import com.jforex.programming.order.task.params.basic.SetSLParams;
import com.jforex.programming.order.task.params.test.CommonParamsForTest;

public class SetSLParamsTest extends CommonParamsForTest {

    private SetSLParams setSLParams;

    @Mock
    public Consumer<OrderEvent> changedSLConsumerMock;
    @Mock
    public Consumer<OrderEvent> changeRejectConsumerMock;
    private final double newSL = 1.1234;

    @Test
    public void bidIsOfferSideWhenOrderIsLong() {
        orderUtilForTest.setOrderCommand(buyOrderEURUSD, OrderCommand.BUY);

        setSLParams = SetSLParams
            .setSLAtPrice(buyOrderEURUSD, newSL)
            .build();

        assertThat(setSLParams.offerSide(), equalTo(OfferSide.BID));
    }

    @Test
    public void askIsOfferSideWhenOrderIsShort() {
        orderUtilForTest.setOrderCommand(buyOrderEURUSD, OrderCommand.SELL);

        setSLParams = SetSLParams
            .setSLAtPrice(buyOrderEURUSD, newSL)
            .build();

        assertThat(setSLParams.offerSide(), equalTo(OfferSide.ASK));
    }

    @Test
    public void trailingStepIsZeroWhenNotSpecified() {
        setSLParams = SetSLParams
            .setSLAtPrice(buyOrderEURUSD, newSL)
            .build();

        assertThat(setSLParams.trailingStep(), equalTo(0.0));
    }

    @Test
    public void valuesWithPriceAreCorrect() {
        final double trailingStep = 11.1;

        setSLParams = SetSLParams
            .setSLAtPrice(buyOrderEURUSD, newSL)
            .withOfferSide(OfferSide.ASK)
            .withTrailingStep(11.1)
            .build();

        assertThat(setSLParams.order(), equalTo(buyOrderEURUSD));
        assertThat(setSLParams.priceOrPips(), equalTo(newSL));
        assertThat(setSLParams.offerSide(), equalTo(OfferSide.ASK));
        assertThat(setSLParams.trailingStep(), equalTo(trailingStep));
    }

    @Test
    public void valuesWithPipsAreCorrect() {
        final double pipsToSL = 12.3;

        setSLParams = SetSLParams
            .setSLWithPips(buyOrderEURUSD, pipsToSL)
            .build();

        assertThat(setSLParams.order(), equalTo(buyOrderEURUSD));
        assertThat(setSLParams.priceOrPips(), equalTo(pipsToSL));
        assertThat(setSLParams.setSLTPMode(), equalTo(SetSLTPMode.PIPS));
    }

    @Test
    public void handlersAreCorrect() {
        setSLParams = SetSLParams
            .setSLAtPrice(buyOrderEURUSD, 1.1234)
            .doOnChangedSL(changedSLConsumerMock)
            .doOnReject(changeRejectConsumerMock)
            .build();

        consumerForEvent = setSLParams
            .composeData()
            .consumerByEventType();

        assertThat(setSLParams.type(), equalTo(TaskParamsType.SETSL));
        assertThat(consumerForEvent.size(), equalTo(2));
        assertEventConsumer(OrderEventType.CHANGED_SL, changedSLConsumerMock);
        assertEventConsumer(OrderEventType.CHANGE_SL_REJECTED, changeRejectConsumerMock);
    }
}
