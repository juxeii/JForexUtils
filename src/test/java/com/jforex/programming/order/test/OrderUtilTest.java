package com.jforex.programming.order.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.google.common.collect.Sets;
import com.jforex.programming.order.OrderUtil;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.task.BasicTaskObservable;
import com.jforex.programming.order.task.ClosePositionTaskObservable;
import com.jforex.programming.order.task.MergePositionTaskObservable;
import com.jforex.programming.order.task.params.TaskParamsUtil;
import com.jforex.programming.order.task.params.basic.CloseParams;
import com.jforex.programming.order.task.params.basic.MergeParams;
import com.jforex.programming.order.task.params.basic.SetAmountParams;
import com.jforex.programming.order.task.params.basic.SetGTTParams;
import com.jforex.programming.order.task.params.basic.SetLabelParams;
import com.jforex.programming.order.task.params.basic.SetOpenPriceParams;
import com.jforex.programming.order.task.params.basic.SetSLParams;
import com.jforex.programming.order.task.params.basic.SetTPParams;
import com.jforex.programming.order.task.params.basic.SubmitParams;
import com.jforex.programming.order.task.params.position.MergePositionParams;
import com.jforex.programming.order.task.params.position.SimpleClosePositionParams;
import com.jforex.programming.position.PositionOrders;
import com.jforex.programming.position.PositionUtil;
import com.jforex.programming.test.common.InstrumentUtilForTest;

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Observable;
import io.reactivex.functions.Function;

@RunWith(HierarchicalContextRunner.class)
public class OrderUtilTest extends InstrumentUtilForTest {

    private OrderUtil orderUtil;

    @Mock
    private BasicTaskObservable basicTaskMock;
    @Mock
    private MergePositionTaskObservable orderMergeTaskMock;
    @Mock
    private ClosePositionTaskObservable orderCloseTaskMock;
    @Mock
    private PositionUtil positionUtilMock;
    @Mock
    private TaskParamsUtil taskParamsUtilMock;
    @Mock
    private MergePositionParams mergepositionParamsMock;
    @Mock
    private Function<Instrument, MergePositionParams> mergePositionParamsFactory;
    @Mock
    private Function<Instrument, SimpleClosePositionParams> closePositionParamsFactory;
    private final IOrder orderForTest = buyOrderEURUSD;
    private Observable<OrderEvent> orderEventObservable;
    private final Set<IOrder> toMergeOrders = Sets.newHashSet(buyOrderEURUSD, sellOrderEURUSD);

    @Before
    public void setUp() {
        orderUtil = new OrderUtil(basicTaskMock,
                                  orderMergeTaskMock,
                                  orderCloseTaskMock,
                                  positionUtilMock,
                                  taskParamsUtilMock);
    }

    @Test
    public void submitOrderCallsSubscribeOnTaskParams() {
        final SubmitParams submitParamsMock = mock(SubmitParams.class);

        orderUtil.submitOrder(submitParamsMock);

        verify(taskParamsUtilMock).subscribeBasicParams(basicTaskMock.submitOrder(submitParamsMock),
                                                        submitParamsMock);
    }

    @Test
    public void mergeOrdersCallsSubscribeOnTaskParams() {
        final MergeParams mergeParamsMock = mock(MergeParams.class);

        orderUtil.mergeOrders(mergeParamsMock);

        verify(taskParamsUtilMock).subscribeBasicParams(basicTaskMock.mergeOrders(mergeParamsMock),
                                                        mergeParamsMock);
    }

    @Test
    public void closeCallsSubscribeOnTaskParams() {
        final CloseParams closeParamsMock = mock(CloseParams.class);

        orderUtil.close(closeParamsMock);

        verify(taskParamsUtilMock).subscribeBasicParams(basicTaskMock.close(closeParamsMock),
                                                        closeParamsMock);
    }

    @Test
    public void setLabelDelegatesToOrderTask() {
        final SetLabelParams setLabelParamsMock = mock(SetLabelParams.class);

        orderUtil.setLabel(setLabelParamsMock);

        verify(taskParamsUtilMock).subscribeBasicParams(basicTaskMock.setLabel(setLabelParamsMock),
                                                        setLabelParamsMock);
    }

    @Test
    public void setGTTCallsSubscribeOnSetGTTParams() {
        final SetGTTParams setGTTParamsMock = mock(SetGTTParams.class);

        orderUtil.setGoodTillTime(setGTTParamsMock);

        verify(taskParamsUtilMock).subscribeBasicParams(basicTaskMock.setGoodTillTime(setGTTParamsMock),
                                                        setGTTParamsMock);
    }

    @Test
    public void setRequestedAmountCallsSubscribeOnSetAmountParams() {
        final SetAmountParams setAmountParamsMock = mock(SetAmountParams.class);

        orderUtil.setRequestedAmount(setAmountParamsMock);

        verify(taskParamsUtilMock).subscribeBasicParams(basicTaskMock.setRequestedAmount(setAmountParamsMock),
                                                        setAmountParamsMock);
    }

    @Test
    public void setOpenPriceCallsSubscribeOnSetOpenPriceParams() {
        final SetOpenPriceParams setOpenPriceParamsMock = mock(SetOpenPriceParams.class);

        orderUtil.setOpenPrice(setOpenPriceParamsMock);

        verify(taskParamsUtilMock).subscribeBasicParams(basicTaskMock.setOpenPrice(setOpenPriceParamsMock),
                                                        setOpenPriceParamsMock);
    }

    @Test
    public void setSLCallsSubscribeOnSetSLParams() {
        final SetSLParams setSLParamsMock = mock(SetSLParams.class);

        orderUtil.setStopLossPrice(setSLParamsMock);

        verify(taskParamsUtilMock).subscribeBasicParams(basicTaskMock.setStopLossPrice(setSLParamsMock),
                                                        setSLParamsMock);
    }

    @Test
    public void setTPCallsSubscribeOnSetTPParams() {
        final SetTPParams setTPParamsMock = mock(SetTPParams.class);

        orderUtil.setTakeProfitPrice(setTPParamsMock);

        verify(taskParamsUtilMock).subscribeBasicParams(basicTaskMock.setTakeProfitPrice(setTPParamsMock),
                                                        setTPParamsMock);
    }

    // @Test
    // public void mergePositionDelegatesToMergeTask() {
    // when(orderMergeTaskMock.mergePosition(instrumentEURUSD,
    // mergepositionParamsMock))
    // .thenReturn(orderEventObservable);
    //
    // final Observable<OrderEvent> actualObservable =
    // orderUtil.mergePosition(instrumentEURUSD, mergepositionParamsMock);
    //
    // verify(orderMergeTaskMock).mergePosition(instrumentEURUSD,
    // mergepositionParamsMock);
    // assertThat(actualObservable, equalTo(orderEventObservable));
    // }
    //
    // @Test
    // public void mergeAllPositionsDelegatesToMergeTask() {
    // when(orderMergeTaskMock.mergeAll(mergePositionParamsFactory))
    // .thenReturn(orderEventObservable);
    //
    // final Observable<OrderEvent> actualObservable =
    // orderUtil.mergeAllPositions(mergePositionParamsFactory);
    //
    // verify(orderMergeTaskMock).mergeAll(mergePositionParamsFactory);
    // assertThat(actualObservable, equalTo(orderEventObservable));
    // }
    //
    // @Test
    // public void closePositionDelegatesToCloseTask() {
    // final SimpleClosePositionParams positionParams =
    // mock(SimpleClosePositionParams.class);
    //
    // when(orderCloseTaskMock.close(positionParams))
    // .thenReturn(orderEventObservable);
    //
    // final Observable<OrderEvent> actualObservable =
    // orderUtil.closePosition(positionParams);
    //
    // verify(orderCloseTaskMock).close(positionParams);
    // assertThat(actualObservable, equalTo(orderEventObservable));
    // }
    //
    // @Test
    // public void closeAllPositionsDelegatesToCloseTask() {
    // when(orderCloseTaskMock.closeAll(closePositionParamsFactory))
    // .thenReturn(orderEventObservable);
    //
    // final Observable<OrderEvent> actualObservable =
    // orderUtil.closeAll(closePositionParamsFactory);
    //
    // verify(orderCloseTaskMock).closeAll(closePositionParamsFactory);
    // assertThat(actualObservable, equalTo(orderEventObservable));
    // }

    @Test
    public void positionOrdersDelegatesToPositionTask() {
        final PositionOrders positionOrders = mock(PositionOrders.class);
        when(positionUtilMock.positionOrders(instrumentEURUSD))
            .thenReturn(positionOrders);

        final PositionOrders actualPositionOrders = orderUtil.positionOrders(instrumentEURUSD);

        verify(positionUtilMock).positionOrders(instrumentEURUSD);
        assertThat(actualPositionOrders, equalTo(positionOrders));
    }
}
