package com.jforex.programming.test.fakes;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.aeonbits.owner.ConfigFactory;

import com.dukascopy.api.ICloseOrder;
import com.dukascopy.api.IEngine.OrderCommand;
import com.dukascopy.api.IFillOrder;
import com.dukascopy.api.IMessage;
import com.dukascopy.api.IOrder;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.JFException;
import com.dukascopy.api.OfferSide;
import com.jforex.programming.order.OrderParams;
import com.jforex.programming.settings.PlatformSettings;
import com.jforex.programming.settings.UserSettings;
import com.jforex.programming.test.common.OrderParamsForTest;

public class IOrderForTest implements IOrder {

    private String label;
    private final Instrument instrument;
    private OrderCommand orderCommand;
    private double amount;

    private double stopLossPrice;
    private double takeProfitPrice;
    private double openPrice;
    private double requestedAmount;
    private long goodTillTime;
    private final String comment;
    private IOrder.State orderState = IOrder.State.CREATED;
    private String id;

    private static final PlatformSettings platformSettings =
            ConfigFactory.create(PlatformSettings.class);
    private static final UserSettings userSettings =
            ConfigFactory.create(UserSettings.class);

    private IOrderForTest(final Builder builder) {
        label = builder.label;
        instrument = builder.instrument;
        orderCommand = builder.orderCommand;
        amount = builder.amount;
        stopLossPrice = builder.stopLossPrice;
        takeProfitPrice = builder.takeProfitPrice;
        openPrice = builder.openPrice;
        requestedAmount = builder.requestedAmount;
        goodTillTime = builder.goodTillTime;
        comment = builder.comment;
    }

    public void setState(final IOrder.State orderState) {
        this.orderState = orderState;
    }

    @Override
    public void close() {
        amount = 0;
        orderState = IOrder.State.CLOSED;
    }

    @Override
    public void close(final double arg0) throws JFException {
    }

    @Override
    public void close(final double arg0,
                      final double arg1) throws JFException {
    }

    @Override
    public void close(final double arg0,
                      final double arg1,
                      final double arg2) throws JFException {
    }

    @Override
    public double getAmount() {
        return amount;
    }

    @Override
    public List<ICloseOrder> getCloseHistory() {
        return null;
    }

    @Override
    public double getClosePrice() {
        return 0;
    }

    @Override
    public long getCloseTime() {
        return 0;
    }

    @Override
    public String getComment() {
        return comment;
    }

    @Override
    public double getCommission() {
        return 0;
    }

    @Override
    public double getCommissionInUSD() {
        return 0;
    }

    @Override
    public long getCreationTime() {
        return 0;
    }

    @Override
    public List<IFillOrder> getFillHistory() {
        return null;
    }

    @Override
    public long getFillTime() {
        return 0;
    }

    @Override
    public long getGoodTillTime() {
        return goodTillTime;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Instrument getInstrument() {
        return instrument;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public double getOpenPrice() {
        return openPrice;
    }

    @Override
    public OrderCommand getOrderCommand() {
        return orderCommand;
    }

    public void setOrderCommand(final OrderCommand orderCommand) {
        this.orderCommand = orderCommand;
    }

    @Override
    public double getOriginalAmount() {
        return 0;
    }

    @Override
    public double getProfitLossInAccountCurrency() {
        return 0;
    }

    @Override
    public double getProfitLossInPips() {
        return 0;
    }

    @Override
    public double getProfitLossInUSD() {
        return 0;
    }

    @Override
    public double getRequestedAmount() {
        return requestedAmount;
    }

    @Override
    public State getState() {
        return orderState;
    }

    @Override
    public double getStopLossPrice() {
        return stopLossPrice;
    }

    @Override
    public OfferSide getStopLossSide() {
        return null;
    }

    @Override
    public double getTakeProfitPrice() {
        return takeProfitPrice;
    }

    @Override
    public double getTrailingStep() {
        return 0;
    }

    @Override
    public boolean isLong() {
        return orderCommand == OrderCommand.BUY;
    }

    @Override
    public void setGoodTillTime(final long goodTillTime) {
        this.goodTillTime = goodTillTime;
    }

    @Override
    public void setLabel(final String label) {
        this.label = label;
    }

    @Override
    public void setOpenPrice(final double openPrice) {
        this.openPrice = openPrice;
    }

    public void setAmount(final double amount) {
        this.amount = amount;
    }

    @Override
    public void setRequestedAmount(final double requestedAmount) {
        this.requestedAmount = requestedAmount;
    }

    @Override
    public void setStopLossPrice(final double stopLossPrice) {
        this.stopLossPrice = stopLossPrice;
    }

    @Override
    public void setStopLossPrice(final double arg0,
                                 final OfferSide arg1) {
    }

    @Override
    public void setStopLossPrice(final double arg0,
                                 final OfferSide arg1,
                                 final double arg2) {
    }

    @Override
    public void setTakeProfitPrice(final double takeProfitPrice) {
        this.takeProfitPrice = takeProfitPrice;
    }

    @Override
    public void waitForUpdate(final long arg0) {
    }

    @Override
    public IMessage waitForUpdate(final State... arg0) throws JFException {
        return null;
    }

    @Override
    public IMessage waitForUpdate(final long arg0,
                                  final TimeUnit arg1) {
        return null;
    }

    @Override
    public IMessage waitForUpdate(final long arg0,
                                  final State... arg1) throws JFException {
        return null;
    }

    @Override
    public IMessage waitForUpdate(final long arg0,
                                  final TimeUnit arg1,
                                  final State... arg2) throws JFException {
        return null;
    }

    public static class Builder {

        private final String label;
        private final Instrument instrument;
        private final OrderCommand orderCommand;
        private final double amount;

        private double stopLossPrice;
        private double takeProfitPrice;
        private double openPrice;
        private double requestedAmount;
        private long goodTillTime;
        private String comment;

        public Builder(final String label,
                       final Instrument instrument,
                       final OrderCommand orderCommand,
                       final double amount) {
            this.label = label;
            this.instrument = instrument;
            this.orderCommand = orderCommand;
            this.amount = amount;

            stopLossPrice = platformSettings.noSLPrice();
            takeProfitPrice = platformSettings.noTPPrice();
            goodTillTime = userSettings.defaultGTT();
            openPrice = userSettings.defaultOpenPrice();
            requestedAmount = 0;
            comment = userSettings.defaultOrderComment();
        }

        public Builder stopLossPrice(final double stopLossPrice) {
            this.stopLossPrice = stopLossPrice;
            return this;
        }

        public Builder takeProfitPrice(final double takeProfitPrice) {
            this.takeProfitPrice = takeProfitPrice;
            return this;
        }

        public Builder openPrice(final double openPrice) {
            this.openPrice = openPrice;
            return this;
        }

        public Builder requestedAmount(final double requestedAmount) {
            this.requestedAmount = requestedAmount;
            return this;
        }

        public Builder goodTillTime(final long goodTillTime) {
            this.goodTillTime = goodTillTime;
            return this;
        }

        public Builder comment(final String comment) {
            this.comment = comment;
            return this;
        }

        public IOrderForTest build() {
            return spy(new IOrderForTest(this));
        }
    }

    public static void throwOnClose(final IOrderForTest order) throws JFException {
        doThrow(new JFException("Exception thrown during order close!")).when(order).close();
    }

    public static void throwOnSetLabel(final IOrderForTest order) throws JFException {
        doThrow(new JFException("Exception thrown during label change!")).when(order)
                .setLabel(any());
    }

    public static void throwOnSetGTT(final IOrderForTest order) throws JFException {
        doThrow(new JFException("Exception thrown during GTT change!")).when(order)
                .setGoodTillTime(anyLong());
    }

    public static void throwOnSetAmount(final IOrderForTest order) throws JFException {
        doThrow(new JFException("Exception thrown during amount change!")).when(order)
                .setRequestedAmount(anyDouble());
    }

    public static void throwOnSetOpenPrice(final IOrderForTest order) throws JFException {
        doThrow(new JFException("Exception thrown during open price change!")).when(order)
                .setOpenPrice(anyDouble());
    }

    public static void throwOnSetSL(final IOrderForTest order) throws JFException {
        doThrow(new JFException("Exception thrown during SL change!")).when(order)
                .setStopLossPrice(anyDouble());
    }

    public static void throwOnSetTP(final IOrderForTest order) throws JFException {
        doThrow(new JFException("Exception thrown during TP change!")).when(order)
                .setTakeProfitPrice(anyDouble());
    }

    public static IOrderForTest fromOrderParams(final OrderParams orderParams) {
        return new IOrderForTest.Builder(orderParams.label(),
                                         orderParams.instrument(),
                                         orderParams.orderCommand(),
                                         orderParams.amount())
                                                 .stopLossPrice(orderParams.stopLossPrice())
                                                 .takeProfitPrice(orderParams.takeProfitPrice())
                                                 .goodTillTime(orderParams.goodTillTime())
                                                 .comment(orderParams.comment())
                                                 .build();
    }

    public static IOrderForTest buyOrderEURUSD() {
        return fromOrderParams(OrderParamsForTest.paramsBuyEURUSD());
    }

    public static IOrderForTest sellOrderEURUSD() {
        return fromOrderParams(OrderParamsForTest.paramsSellEURUSD());
    }

    public static IOrderForTest orderAUDUSD() {
        return new IOrderForTest.Builder("TestLabelAUDUSD",
                                         Instrument.AUDUSD,
                                         OrderCommand.SELL,
                                         0.12)
                                                 .build();
    }

    public static IOrderForTest orderUSDJPY() {
        return new IOrderForTest.Builder("TestLabelUSDJPY",
                                         Instrument.USDJPY,
                                         OrderCommand.SELL,
                                         0.25)
                                                 .build();
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null)
            return false;
        if (!(o instanceof IOrder))
            return false;

        final IOrder other = (IOrder) o;
        if (this.orderState != other.getState())
            return false;
        if (this.amount != other.getAmount())
            return false;
        if (!this.id.equals(other.getId()))
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        final int hash = (int) (orderState.ordinal() * amount * id.hashCode());
        return hash;
    }

    @Override
    @Deprecated
    public boolean compare(final IOrder order) {
        return false;
    }
}
