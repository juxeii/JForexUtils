package com.jforex.programming.order.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Set;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.google.common.collect.Sets;
import com.jforex.programming.order.OrderUtil;
import com.jforex.programming.order.OrderUtilBuilder;
import com.jforex.programming.order.command.CloseCommand;
import com.jforex.programming.order.command.MergeCommand;
import com.jforex.programming.order.command.option.CloseOption;
import com.jforex.programming.order.command.option.MergeOption;
import com.jforex.programming.order.command.option.SetAmountOption;
import com.jforex.programming.order.command.option.SetGTTOption;
import com.jforex.programming.order.command.option.SetLabelOption;
import com.jforex.programming.order.command.option.SetOpenPriceOption;
import com.jforex.programming.order.command.option.SetSLOption;
import com.jforex.programming.order.command.option.SetTPOption;
import com.jforex.programming.order.command.option.SubmitOption;
import com.jforex.programming.position.PositionOrders;
import com.jforex.programming.position.PositionUtil;
import com.jforex.programming.test.common.InstrumentUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import rx.Completable;

@RunWith(HierarchicalContextRunner.class)
public class OrderUtilTest extends InstrumentUtilForTest {

    private OrderUtil orderUtil;

    @Mock
    private OrderUtilBuilder orderUtilBuilderMock;
    @Mock
    private PositionUtil positionUtilMock;
    @Mock
    private Completable completableMock;
    @Mock
    private Function<Set<IOrder>, MergeCommand> mergeCommandFactory;
    @Mock
    private Function<IOrder, CloseCommand> closeCommandFactory;

    @Before
    public void setUp() {
        orderUtil = new OrderUtil(orderUtilBuilderMock, positionUtilMock);
    }

    @Test
    public void submitBuilderCallIsInvokingOnBuilderUtil() {
        final SubmitOption submitBuilderOptionMock = mock(SubmitOption.class);
        when(orderUtilBuilderMock.submitBuilder(buyParamsEURUSD))
            .thenReturn(submitBuilderOptionMock);

        final SubmitOption submitOption = orderUtil.submitBuilder(buyParamsEURUSD);

        verify(orderUtilBuilderMock).submitBuilder(buyParamsEURUSD);
        assertThat(submitOption, equalTo(submitBuilderOptionMock));
    }

    @Test
    public void mergeBuilderCallIsInvokingOnBuilderUtil() {
        final String mergeOrderLabel = "mergeOrderLabel";
        final Set<IOrder> toMergeOrders = Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD);
        final MergeOption mergeBuilderOptionMock = mock(MergeOption.class);
        when(orderUtilBuilderMock.mergeBuilder(mergeOrderLabel, toMergeOrders))
            .thenReturn(mergeBuilderOptionMock);

        final MergeOption mergeOption = orderUtil.mergeBuilder(mergeOrderLabel, toMergeOrders);

        verify(orderUtilBuilderMock).mergeBuilder(mergeOrderLabel, toMergeOrders);
        assertThat(mergeOption, equalTo(mergeBuilderOptionMock));
    }

    @Test
    public void closeBuilderCallIsInvokingOnBuilderUtil() {
        final CloseOption closeBuilderOptionMock = mock(CloseOption.class);
        when(orderUtilBuilderMock.closeBuilder(buyOrderEURUSD))
            .thenReturn(closeBuilderOptionMock);

        final CloseOption closeOption = orderUtil.closeBuilder(buyOrderEURUSD);

        verify(orderUtilBuilderMock).closeBuilder(buyOrderEURUSD);
        assertThat(closeOption, equalTo(closeBuilderOptionMock));
    }

    @Test
    public void setLabelBuilderCallIsInvokingOnBuilderUtil() {
        final String newLabel = "newLabel";
        final SetLabelOption setLabelBuilderOptionMock = mock(SetLabelOption.class);
        when(orderUtilBuilderMock.setLabelBuilder(buyOrderEURUSD, newLabel))
            .thenReturn(setLabelBuilderOptionMock);

        final SetLabelOption setLabelOption = orderUtil.setLabelBuilder(buyOrderEURUSD, newLabel);

        verify(orderUtilBuilderMock).setLabelBuilder(buyOrderEURUSD, newLabel);
        assertThat(setLabelOption, equalTo(setLabelBuilderOptionMock));
    }

    @Test
    public void setGTTBuilderCallIsInvokingOnBuilderUtil() {
        final long newGTT = 1L;
        final SetGTTOption setGTTBuilderOptionMock = mock(SetGTTOption.class);
        when(orderUtilBuilderMock.setGTTBuilder(buyOrderEURUSD, newGTT))
            .thenReturn(setGTTBuilderOptionMock);

        final SetGTTOption setGTTOption = orderUtil.setGTTBuilder(buyOrderEURUSD, newGTT);

        verify(orderUtilBuilderMock).setGTTBuilder(buyOrderEURUSD, newGTT);
        assertThat(setGTTOption, equalTo(setGTTBuilderOptionMock));
    }

    @Test
    public void setAmountBuilderCallIsInvokingOnBuilderUtil() {
        final double newAmount = 0.12;
        final SetAmountOption setAmountBuilderOptionMock = mock(SetAmountOption.class);
        when(orderUtilBuilderMock.setAmountBuilder(buyOrderEURUSD, newAmount))
            .thenReturn(setAmountBuilderOptionMock);

        final SetAmountOption setAmountOption = orderUtil.setAmountBuilder(buyOrderEURUSD, newAmount);

        verify(orderUtilBuilderMock).setAmountBuilder(buyOrderEURUSD, newAmount);
        assertThat(setAmountOption, equalTo(setAmountBuilderOptionMock));
    }

    @Test
    public void setOpenPriceBuilderCallIsInvokingOnBuilderUtil() {
        final double newOpenPrice = 1.234;
        final SetOpenPriceOption setOpenPriceBuilderOptionMock = mock(SetOpenPriceOption.class);
        when(orderUtilBuilderMock.setOpenPriceBuilder(buyOrderEURUSD, newOpenPrice))
            .thenReturn(setOpenPriceBuilderOptionMock);

        final SetOpenPriceOption setOpenPriceOption = orderUtil.setOpenPriceBuilder(buyOrderEURUSD, newOpenPrice);

        verify(orderUtilBuilderMock).setOpenPriceBuilder(buyOrderEURUSD, newOpenPrice);
        assertThat(setOpenPriceOption, equalTo(setOpenPriceBuilderOptionMock));
    }

    @Test
    public void setSLBuilderCallIsInvokingOnBuilderUtil() {
        final double newSL = 1.234;
        final SetSLOption setSLBuilderOptionMock = mock(SetSLOption.class);
        when(orderUtilBuilderMock.setSLBuilder(buyOrderEURUSD, newSL))
            .thenReturn(setSLBuilderOptionMock);

        final SetSLOption setSLOption = orderUtil.setSLBuilder(buyOrderEURUSD, newSL);

        verify(orderUtilBuilderMock).setSLBuilder(buyOrderEURUSD, newSL);
        assertThat(setSLOption, equalTo(setSLBuilderOptionMock));
    }

    @Test
    public void setTPBuilderCallIsInvokingOnBuilderUtil() {
        final double newTP = 1.234;
        final SetTPOption setTPBuilderOptionMock = mock(SetTPOption.class);
        when(orderUtilBuilderMock.setTPBuilder(buyOrderEURUSD, newTP))
            .thenReturn(setTPBuilderOptionMock);

        final SetTPOption setTPOption = orderUtil.setTPBuilder(buyOrderEURUSD, newTP);

        verify(orderUtilBuilderMock).setTPBuilder(buyOrderEURUSD, newTP);
        assertThat(setTPOption, equalTo(setTPBuilderOptionMock));
    }

    @Test
    public void mergePositionCallIsInvokingOnPositionUtil() {
        when(positionUtilMock.merge(instrumentEURUSD, mergeCommandFactory))
            .thenReturn(completableMock);

        final Completable completable = orderUtil.mergePosition(instrumentEURUSD, mergeCommandFactory);

        verify(positionUtilMock).merge(instrumentEURUSD, mergeCommandFactory);
        assertThat(completable, equalTo(completableMock));
    }

    @Test
    public void mergeAllPositionsCallIsInvokingOnPositionUtil() {
        when(positionUtilMock.mergeAll(mergeCommandFactory))
            .thenReturn(completableMock);

        final Completable completable = orderUtil.mergeAllPositions(mergeCommandFactory);

        verify(positionUtilMock).mergeAll(mergeCommandFactory);
        assertThat(completable, equalTo(completableMock));
    }

    @Test
    public void closePositionCallIsInvokingOnPositionUtil() {
        when(positionUtilMock.close(instrumentEURUSD,
                                            mergeCommandFactory,
                                            closeCommandFactory))
                                                .thenReturn(completableMock);

        final Completable completable = orderUtil.closePosition(instrumentEURUSD,
                                                                mergeCommandFactory,
                                                                closeCommandFactory);

        verify(positionUtilMock).close(instrumentEURUSD,
                                               mergeCommandFactory,
                                               closeCommandFactory);
        assertThat(completable, equalTo(completableMock));
    }

    @Test
    public void closeAllPositionsCallIsInvokingOnPositionUtil() {
        when(positionUtilMock.closeAll(mergeCommandFactory, closeCommandFactory))
            .thenReturn(completableMock);

        final Completable completable = orderUtil.closeAllPositions(mergeCommandFactory,
                                                                    closeCommandFactory);

        verify(positionUtilMock).closeAll(mergeCommandFactory,
                                                   closeCommandFactory);
        assertThat(completable, equalTo(completableMock));
    }

    @Test
    public void positionOrdersReturnsCorrectInstance() {
        final PositionOrders positionOrdersMock = mock(PositionOrders.class);
        when(positionUtilMock.positionOrders(instrumentEURUSD))
            .thenReturn(positionOrdersMock);

        final PositionOrders positionOrders = orderUtil.positionOrders(instrumentEURUSD);

        assertThat(positionOrders, equalTo(positionOrdersMock));
    }
}
