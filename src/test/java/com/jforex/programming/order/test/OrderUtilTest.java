package com.jforex.programming.order.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.google.common.collect.Sets;
import com.jforex.programming.order.OrderParams;
import com.jforex.programming.order.OrderPositionHandler;
import com.jforex.programming.order.OrderUtil;
import com.jforex.programming.order.OrderUtilHandler;
import com.jforex.programming.order.command.CloseCommand;
import com.jforex.programming.order.command.MergeCommand;
import com.jforex.programming.order.command.MergePositionCommand;
import com.jforex.programming.order.command.OrderCallCommand;
import com.jforex.programming.order.command.SetAmountCommand;
import com.jforex.programming.order.command.SetGTTCommand;
import com.jforex.programming.order.command.SetLabelCommand;
import com.jforex.programming.order.command.SetOpenPriceCommand;
import com.jforex.programming.order.command.SetSLCommand;
import com.jforex.programming.order.command.SetTPCommand;
import com.jforex.programming.order.command.SubmitCommand;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.position.Position;
import com.jforex.programming.position.PositionFactory;
import com.jforex.programming.position.PositionOrders;
import com.jforex.programming.position.RestoreSLTPPolicy;
import com.jforex.programming.test.common.InstrumentUtilForTest;
import com.jforex.programming.test.common.OrderParamsForTest;
import com.jforex.programming.test.fakes.IOrderForTest;

import rx.Observable;
import rx.observers.TestSubscriber;

public class OrderUtilTest extends InstrumentUtilForTest {

    private OrderUtil orderUtil;

    @Mock
    private OrderUtilHandler orderUtilHandlerMock;
    @Mock
    private OrderPositionHandler orderPositionHandlerMock;
    @Mock
    private RestoreSLTPPolicy restoreSLTPPolicyMock;
    @Mock
    private PositionOrders positionOrdersMock;
    @Mock
    private PositionFactory positionFactoryMock;
    @Mock
    private Position positionMock;
    private final TestSubscriber<OrderEvent> orderEventSubscriber = new TestSubscriber<>();
    private final IOrderForTest orderToChange = IOrderForTest.buyOrderEURUSD();
    private final Set<IOrder> toMergeOrders = Sets.newHashSet(IOrderForTest.buyOrderEURUSD(),
                                                              IOrderForTest.sellOrderEURUSD());
    private final String mergeOrderLabel = "MergeLabel";

    @Before
    public void setUp() {
        setUpMocks();

        orderUtil = new OrderUtil(engineMock,
                                  positionFactoryMock,
                                  orderPositionHandlerMock,
                                  orderUtilHandlerMock);
    }

    public void setUpMocks() {
        when(positionFactoryMock.forInstrument(instrumentEURUSD))
                .thenReturn(positionMock);
    }

    private void expectOnOrderUtilHadler(final Class<? extends OrderCallCommand> clazz) {
        when(orderUtilHandlerMock.callObservable(any(clazz)))
                .thenReturn(Observable.empty());
    }

    private void verifyOnOrderUtilHadler(final Class<? extends OrderCallCommand> clazz) {
        verify(orderUtilHandlerMock).callObservable(any(clazz));
        orderEventSubscriber.assertCompleted();
    }

    @Test
    public void positionOrdersReturnsCorrectInstance() {
        assertThat(orderUtil.positionOrders(instrumentEURUSD),
                   equalTo(positionMock));
    }

    @Test
    public void submitCallsOnPositionHandler() {
        final OrderParams orderParams = OrderParamsForTest.paramsBuyEURUSD();
        when(orderPositionHandlerMock.submitOrder(any(SubmitCommand.class)))
                .thenReturn(Observable.empty());

        orderUtil
                .submitOrder(orderParams)
                .subscribe(orderEventSubscriber);

        verify(orderPositionHandlerMock).submitOrder(any(SubmitCommand.class));
        orderEventSubscriber.assertCompleted();
    }

    @Test
    public void mergeCallsOnPositionHandler() {
        when(orderPositionHandlerMock.mergeOrders(any(MergeCommand.class)))
                .thenReturn(Observable.empty());

        orderUtil
                .mergeOrders(mergeOrderLabel, toMergeOrders)
                .subscribe(orderEventSubscriber);

        verify(orderPositionHandlerMock).mergeOrders(any(MergeCommand.class));
        orderEventSubscriber.assertCompleted();
    }

    @Test
    public void mergePositionCallsOnPositionHandler() {
        when(orderPositionHandlerMock.mergePositionOrders(any(MergePositionCommand.class)))
                .thenReturn(Observable.empty());
        when(positionMock.filled()).thenReturn(toMergeOrders);

        orderUtil.mergePositionOrders(mergeOrderLabel, instrumentEURUSD, restoreSLTPPolicyMock)
                .subscribe(orderEventSubscriber);

        verify(orderPositionHandlerMock)
                .mergePositionOrders(any(MergePositionCommand.class));
        orderEventSubscriber.assertCompleted();
    }

    @Test
    public void mergePositionReturnsEmptyObservale() {
        when(orderPositionHandlerMock.positionOrders(instrumentEURUSD))
                .thenReturn(positionOrdersMock);
        when(positionOrdersMock.filled())
                .thenReturn(Sets.newHashSet());

        orderUtil.mergePositionOrders(mergeOrderLabel, instrumentEURUSD, restoreSLTPPolicyMock)
                .subscribe(orderEventSubscriber);

        orderEventSubscriber.assertValueCount(0);
        orderEventSubscriber.assertCompleted();
    }

    @Test
    public void closePositionCallsOnPositionHandler() {
        when(orderPositionHandlerMock.closePosition(instrumentEURUSD))
                .thenReturn(Observable.empty().toCompletable());

        orderUtil
                .closePosition(instrumentEURUSD)
                .subscribe(orderEventSubscriber);

        verify(orderPositionHandlerMock).closePosition(instrumentEURUSD);
        orderEventSubscriber.assertCompleted();
    }

    @Test
    public void closeCallsOnOrderUtilHandler() {
        expectOnOrderUtilHadler(CloseCommand.class);

        orderUtil
                .close(orderToChange)
                .subscribe(orderEventSubscriber);

        verifyOnOrderUtilHadler(CloseCommand.class);
    }

    @Test
    public void setLabelCallsOnOrderUtilHandler() {
        final String newLabel = "NewLabel";
        expectOnOrderUtilHadler(SetLabelCommand.class);

        orderUtil
                .setLabel(orderToChange, newLabel)
                .subscribe(orderEventSubscriber);

        verifyOnOrderUtilHadler(SetLabelCommand.class);
    }

    @Test
    public void setGTTCallsOnOrderUtilHandler() {
        final long newGTT = 123456L;
        expectOnOrderUtilHadler(SetGTTCommand.class);

        orderUtil
                .setGoodTillTime(orderToChange, newGTT)
                .subscribe(orderEventSubscriber);

        verifyOnOrderUtilHadler(SetGTTCommand.class);
    }

    @Test
    public void setOpenPriceCallsOnOrderUtilHandler() {
        final double newOpenPrice = 1.12122;
        expectOnOrderUtilHadler(SetOpenPriceCommand.class);

        orderUtil
                .setOpenPrice(orderToChange, newOpenPrice)
                .subscribe(orderEventSubscriber);

        verifyOnOrderUtilHadler(SetOpenPriceCommand.class);
    }

    @Test
    public void setRequestedAmountCallsOnOrderUtilHandler() {
        final double newRequestedAmount = 0.12;
        expectOnOrderUtilHadler(SetAmountCommand.class);

        orderUtil
                .setRequestedAmount(orderToChange, newRequestedAmount)
                .subscribe(orderEventSubscriber);

        verifyOnOrderUtilHadler(SetAmountCommand.class);
    }

    @Test
    public void setStopLossPriceCallsOnOrderUtilHandler() {
        final double newSL = 1.10987;
        expectOnOrderUtilHadler(SetSLCommand.class);

        orderUtil
                .setStopLossPrice(orderToChange, newSL)
                .subscribe(orderEventSubscriber);

        verifyOnOrderUtilHadler(SetSLCommand.class);
    }

    @Test
    public void setTakeProfitPriceCallsOnOrderUtilHandler() {
        final double newTP = 1.11001;
        expectOnOrderUtilHadler(SetTPCommand.class);

        orderUtil
                .setTakeProfitPrice(orderToChange, newTP)
                .subscribe(orderEventSubscriber);

        verifyOnOrderUtilHadler(SetTPCommand.class);
    }
}
