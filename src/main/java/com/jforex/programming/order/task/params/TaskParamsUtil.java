package com.jforex.programming.order.task.params;

import java.util.Map;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventType;
import com.jforex.programming.order.task.TaskRetry;

import io.reactivex.Observable;

public class TaskParamsUtil {

    public static void subscribe(final Observable<OrderEvent> observable,
                                 final SubscribeParams subscribeParams) {
        composeRetry(observable, subscribeParams)
            .doOnSubscribe(d -> subscribeParams.startAction().run())
            .subscribe(orderEvent -> handlerOrderEvent(orderEvent, subscribeParams.consumerForEvent()),
                       e -> subscribeParams.errorConsumer().accept(e),
                       subscribeParams.completeAction());
    }

    private static Observable<OrderEvent> composeRetry(final Observable<OrderEvent> observable,
                                                       final RetryParams retryParams) {
        final int noOfRetries = retryParams.noOfRetries();
        return noOfRetries > 0
                ? observable.compose(TaskRetry.onRejectRetryWith(noOfRetries, retryParams.delayInMillis()))
                : observable;
    }

    private static void handlerOrderEvent(final OrderEvent orderEvent,
                                          final Map<OrderEventType, OrderEventConsumer> consumerForEvent) {
        consumerForEvent.computeIfPresent(orderEvent.type(),
                                          (type, consumer) -> {
                                              consumer.accept(orderEvent);
                                              return consumer;
                                          });
    }

    public static Observable<OrderEvent> composeCancelSL(final IOrder order,
                                                         final Observable<OrderEvent> observable,
                                                         final CancelSLParams cancelSLParams) {
        return composeRetry(observable, cancelSLParams)
            .doOnSubscribe(d -> cancelSLParams.startAction(order).run())
            .doOnComplete(cancelSLParams.completeAction(order))
            .doOnError(cancelSLParams.errorConsumer(order)::accept)
            .doOnNext(orderEvent -> handlerOrderEvent(orderEvent, cancelSLParams.consumerForEvent()));
    }

    public static Observable<OrderEvent> composeCancelTP(final IOrder order,
                                                         final Observable<OrderEvent> observable,
                                                         final CancelSLParams cancelTPParams) {
        return composeRetry(observable, cancelTPParams)
            .doOnSubscribe(d -> cancelTPParams.startAction(order).run())
            .doOnComplete(cancelTPParams.completeAction(order))
            .doOnError(cancelTPParams.errorConsumer(order)::accept)
            .doOnNext(orderEvent -> handlerOrderEvent(orderEvent, cancelTPParams.consumerForEvent()));
    }

    public static Observable<OrderEvent> composeMergePosition(final Instrument instrument,
                                                              final Observable<OrderEvent> observable,
                                                              final MergePositionParams mergePositionParams) {
        return composeRetry(observable, mergePositionParams)
            .doOnSubscribe(d -> mergePositionParams.startAction(instrument).run())
            .doOnComplete(mergePositionParams.completeAction(instrument))
            .doOnError(mergePositionParams.errorConsumer(instrument)::accept)
            .doOnNext(orderEvent -> handlerOrderEvent(orderEvent, mergePositionParams.consumerForEvent()));
    }

    public static Observable<OrderEvent> composeBatchCancelSL(final Instrument instrument,
                                                              final Observable<OrderEvent> observable,
                                                              final BatchCancelSLParams batchCancelSLParams) {
        return composeRetry(observable, batchCancelSLParams)
            .doOnSubscribe(d -> batchCancelSLParams.startAction(instrument).run())
            .doOnComplete(batchCancelSLParams.completeAction(instrument))
            .doOnError(batchCancelSLParams.errorConsumer(instrument)::accept);
    }

    public static Observable<OrderEvent> composeBatchCancelTP(final Instrument instrument,
                                                              final Observable<OrderEvent> observable,
                                                              final BatchCancelTPParams batchCancelTPParams) {
        return composeRetry(observable, batchCancelTPParams)
            .doOnSubscribe(d -> batchCancelTPParams.startAction(instrument).run())
            .doOnComplete(batchCancelTPParams.completeAction(instrument))
            .doOnError(batchCancelTPParams.errorConsumer(instrument)::accept);
    }

    public static Observable<OrderEvent> composeBatchCancelSLTP(final Instrument instrument,
                                                                final Observable<OrderEvent> observable,
                                                                final BatchCancelSLAndTPParams batchCancelSLAndTPParams) {
        return composeRetry(observable, batchCancelSLAndTPParams)
            .doOnSubscribe(d -> batchCancelSLAndTPParams.startAction(instrument).run())
            .doOnComplete(batchCancelSLAndTPParams.completeAction(instrument))
            .doOnError(batchCancelSLAndTPParams.errorConsumer(instrument)::accept);
    }

    public static Observable<OrderEvent> composeBatchClose(final Instrument instrument,
                                                           final Observable<OrderEvent> observable,
                                                           final ClosePositionParams closePositionParams) {
        return composeRetry(observable, closePositionParams)
            .doOnSubscribe(d -> closePositionParams.startAction(instrument).run())
            .doOnComplete(closePositionParams.completeAction(instrument))
            .doOnError(closePositionParams.errorConsumer(instrument)::accept);
    }

    public static void subscribePositionClose(final Instrument instrument,
                                              final Observable<OrderEvent> observable,
                                              final ComplexClosePositionParams complexClosePositionParams) {
        composeRetry(observable, complexClosePositionParams)
            .doOnSubscribe(d -> complexClosePositionParams.startAction(instrument).run())
            .subscribe(orderEvent -> handlerOrderEvent(orderEvent, complexClosePositionParams.consumerForEvent()),
                       e -> complexClosePositionParams.errorConsumer(instrument).accept(e),
                       complexClosePositionParams.completeAction(instrument)::run);
    }

    public static void subscribePositionMerge(final Instrument instrument,
                                              final Observable<OrderEvent> observable,
                                              final ComplexMergePositionParams complexMergePositionParams) {
        composeRetry(observable, complexMergePositionParams)
            .doOnSubscribe(d -> complexMergePositionParams.startAction(instrument).run())
            .subscribe(orderEvent -> handlerOrderEvent(orderEvent, complexMergePositionParams.consumerForEvent()),
                       e -> complexMergePositionParams.errorConsumer(instrument).accept(e),
                       complexMergePositionParams.completeAction(instrument)::run);
    }
}
