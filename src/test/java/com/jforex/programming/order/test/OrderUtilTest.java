package com.jforex.programming.order.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.jforex.programming.order.OrderUtil;
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
import com.jforex.programming.order.task.params.position.CloseAllPositionsParams;
import com.jforex.programming.order.task.params.position.ClosePositionParams;
import com.jforex.programming.order.task.params.position.MergeAllPositionsParams;
import com.jforex.programming.order.task.params.position.MergePositionParams;
import com.jforex.programming.position.PositionOrders;
import com.jforex.programming.position.PositionUtil;
import com.jforex.programming.test.common.InstrumentUtilForTest;

public class OrderUtilTest extends InstrumentUtilForTest {

    private OrderUtil orderUtil;

    @Mock
    private BasicTaskObservable basicTaskMock;
    @Mock
    private MergePositionTaskObservable mergePositionTaskMock;
    @Mock
    private ClosePositionTaskObservable closePositionTaskMock;
    @Mock
    private PositionUtil positionUtilMock;
    @Mock
    private TaskParamsUtil taskParamsUtilMock;

    @Before
    public void setUp() {
        orderUtil = new OrderUtil(basicTaskMock,
                                  mergePositionTaskMock,
                                  closePositionTaskMock,
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
    public void setLabelCallsSubscribeOnTaskParams() {
        final SetLabelParams setLabelParamsMock = mock(SetLabelParams.class);

        orderUtil.setLabel(setLabelParamsMock);

        verify(taskParamsUtilMock).subscribeBasicParams(basicTaskMock.setLabel(setLabelParamsMock),
                                                        setLabelParamsMock);
    }

    @Test
    public void setGTTCallsSubscribeOnTaskParams() {
        final SetGTTParams setGTTParamsMock = mock(SetGTTParams.class);

        orderUtil.setGoodTillTime(setGTTParamsMock);

        verify(taskParamsUtilMock).subscribeBasicParams(basicTaskMock.setGoodTillTime(setGTTParamsMock),
                                                        setGTTParamsMock);
    }

    @Test
    public void setRequestedAmountCallsSubscribeOnTaskParams() {
        final SetAmountParams setAmountParamsMock = mock(SetAmountParams.class);

        orderUtil.setRequestedAmount(setAmountParamsMock);

        verify(taskParamsUtilMock).subscribeBasicParams(basicTaskMock.setRequestedAmount(setAmountParamsMock),
                                                        setAmountParamsMock);
    }

    @Test
    public void setOpenPriceCallsSubscribeOnTaskParams() {
        final SetOpenPriceParams setOpenPriceParamsMock = mock(SetOpenPriceParams.class);

        orderUtil.setOpenPrice(setOpenPriceParamsMock);

        verify(taskParamsUtilMock).subscribeBasicParams(basicTaskMock.setOpenPrice(setOpenPriceParamsMock),
                                                        setOpenPriceParamsMock);
    }

    @Test
    public void setSLCallsSubscribeOnTaskParams() {
        final SetSLParams setSLParamsMock = mock(SetSLParams.class);

        orderUtil.setStopLossPrice(setSLParamsMock);

        verify(taskParamsUtilMock).subscribeBasicParams(basicTaskMock.setStopLossPrice(setSLParamsMock),
                                                        setSLParamsMock);
    }

    @Test
    public void setTPCallsSubscribeOnTaskParams() {
        final SetTPParams setTPParamsMock = mock(SetTPParams.class);

        orderUtil.setTakeProfitPrice(setTPParamsMock);

        verify(taskParamsUtilMock).subscribeBasicParams(basicTaskMock.setTakeProfitPrice(setTPParamsMock),
                                                        setTPParamsMock);
    }

    @Test
    public void mergePositionCallsSubscribeOnTaskParams() {
        final MergePositionParams mergePositionParamsMock = mock(MergePositionParams.class);

        orderUtil.mergePosition(mergePositionParamsMock);

        verify(taskParamsUtilMock)
            .subscribePositionTask(mergePositionTaskMock.merge(mergePositionParamsMock),
                                   mergePositionParamsMock);
    }

    @Test
    public void mergeAllPositionsCallsSubscribeOnTaskParams() {
        final MergeAllPositionsParams mergeAllPositionsParamsMock = mock(MergeAllPositionsParams.class);

        orderUtil.mergeAllPositions(mergeAllPositionsParamsMock);

        verify(taskParamsUtilMock)
            .subscribeToAllPositionsTask(mergePositionTaskMock.mergeAll(mergeAllPositionsParamsMock),
                                         mergeAllPositionsParamsMock);
    }

    @Test
    public void closePositionCallsSubscribeOnTaskParams() {
        final ClosePositionParams closePositionParamsMock = mock(ClosePositionParams.class);

        orderUtil.closePosition(closePositionParamsMock);

        verify(taskParamsUtilMock)
            .subscribePositionTask(closePositionTaskMock.close(closePositionParamsMock),
                                   closePositionParamsMock);
    }

    @Test
    public void closeAllPositionsCallsSubscribeOnTaskParams() {
        final CloseAllPositionsParams closeAllPositionsParamsMock = mock(CloseAllPositionsParams.class);

        orderUtil.closeAllPositions(closeAllPositionsParamsMock);

        verify(taskParamsUtilMock)
            .subscribeToAllPositionsTask(closePositionTaskMock.closeAll(closeAllPositionsParamsMock),
                                         closeAllPositionsParamsMock);
    }

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
