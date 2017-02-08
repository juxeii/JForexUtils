package com.jforex.programming.order.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import com.jforex.programming.order.OrderUtil;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.task.BasicTask;
import com.jforex.programming.order.task.ClosePositionTask;
import com.jforex.programming.order.task.MergePositionTask;
import com.jforex.programming.order.task.params.ComposeData;
import com.jforex.programming.order.task.params.ComposeParams;
import com.jforex.programming.order.task.params.TaskParamsBase;
import com.jforex.programming.order.task.params.TaskParamsType;
import com.jforex.programming.order.task.params.TaskParamsUtil;
import com.jforex.programming.order.task.params.basic.BatchParams;
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

import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

public class OrderUtilTest extends InstrumentUtilForTest {

    private OrderUtil orderUtil;

    @Mock
    private BasicTask basicTaskMock;
    @Mock
    private MergePositionTask mergePositionTaskMock;
    @Mock
    private ClosePositionTask closePositionTaskMock;
    @Mock
    private PositionUtil positionUtilMock;
    @Mock
    private TaskParamsUtil taskParamsUtilMock;
    @Mock
    private ComposeParams composeParamsMock;
    @Captor
    private ArgumentCaptor<Observable<OrderEvent>> mergeCaptor;

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
        when(submitParamsMock.type()).thenReturn(TaskParamsType.SUBMIT);

        orderUtil.execute(submitParamsMock);

        verify(taskParamsUtilMock).subscribeBasicParams(basicTaskMock.submitOrder(submitParamsMock),
                                                        submitParamsMock);
    }

    @Test
    public void mergeOrdersCallsSubscribeOnTaskParams() {
        final MergeParams mergeParamsMock = mock(MergeParams.class);
        when(mergeParamsMock.type()).thenReturn(TaskParamsType.MERGE);

        orderUtil.execute(mergeParamsMock);

        verify(taskParamsUtilMock).subscribeBasicParams(basicTaskMock.mergeOrders(mergeParamsMock),
                                                        mergeParamsMock);
    }

    @Test
    public void closeCallsSubscribeOnTaskParams() {
        final CloseParams closeParamsMock = mock(CloseParams.class);
        when(closeParamsMock.type()).thenReturn(TaskParamsType.CLOSE);

        orderUtil.execute(closeParamsMock);

        verify(taskParamsUtilMock).subscribeBasicParams(basicTaskMock.close(closeParamsMock),
                                                        closeParamsMock);
    }

    @Test
    public void setLabelCallsSubscribeOnTaskParams() {
        final SetLabelParams setLabelParamsMock = mock(SetLabelParams.class);
        when(setLabelParamsMock.type()).thenReturn(TaskParamsType.SETLABEL);

        orderUtil.execute(setLabelParamsMock);

        verify(taskParamsUtilMock).subscribeBasicParams(basicTaskMock.setLabel(setLabelParamsMock),
                                                        setLabelParamsMock);
    }

    @Test
    public void setGTTCallsSubscribeOnTaskParams() {
        final SetGTTParams setGTTParamsMock = mock(SetGTTParams.class);
        when(setGTTParamsMock.type()).thenReturn(TaskParamsType.SETGTT);

        orderUtil.execute(setGTTParamsMock);

        verify(taskParamsUtilMock).subscribeBasicParams(basicTaskMock.setGoodTillTime(setGTTParamsMock),
                                                        setGTTParamsMock);
    }

    @Test
    public void setRequestedAmountCallsSubscribeOnTaskParams() {
        final SetAmountParams setAmountParamsMock = mock(SetAmountParams.class);
        when(setAmountParamsMock.type()).thenReturn(TaskParamsType.SETAMOUNT);

        orderUtil.execute(setAmountParamsMock);

        verify(taskParamsUtilMock).subscribeBasicParams(basicTaskMock.setRequestedAmount(setAmountParamsMock),
                                                        setAmountParamsMock);
    }

    @Test
    public void setOpenPriceCallsSubscribeOnTaskParams() {
        final SetOpenPriceParams setOpenPriceParamsMock = mock(SetOpenPriceParams.class);
        when(setOpenPriceParamsMock.type()).thenReturn(TaskParamsType.SETOPENPRICE);

        orderUtil.execute(setOpenPriceParamsMock);

        verify(taskParamsUtilMock).subscribeBasicParams(basicTaskMock.setOpenPrice(setOpenPriceParamsMock),
                                                        setOpenPriceParamsMock);
    }

    @Test
    public void setSLCallsSubscribeOnTaskParams() {
        final SetSLParams setSLParamsMock = mock(SetSLParams.class);
        when(setSLParamsMock.type()).thenReturn(TaskParamsType.SETSL);

        orderUtil.execute(setSLParamsMock);

        verify(taskParamsUtilMock).subscribeBasicParams(basicTaskMock.setStopLossPrice(setSLParamsMock),
                                                        setSLParamsMock);
    }

    @Test
    public void setTPCallsSubscribeOnTaskParams() {
        final SetTPParams setTPParamsMock = mock(SetTPParams.class);
        when(setTPParamsMock.type()).thenReturn(TaskParamsType.SETTP);

        orderUtil.execute(setTPParamsMock);

        verify(taskParamsUtilMock).subscribeBasicParams(basicTaskMock.setTakeProfitPrice(setTPParamsMock),
                                                        setTPParamsMock);
    }

    @Test
    public void mergePositionCallsSubscribeOnTaskParams() {
        final MergePositionParams mergePositionParamsMock = mock(MergePositionParams.class);
        when(mergePositionParamsMock.composeData())
            .thenReturn(composeParamsMock);
        when(mergePositionParamsMock.type()).thenReturn(TaskParamsType.MERGEPOSITION);

        orderUtil.execute(mergePositionParamsMock);

        verify(taskParamsUtilMock)
            .subscribeComposeData(mergePositionTaskMock.merge(mergePositionParamsMock),
                                  composeParamsMock);
    }

    @Test
    public void mergeAllPositionsCallsSubscribeOnTaskParams() {
        final MergeAllPositionsParams mergeAllPositionsParamsMock = mock(MergeAllPositionsParams.class);
        when(mergeAllPositionsParamsMock.composeData())
            .thenReturn(composeParamsMock);
        when(mergeAllPositionsParamsMock.type()).thenReturn(TaskParamsType.MERGEALLPOSITIONS);

        orderUtil.execute(mergeAllPositionsParamsMock);

        verify(taskParamsUtilMock)
            .subscribeComposeData(mergePositionTaskMock.mergeAll(mergeAllPositionsParamsMock),
                                  composeParamsMock);
    }

    @Test
    public void closePositionCallsSubscribeOnTaskParams() {
        final ClosePositionParams closePositionParamsMock = mock(ClosePositionParams.class);
        when(closePositionParamsMock.composeData())
            .thenReturn(composeParamsMock);
        when(closePositionParamsMock.type()).thenReturn(TaskParamsType.CLOSEPOSITION);

        orderUtil.execute(closePositionParamsMock);

        verify(taskParamsUtilMock)
            .subscribeComposeData(closePositionTaskMock.close(closePositionParamsMock),
                                  composeParamsMock);
    }

    @Test
    public void closeAllPositionsCallsSubscribeOnTaskParams() {
        final CloseAllPositionsParams closeAllPositionsParamsMock = mock(CloseAllPositionsParams.class);
        when(closeAllPositionsParamsMock.composeData())
            .thenReturn(composeParamsMock);
        when(closeAllPositionsParamsMock.type()).thenReturn(TaskParamsType.CLOSEALLPOSITIONS);

        orderUtil.execute(closeAllPositionsParamsMock);

        verify(taskParamsUtilMock)
            .subscribeComposeData(closePositionTaskMock.closeAll(closeAllPositionsParamsMock),
                                  composeParamsMock);

    }

    @Test
    public void executeBatchSubscribesCorrect() {
        final SubmitParams submitParamsMock = mock(SubmitParams.class);
        when(submitParamsMock.type()).thenReturn(TaskParamsType.SUBMIT);
        final Subject<OrderEvent> submitSubject = PublishSubject.create();
        when(basicTaskMock.submitOrder(submitParamsMock)).thenReturn(submitSubject);

        final CloseParams closeParamsMock = mock(CloseParams.class);
        when(closeParamsMock.type()).thenReturn(TaskParamsType.CLOSE);
        final Subject<OrderEvent> closeSubject = PublishSubject.create();
        when(basicTaskMock.close(closeParamsMock)).thenReturn(closeSubject);

        final List<TaskParamsBase> paramsList = new ArrayList<>();
        paramsList.add(submitParamsMock);
        paramsList.add(closeParamsMock);
        final BatchParams batchParamsMock = mock(BatchParams.class);
        when(batchParamsMock.paramsCollection()).thenReturn(paramsList);
        final ComposeData composeDataMock = mock(ComposeData.class);
        when(batchParamsMock.composeData()).thenReturn(composeDataMock);

        when(taskParamsUtilMock.composeParamsWithEvents(submitSubject,
                                                        submitParamsMock.composeData(),
                                                        submitParamsMock.consumerForEvent()))
                                                            .thenReturn(submitSubject);
        when(taskParamsUtilMock.composeParamsWithEvents(closeSubject,
                                                        closeParamsMock.composeData(),
                                                        closeParamsMock.consumerForEvent()))
                                                            .thenReturn(closeSubject);

        orderUtil.executeBatch(batchParamsMock);

        verify(taskParamsUtilMock).composeParamsWithEvents(submitSubject,
                                                           submitParamsMock.composeData(),
                                                           submitParamsMock.consumerForEvent());
        verify(taskParamsUtilMock).composeParamsWithEvents(closeSubject,
                                                           closeParamsMock.composeData(),
                                                           closeParamsMock.consumerForEvent());

        verify(taskParamsUtilMock).subscribeComposeData(mergeCaptor.capture(), eq(composeDataMock));
        final Observable<OrderEvent> mergedObservables = mergeCaptor.getValue();
        final TestObserver<OrderEvent> testObserver = mergedObservables.test();

        submitSubject.onNext(submitEvent);
        closeSubject.onNext(closeEvent);
        testObserver.assertValues(submitEvent, closeEvent);

        submitSubject.onComplete();
        testObserver.assertNotComplete();

        closeSubject.onComplete();
        testObserver.assertComplete();
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
