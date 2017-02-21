package com.jforex.programming.order.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import com.dukascopy.api.JFException;
import com.jforex.programming.misc.Exposure;
import com.jforex.programming.order.OrderUtil;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.task.BasicTask;
import com.jforex.programming.order.task.ClosePositionTask;
import com.jforex.programming.order.task.MergePositionTask;
import com.jforex.programming.order.task.params.ComposeData;
import com.jforex.programming.order.task.params.ComposeDataImpl;
import com.jforex.programming.order.task.params.TaskParams;
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

import de.bechte.junit.runners.context.HierarchicalContextRunner;
import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

@RunWith(HierarchicalContextRunner.class)
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
    private ComposeDataImpl composeParamsMock;
    @Mock
    private PositionOrders positionOrdersMock;
    @Mock
    private Exposure exposureMock;
    @Captor
    private ArgumentCaptor<Observable<OrderEvent>> observableCaptor;

    @Before
    public void setUp() {
        when(positionUtilMock.positionOrders(instrumentEURUSD))
            .thenReturn(positionOrdersMock);

        orderUtil = new OrderUtil(basicTaskMock,
                                  mergePositionTaskMock,
                                  closePositionTaskMock,
                                  positionUtilMock,
                                  taskParamsUtilMock,
                                  exposureMock);
    }

    public class SubmitOrder {

        private SubmitParams submitParamsMock;

        @Before
        public void setUp() {
            submitParamsMock = mock(SubmitParams.class);

            when(submitParamsMock.type()).thenReturn(TaskParamsType.SUBMIT);
        }

        public class ExposureNotExceeded {

            @Before
            public void setUp() {
                when(submitParamsMock.orderParams()).thenReturn(buyParamsEURUSD);

                when(exposureMock.wouldExceed(eq(instrumentEURUSD), anyDouble()))
                    .thenReturn(false);
            }

            @Test
            public void executeForSubmitParamsIsCorrect() {
                final Observable<OrderEvent> submitObservable = eventObservable(submitEvent);
                when(basicTaskMock.submitOrder(submitParamsMock))
                    .thenReturn(submitObservable);

                orderUtil.execute(submitParamsMock);

                verify(taskParamsUtilMock).composeAndSubscribe(submitObservable,
                                                               submitParamsMock);
            }

            @Test
            public void asObservableForSubmitParamsIsCorrect() {
                final Observable<OrderEvent> submitObservable = eventObservable(submitEvent);
                when(basicTaskMock.submitOrder(submitParamsMock))
                    .thenReturn(submitObservable);
                when(taskParamsUtilMock.compose(submitObservable, submitParamsMock))
                    .thenReturn(submitObservable);

                final Observable<OrderEvent> observable = orderUtil.paramsToObservable(submitParamsMock);

                assertThat(observable, equalTo(submitObservable));
                verify(taskParamsUtilMock).compose(submitObservable, submitParamsMock);
            }

            @Test
            public void executeBatchSubscribesCorrect() {
                final Subject<OrderEvent> submitSubject = PublishSubject.create();
                when(basicTaskMock.submitOrder(submitParamsMock)).thenReturn(submitSubject);

                final CloseParams closeParamsMock = mock(CloseParams.class);
                when(closeParamsMock.type()).thenReturn(TaskParamsType.CLOSE);
                when(closeParamsMock.order()).thenReturn(buyOrderEURUSD);
                final Subject<OrderEvent> closeSubject = PublishSubject.create();
                when(basicTaskMock.close(closeParamsMock)).thenReturn(closeSubject);

                final List<TaskParams> paramsList = new ArrayList<>();
                paramsList.add(submitParamsMock);
                paramsList.add(closeParamsMock);
                final BatchParams batchParamsMock = mock(BatchParams.class);
                when(batchParamsMock.taskParams()).thenReturn(paramsList);
                final ComposeData composeDataMock = mock(ComposeData.class);
                when(batchParamsMock.composeData()).thenReturn(composeDataMock);

                when(taskParamsUtilMock.compose(submitSubject, submitParamsMock))
                    .thenReturn(submitSubject);
                when(taskParamsUtilMock.compose(closeSubject, closeParamsMock))
                    .thenReturn(closeSubject);

                orderUtil.executeBatch(batchParamsMock);

                verify(taskParamsUtilMock).compose(submitSubject, submitParamsMock);
                verify(taskParamsUtilMock).compose(closeSubject, closeParamsMock);

                verify(taskParamsUtilMock).composeAndSubscribe(observableCaptor.capture(), eq(batchParamsMock));
                final Observable<OrderEvent> mergedObservables = observableCaptor.getValue();
                final TestObserver<OrderEvent> testObserver = mergedObservables.test();

                submitSubject.onNext(submitEvent);
                closeSubject.onNext(closeEvent);
                testObserver.assertValues(submitEvent, closeEvent);

                submitSubject.onComplete();
                testObserver.assertNotComplete();

                closeSubject.onComplete();
                testObserver.assertComplete();
            }
        }

        public class ExposureExceeded {

            @Before
            public void setUp() {
                when(exposureMock.wouldExceed(eq(instrumentEURUSD), anyDouble()))
                    .thenReturn(true);
            }

            @Test
            public void errorObservableIsCreatedForBuy() {
                when(submitParamsMock.orderParams()).thenReturn(buyParamsEURUSD);

                orderUtil.execute(submitParamsMock);

                verify(exposureMock).wouldExceed(instrumentEURUSD, buyParamsEURUSD.amount());
                verify(taskParamsUtilMock).composeAndSubscribe(observableCaptor.capture(),
                                                               eq(submitParamsMock));
                final Observable<OrderEvent> observable = observableCaptor.getValue();
                observable
                    .test()
                    .assertError(JFException.class);
            }

            @Test
            public void errorObservableIsCreatedForSell() {
                when(submitParamsMock.orderParams()).thenReturn(sellParamsEURUSD);

                orderUtil.execute(submitParamsMock);

                verify(exposureMock).wouldExceed(instrumentEURUSD, -sellParamsEURUSD.amount());
                verify(taskParamsUtilMock).composeAndSubscribe(observableCaptor.capture(),
                                                               eq(submitParamsMock));
                final Observable<OrderEvent> observable = observableCaptor.getValue();
                observable
                    .test()
                    .assertError(JFException.class);
            }
        }
    }

    @Test
    public void mergeOrdersCallsSubscribeOnTaskParams() {
        final MergeParams mergeParamsMock = mock(MergeParams.class);
        when(mergeParamsMock.type()).thenReturn(TaskParamsType.MERGE);

        orderUtil.execute(mergeParamsMock);

        verify(taskParamsUtilMock).composeAndSubscribe(basicTaskMock.mergeOrders(mergeParamsMock),
                                                       mergeParamsMock);
    }

    public class OrderClose {

        private CloseParams closeParamsMock;

        @Before
        public void setUp() {
            closeParamsMock = mock(CloseParams.class);
            when(closeParamsMock.type()).thenReturn(TaskParamsType.CLOSE);
        }

        public class ExposureNotExceeded {

            @Before
            public void setUp() {
                when(closeParamsMock.order()).thenReturn(buyOrderEURUSD);

                when(exposureMock.wouldExceed(eq(instrumentEURUSD), anyDouble()))
                    .thenReturn(false);
            }

            @Test
            public void closeCallsSubscribeOnTaskParams() {
                orderUtil.execute(closeParamsMock);

                verify(taskParamsUtilMock).composeAndSubscribe(basicTaskMock.close(closeParamsMock),
                                                               closeParamsMock);
            }
        }

        public class ExposureExceededForFullClose {

            @Before
            public void setUp() {
                when(exposureMock.wouldExceed(eq(instrumentEURUSD), anyDouble()))
                    .thenReturn(true);

                when(closeParamsMock.partialCloseAmount())
                    .thenReturn(0.0);
            }

            @Test
            public void errorObservableIsCreatedForBuyOrder() {
                when(closeParamsMock.order()).thenReturn(buyOrderEURUSD);

                orderUtil.execute(closeParamsMock);

                verify(exposureMock).wouldExceed(instrumentEURUSD, -buyOrderEURUSD.getAmount());
                verify(taskParamsUtilMock).composeAndSubscribe(observableCaptor.capture(),
                                                               eq(closeParamsMock));
                final Observable<OrderEvent> observable = observableCaptor.getValue();
                observable
                    .test()
                    .assertError(JFException.class);
            }

            @Test
            public void errorObservableIsCreatedForSellOrder() {
                when(closeParamsMock.order()).thenReturn(sellOrderEURUSD);

                orderUtil.execute(closeParamsMock);

                verify(exposureMock).wouldExceed(instrumentEURUSD, sellOrderEURUSD.getAmount());
                verify(taskParamsUtilMock).composeAndSubscribe(observableCaptor.capture(),
                                                               eq(closeParamsMock));
                final Observable<OrderEvent> observable = observableCaptor.getValue();
                observable
                    .test()
                    .assertError(JFException.class);
            }
        }

        public class ExposureExceededForPartialClose {

            private final double partialCloseAmount = 0.12;

            @Before
            public void setUp() {
                when(exposureMock.wouldExceed(eq(instrumentEURUSD), anyDouble()))
                    .thenReturn(true);

                when(closeParamsMock.partialCloseAmount())
                    .thenReturn(partialCloseAmount);
            }

            @Test
            public void errorObservableIsCreatedForBuyOrder() {
                when(closeParamsMock.order()).thenReturn(buyOrderEURUSD);

                orderUtil.execute(closeParamsMock);

                verify(exposureMock).wouldExceed(instrumentEURUSD, -partialCloseAmount);
                verify(taskParamsUtilMock).composeAndSubscribe(observableCaptor.capture(),
                                                               eq(closeParamsMock));
                final Observable<OrderEvent> observable = observableCaptor.getValue();
                observable
                    .test()
                    .assertError(JFException.class);
            }

            @Test
            public void errorObservableIsCreatedForSellOrder() {
                when(closeParamsMock.order()).thenReturn(sellOrderEURUSD);

                orderUtil.execute(closeParamsMock);

                verify(exposureMock).wouldExceed(instrumentEURUSD, partialCloseAmount);
                verify(taskParamsUtilMock).composeAndSubscribe(observableCaptor.capture(),
                                                               eq(closeParamsMock));
                final Observable<OrderEvent> observable = observableCaptor.getValue();
                observable
                    .test()
                    .assertError(JFException.class);
            }
        }
    }

    @Test
    public void setLabelCallsSubscribeOnTaskParams() {
        final SetLabelParams setLabelParamsMock = mock(SetLabelParams.class);
        when(setLabelParamsMock.type()).thenReturn(TaskParamsType.SETLABEL);

        orderUtil.execute(setLabelParamsMock);

        verify(taskParamsUtilMock).composeAndSubscribe(basicTaskMock.setLabel(setLabelParamsMock),
                                                       setLabelParamsMock);
    }

    @Test
    public void setGTTCallsSubscribeOnTaskParams() {
        final SetGTTParams setGTTParamsMock = mock(SetGTTParams.class);
        when(setGTTParamsMock.type()).thenReturn(TaskParamsType.SETGTT);

        orderUtil.execute(setGTTParamsMock);

        verify(taskParamsUtilMock).composeAndSubscribe(basicTaskMock.setGoodTillTime(setGTTParamsMock),
                                                       setGTTParamsMock);
    }

    @Test
    public void setRequestedAmountCallsSubscribeOnTaskParams() {
        final SetAmountParams setAmountParamsMock = mock(SetAmountParams.class);
        when(setAmountParamsMock.type()).thenReturn(TaskParamsType.SETAMOUNT);

        orderUtil.execute(setAmountParamsMock);

        verify(taskParamsUtilMock).composeAndSubscribe(basicTaskMock.setRequestedAmount(setAmountParamsMock),
                                                       setAmountParamsMock);
    }

    @Test
    public void setOpenPriceCallsSubscribeOnTaskParams() {
        final SetOpenPriceParams setOpenPriceParamsMock = mock(SetOpenPriceParams.class);
        when(setOpenPriceParamsMock.type()).thenReturn(TaskParamsType.SETOPENPRICE);

        orderUtil.execute(setOpenPriceParamsMock);

        verify(taskParamsUtilMock).composeAndSubscribe(basicTaskMock.setOpenPrice(setOpenPriceParamsMock),
                                                       setOpenPriceParamsMock);
    }

    @Test
    public void setSLCallsSubscribeOnTaskParams() {
        final SetSLParams setSLParamsMock = mock(SetSLParams.class);
        when(setSLParamsMock.type()).thenReturn(TaskParamsType.SETSL);

        orderUtil.execute(setSLParamsMock);

        verify(taskParamsUtilMock).composeAndSubscribe(basicTaskMock.setStopLossPrice(setSLParamsMock),
                                                       setSLParamsMock);
    }

    @Test
    public void setTPCallsSubscribeOnTaskParams() {
        final SetTPParams setTPParamsMock = mock(SetTPParams.class);
        when(setTPParamsMock.type()).thenReturn(TaskParamsType.SETTP);

        orderUtil.execute(setTPParamsMock);

        verify(taskParamsUtilMock).composeAndSubscribe(basicTaskMock.setTakeProfitPrice(setTPParamsMock),
                                                       setTPParamsMock);
    }

    @Test
    public void mergePositionCallsSubscribeOnTaskParams() {
        final MergePositionParams mergePositionParamsMock = mock(MergePositionParams.class);
        when(mergePositionParamsMock.composeData())
            .thenReturn(composeParamsMock);
        when(mergePositionParamsMock.type()).thenReturn(TaskParamsType.MERGEPOSITION);

        orderUtil.execute(mergePositionParamsMock);

        verify(taskParamsUtilMock).composeAndSubscribe(mergePositionTaskMock.merge(mergePositionParamsMock),
                                                       mergePositionParamsMock);
    }

    @Test
    public void mergeAllPositionsCallsSubscribeOnTaskParams() {
        final MergeAllPositionsParams mergeAllPositionsParamsMock = mock(MergeAllPositionsParams.class);
        when(mergeAllPositionsParamsMock.composeData())
            .thenReturn(composeParamsMock);
        when(mergeAllPositionsParamsMock.type()).thenReturn(TaskParamsType.MERGEALLPOSITIONS);

        orderUtil.execute(mergeAllPositionsParamsMock);

        verify(taskParamsUtilMock).composeAndSubscribe(mergePositionTaskMock.mergeAll(mergeAllPositionsParamsMock),
                                                       mergeAllPositionsParamsMock);
    }

    @Test
    public void closePositionCallsSubscribeOnTaskParams() {
        final ClosePositionParams closePositionParamsMock = mock(ClosePositionParams.class);
        when(closePositionParamsMock.composeData())
            .thenReturn(composeParamsMock);
        when(closePositionParamsMock.type()).thenReturn(TaskParamsType.CLOSEPOSITION);

        orderUtil.execute(closePositionParamsMock);

        verify(taskParamsUtilMock).composeAndSubscribe(closePositionTaskMock.close(closePositionParamsMock),
                                                       closePositionParamsMock);
    }

    @Test
    public void closeAllPositionsCallsSubscribeOnTaskParams() {
        final CloseAllPositionsParams closeAllPositionsParamsMock = mock(CloseAllPositionsParams.class);
        when(closeAllPositionsParamsMock.composeData())
            .thenReturn(composeParamsMock);
        when(closeAllPositionsParamsMock.type()).thenReturn(TaskParamsType.CLOSEALLPOSITIONS);

        orderUtil.execute(closeAllPositionsParamsMock);

        verify(taskParamsUtilMock).composeAndSubscribe(closePositionTaskMock.closeAll(closeAllPositionsParamsMock),
                                                       closeAllPositionsParamsMock);
    }

    @Test
    public void positionOrdersDelegatesToPositionTask() {
        final PositionOrders actualPositionOrders = orderUtil.positionOrders(instrumentEURUSD);

        verify(positionUtilMock).positionOrders(instrumentEURUSD);
        assertThat(actualPositionOrders, equalTo(positionOrdersMock));
    }
}
