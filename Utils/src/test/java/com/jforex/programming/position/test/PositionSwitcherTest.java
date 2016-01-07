package com.jforex.programming.position.test;

import static com.jforex.programming.misc.JForexUtil.uss;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import com.jforex.programming.misc.MathUtil;
import com.jforex.programming.order.OrderDirection;
import com.jforex.programming.order.OrderParams;
import com.jforex.programming.order.OrderParamsSupplier;
import com.jforex.programming.position.Position;
import com.jforex.programming.position.PositionSwitcher;
import com.jforex.programming.test.common.InstrumentUtilForTest;
import com.jforex.programming.test.common.OrderParamsForTest;

import com.dukascopy.api.IEngine.OrderCommand;

public class PositionSwitcherTest extends InstrumentUtilForTest {

    private PositionSwitcher positionSwitcher;

    @Mock private Position position;
    @Mock private OrderParamsSupplier orderParamsSupplierMock;
    @Captor private ArgumentCaptor<OrderParams> orderParamsCaptor;
    private final OrderParams orderParamsBUY = OrderParamsForTest.paramsBuyEURUSD();
    private final OrderParams orderParamsSELL = OrderParamsForTest.paramsSellEURUSD();
    private final String buyMergeLabel = uss.ORDER_MERGE_LABEL_PREFIX() + orderParamsBUY.label();
    private final String sellMergeLabel = uss.ORDER_MERGE_LABEL_PREFIX() + orderParamsSELL.label();

    @Before
    public void setUp() {
        initCommonTestFramework();
        setUpMocks();

        positionSwitcher = new PositionSwitcher(position, orderParamsSupplierMock);
    }

    private void setUpMocks() {
        when(orderParamsSupplierMock.forCommand(OrderCommand.BUY)).thenReturn(orderParamsBUY);
        when(orderParamsSupplierMock.forCommand(OrderCommand.SELL)).thenReturn(orderParamsSELL);
    }

    private void verifyNoPositionCommands() {
        verify(position, never()).submitAndMerge(any(), any());
        verify(position, never()).submit(any());
        verify(position, never()).merge(any());
        verify(position, never()).close();
    }

    private void verifySendedOrderParamsAreCorrect(final String mergeLabel,
                                                   final double expectedAmount,
                                                   final OrderCommand expectedCommand) {
        verify(position).submitAndMerge(orderParamsCaptor.capture(), eq(mergeLabel));
        final OrderParams sendedOrderParams = orderParamsCaptor.getValue();
        assertThat(sendedOrderParams.amount(), equalTo(expectedAmount));
        assertThat(sendedOrderParams.orderCommand(), equalTo(expectedCommand));
    }

    private void setPositionManagerState(final OrderDirection orderDirection,
                                         final boolean isBusy) {
        when(position.direction()).thenReturn(orderDirection);
        when(position.isBusy()).thenReturn(isBusy);
    }

    private double expectedAmount(final double signedExposure,
                                  final OrderParams orderParams) {
        final double expectedAmount = MathUtil.roundAmount(orderParams.amount() + Math.abs(signedExposure));
        when(position.signedExposure()).thenReturn(signedExposure);
        return expectedAmount;
    }

    @Test
    public void testSendFlatSignalWhenPositionIsFlatDoesNotCallPositionCommands() {
        setPositionManagerState(OrderDirection.FLAT, false);

        positionSwitcher.sendFlatSignal();

        verifyNoPositionCommands();
    }

    @Test
    public void testSendBuySignalWhenPositionIsFlatDoesCallSubmitOnPositionManager() {
        setPositionManagerState(OrderDirection.FLAT, false);

        positionSwitcher.sendBuySignal();

        verifySendedOrderParamsAreCorrect(buyMergeLabel, orderParamsBUY.amount(), OrderCommand.BUY);
    }

    @Test
    public void testSendSellSignalWhenPositionIsFlatDoesCallSubmitOnPositionManager() {
        setPositionManagerState(OrderDirection.FLAT, false);

        positionSwitcher.sendSellSignal();

        verifySendedOrderParamsAreCorrect(sellMergeLabel, orderParamsSELL.amount(), OrderCommand.SELL);
    }

    @Test
    public void testSendFlatSignalWhenPositionIsBusyDoesNotCallPositionCommands() {
        setPositionManagerState(OrderDirection.LONG, true);

        positionSwitcher.sendFlatSignal();

        verifyNoPositionCommands();
    }

    @Test
    public void testSendBuySignalWhenPositionIsBusyDoesNotCallPositionCommands() {
        setPositionManagerState(OrderDirection.SHORT, true);

        positionSwitcher.sendBuySignal();

        verifyNoPositionCommands();
    }

    @Test
    public void testSendSellSignalWhenPositionIsBusyDoesNotCallPositionCommands() {
        setPositionManagerState(OrderDirection.LONG, true);

        positionSwitcher.sendSellSignal();

        verifyNoPositionCommands();
    }

    @Test
    public void testSendFlatSignalCallCloseWhenPositionIsLong() {
        setPositionManagerState(OrderDirection.LONG, false);

        positionSwitcher.sendFlatSignal();

        verify(position).close();
    }

    @Test
    public void testSendFlatSignalCallCloseWhenPositionIsShort() {
        setPositionManagerState(OrderDirection.SHORT, false);

        positionSwitcher.sendFlatSignal();

        verify(position).close();
    }

    @Test
    public void testSendBuySignalDoesNotCallPositionCommandsWhenAlreadyLong() {
        setPositionManagerState(OrderDirection.LONG, false);

        positionSwitcher.sendBuySignal();

        verifyNoPositionCommands();
    }

    @Test
    public void testSendSellSignalDoesNotCallPositionCommandsWhenAlreadyShort() {
        setPositionManagerState(OrderDirection.SHORT, false);

        positionSwitcher.sendSellSignal();

        verifyNoPositionCommands();
    }

    @Test
    public void testSendBuySignalCallsSubmitWithCorrectAmountAndCommand() {
        setPositionManagerState(OrderDirection.SHORT, false);
        final double expectedAmount = expectedAmount(-0.12, orderParamsBUY);

        positionSwitcher.sendBuySignal();

        verifySendedOrderParamsAreCorrect(buyMergeLabel, expectedAmount, OrderCommand.BUY);
    }

    @Test
    public void testSendSellSignalCallsSubmitWithCorrectAmountAndCommand() {
        setPositionManagerState(OrderDirection.LONG, false);
        final double expectedAmount = expectedAmount(0.03, orderParamsSELL);

        positionSwitcher.sendSellSignal();

        verifySendedOrderParamsAreCorrect(sellMergeLabel, expectedAmount, OrderCommand.SELL);
    }

    @Test
    public void testOrderCommandIsCorrectedEvenIfParamProviderReturnedWrongCommand() {
        final OrderParams paramsWithWrongCommand = orderParamsBUY.clone()
                                                                 .withOrderCommand(OrderCommand.SELL)
                                                                 .build();

        when(orderParamsSupplierMock.forCommand(OrderCommand.BUY)).thenReturn(paramsWithWrongCommand);
        setPositionManagerState(OrderDirection.FLAT, false);

        positionSwitcher.sendBuySignal();

        verifySendedOrderParamsAreCorrect(buyMergeLabel, orderParamsBUY.amount(), OrderCommand.BUY);
    }
}
