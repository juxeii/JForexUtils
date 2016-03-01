package com.jforex.programming.article;

import java.util.HashMap;
import java.util.Map;

import com.dukascopy.api.IAccount;
import com.dukascopy.api.IBar;
import com.dukascopy.api.IContext;
import com.dukascopy.api.IEngine.OrderCommand;
import com.dukascopy.api.IMessage;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.IStrategy;
import com.dukascopy.api.ITick;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.Library;
import com.dukascopy.api.Period;
import com.dukascopy.api.RequiresFullAccess;
import com.jforex.programming.misc.ConcurrentUtil;
import com.jforex.programming.misc.JForexUtil;
import com.jforex.programming.order.OrderCreateResult;
import com.jforex.programming.order.OrderParams;
import com.jforex.programming.order.OrderUtil;
import com.jforex.programming.order.event.OrderEvent;
import com.jforex.programming.order.event.OrderEventConsumer;
import com.jforex.programming.order.event.OrderEventType;

/* Remove both annotations if you develop a standalone app */
@RequiresFullAccess
/* Change the path to the jar to your path! */
@Library("D:/programs/JForex/libs/JForexUtils-0.9.35.jar")
public class RunningExamplePart3 implements IStrategy {

    private JForexUtil jForexUtil;
    private OrderUtil orderUtil;

    @Override
    public void onStart(final IContext context) throws JFException {
        jForexUtil = new JForexUtil(context);

        minimalSubmitExample();
        resultEvaluationExample1();
        resultEvaluationExample2();
        orderEventCallbackExample1();
        orderEventCallbackExample2();
        orderSubmissionOnThread();
    }

    private OrderParams createOrderParams(final String label) {
        // Prepare order parameters for EUR/USD with fluent interface
        return OrderParams.forInstrument(Instrument.EURUSD)
                          .withOrderCommand(OrderCommand.BUY)
                          .withAmount(0.001)
                          .withLabel(label)
                          .build();
    }

    private void minimalSubmitExample() {
        final OrderParams orderParamsEURUSD = createOrderParams("minimalSubmitExample");

        orderUtil = jForexUtil.orderUtil();
        orderUtil.submit(orderParamsEURUSD);
    }

    private void resultEvaluationExample1() {
        final OrderParams orderParamsEURUSD = createOrderParams("resultEvaluationExample1");

        final OrderCreateResult result = orderUtil.submit(orderParamsEURUSD);
        if (result.exceptionOpt().isPresent()) {
            final Exception e = result.exceptionOpt().get();
            System.out.println("Ouch! An excpetion occured: " + e.getMessage());
            // ... handle the exception somehow
        } else { // No exception, so the new order was created(not yet accepted)
            final IOrder order = result.orderOpt().get();
        }
    }

    private void resultEvaluationExample2() {
        final OrderParams orderParamsEURUSD = createOrderParams("resultEvaluationExample2");

        final OrderCreateResult result = orderUtil.submit(orderParamsEURUSD);
        final IOrder order = result.orderOpt().orElse(null);
        if (order == null) { // Order was not created, exception occured!
            final Exception e = result.exceptionOpt().get();
            System.out.println("Ouch! An excpetion occured: " + e.getMessage());
            // ... handle the exception somehow
        }
    }

    private void orderEventCallbackExample1() {
        final OrderParams orderParamsEURUSD = createOrderParams("orderEventCallbackExample1");

        final MyEventConsumer eventConsumer = new MyEventConsumer();
        final OrderCreateResult result = orderUtil.submit(orderParamsEURUSD, eventConsumer);
        final IOrder order = result.orderOpt().orElse(null);
        if (order == null)
            ;// as before...
    }

    private void orderEventCallbackExample2() {
        final OrderParams orderParamsEURUSD = createOrderParams("orderEventCallbackExample2");

        final Map<OrderEventType, OrderEventConsumer> consumerMap = new HashMap<>();
        consumerMap.put(OrderEventType.SUBMIT_OK, new SubmitHandler());
        consumerMap.put(OrderEventType.SUBMIT_REJECTED, new SubmitRejectHandler());

        final OrderCreateResult result = orderUtil.submit(orderParamsEURUSD, consumerMap);
        final IOrder order = result.orderOpt().orElse(null);
        if (order == null)
            ;// as before...
    }

    private void orderSubmissionOnThread() {
        final MyThread myThread = new MyThread();
        myThread.start();
    }

    private class MyEventConsumer implements OrderEventConsumer {
        @Override
        public void onOrderEvent(final OrderEvent orderEvent) {
            final IOrder order = orderEvent.order();
            final OrderEventType type = orderEvent.type();

            switch (type) {
            case SUBMIT_OK:
                System.out.println("Order was submitted.");
                break;
            case FULL_FILL_OK:
                System.out.println("Order was fully filled.");
                break;
            default:
                break;
            }
        }
    }

    private class SubmitHandler implements OrderEventConsumer {
        @Override
        public void onOrderEvent(final OrderEvent orderEvent) {
            System.out.println("MapHandler: order was submitted.");
        }
    }

    private class SubmitRejectHandler implements OrderEventConsumer {
        @Override
        public void onOrderEvent(final OrderEvent orderEvent) {
            System.out.println("MapHandler: order submit was rejected!");
        }
    }

    private class MyThread extends Thread {

        @Override
        public void run() {
            final OrderParams orderParamsEURUSD = createOrderParams("threadExample");

            final OrderCreateResult result = orderUtil.submit(orderParamsEURUSD);
            final IOrder order = result.orderOpt().orElse(null);
            if (order != null)
                System.out.println("Order created on thread " + ConcurrentUtil.threadName() + "!");
        }
    }

    @Override
    public void onStop() throws JFException {
        jForexUtil.onStop();
    }

    @Override
    public void onMessage(final IMessage message) throws JFException {
        jForexUtil.onMessage(message);
    }

    @Override
    public void onTick(final Instrument instrument,
                       final ITick tick) throws JFException {
        jForexUtil.onTick(instrument, tick);
    }

    @Override
    public void onBar(final Instrument instrument,
                      final Period period,
                      final IBar askBar,
                      final IBar bidBar) throws JFException {
        jForexUtil.onBar(instrument, period, askBar, bidBar);
    }

    @Override
    public void onAccount(final IAccount account) throws JFException {

    }
}
