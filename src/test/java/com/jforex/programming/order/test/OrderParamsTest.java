package com.jforex.programming.order.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.dukascopy.api.IEngine.OrderCommand;
import com.dukascopy.api.Instrument;
import com.jforex.programming.order.OrderParams;
import com.jforex.programming.test.common.InstrumentUtilForTest;

public class OrderParamsTest extends InstrumentUtilForTest {

    private OrderParams orderParams;
    private OrderParams orderParamsClone;

    private String label;
    private Instrument instrument;
    private OrderCommand orderCommand;
    private double amount;
    private double price;
    private double slippage;
    private double stopLossPrice;
    private double takeProfitPrice;
    private long goodTillTime;
    private String comment;

    private void initializeOrderParamsTestValues() {
        label = "TestLabel_1";
        instrument = instrumentEURUSD;
        orderCommand = OrderCommand.BUY;
        amount = 0.1;
        price = userSettings.defaultOpenPrice();
        slippage = userSettings.defaultSlippage();
        stopLossPrice = 1.3254;
        takeProfitPrice = 1.3275;
        goodTillTime = userSettings.defaultGTT();
        comment = "Test comment";
    }

    private void createTestParams() {
        orderParams = OrderParams
            .forInstrument(instrument)
            .withOrderCommand(orderCommand)
            .withAmount(amount)
            .withLabel(label)
            .price(price)
            .slippage(slippage)
            .stopLossPrice(stopLossPrice)
            .takeProfitPrice(takeProfitPrice)
            .goodTillTime(goodTillTime)
            .comment(comment)
            .build();

        logger.info(orderParams);
    }

    private void fillOrderParamsWithOptionalValues() {
        initializeOrderParamsTestValues();
        createTestParams();

    }

    private void fillOrderParamsWithoutOptionalValues() {
        initializeOrderParamsTestValues();
        orderParams = OrderParams
            .forInstrument(instrument)
            .withOrderCommand(orderCommand)
            .withAmount(amount)
            .withLabel(label)
            .build();
    }

    public void assertMandatoryValues(final OrderParams orderParams) {
        assertThat(orderParams.label(), equalTo(label));
        assertThat(orderParams.instrument(), equalTo(instrument));
        assertThat(orderParams.orderCommand(), equalTo(orderCommand));
        assertThat(orderParams.amount(), equalTo(amount));
    }

    @Test
    public void testAreDefaultValuesCorrect() {
        fillOrderParamsWithoutOptionalValues();

        assertMandatoryValues(orderParams);
        assertThat(orderParams.price(), equalTo(userSettings.defaultOpenPrice()));
        assertThat(orderParams.slippage(), equalTo(userSettings.defaultSlippage()));
        assertThat(orderParams.stopLossPrice(), equalTo(platformSettings.noSLPrice()));
        assertThat(orderParams.takeProfitPrice(), equalTo(platformSettings.noTPPrice()));
        assertThat(orderParams.goodTillTime(), equalTo(userSettings.defaultGTT()));
        assertThat(orderParams.comment(), equalTo(""));
    }

    @Test
    public void testAreSpecificValuesCorrect() {
        fillOrderParamsWithOptionalValues();

        assertMandatoryValues(orderParams);
        assertThat(orderParams.price(), equalTo(price));
        assertThat(orderParams.slippage(), equalTo(slippage));
        assertThat(orderParams.stopLossPrice(), equalTo(stopLossPrice));
        assertThat(orderParams.takeProfitPrice(), equalTo(takeProfitPrice));
        assertThat(orderParams.goodTillTime(), equalTo(goodTillTime));
        assertThat(orderParams.comment(), equalTo(comment));
    }

    @Test
    public void testCloneGivesCorrectValues() {
        fillOrderParamsWithOptionalValues();
        orderParamsClone = orderParams
            .clone()
            .forInstrument(instrumentAUDUSD)
            .build();

        assertThat(orderParamsClone.label(), equalTo(label));
        assertThat(orderParamsClone.instrument(), equalTo(instrumentAUDUSD));
        assertThat(orderParamsClone.orderCommand(), equalTo(orderCommand));
        assertThat(orderParamsClone.amount(), equalTo(amount));
        assertThat(orderParamsClone.price(), equalTo(price));
        assertThat(orderParamsClone.slippage(), equalTo(slippage));
        assertThat(orderParamsClone.stopLossPrice(), equalTo(stopLossPrice));
        assertThat(orderParamsClone.takeProfitPrice(), equalTo(takeProfitPrice));
        assertThat(orderParamsClone.goodTillTime(), equalTo(goodTillTime));
        assertThat(orderParamsClone.comment(), equalTo(comment));
    }

    @Test
    public void isEqualsContractOK() {
        fillOrderParamsWithOptionalValues();
        testEqualsContract(orderParams);
    }
}
