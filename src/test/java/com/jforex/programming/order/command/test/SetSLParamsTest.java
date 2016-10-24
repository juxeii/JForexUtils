package com.jforex.programming.order.command.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.dukascopy.api.OfferSide;
import com.jforex.programming.order.command.SetSLParams;
import com.jforex.programming.test.common.QuoteProviderForTest;

public class SetSLParamsTest extends QuoteProviderForTest {

    private SetSLParams setSLParams;

    private final double newSL = askEURUSD;

    @Test
    public void defaultParamsAreCorrectForLongOrder() {
        setSLParams = SetSLParams
            .newBuilder(buyOrderEURUSD, newSL)
            .build();

        assertThat(setSLParams.order(), equalTo(buyOrderEURUSD));
        assertThat(setSLParams.newSL(), equalTo(newSL));
        assertThat(setSLParams.offerSide(), equalTo(OfferSide.BID));
        assertThat(setSLParams.trailingStep(), equalTo(-1.0));
    }

    @Test
    public void defaultParamsAreCorrectForShortOrder() {
        setSLParams = SetSLParams
            .newBuilder(sellOrderEURUSD, newSL)
            .build();

        assertThat(setSLParams.order(), equalTo(sellOrderEURUSD));
        assertThat(setSLParams.newSL(), equalTo(newSL));
        assertThat(setSLParams.offerSide(), equalTo(OfferSide.ASK));
        assertThat(setSLParams.trailingStep(), equalTo(-1.0));
    }

    @Test
    public void paramsAreCorrectWithTrailingStep() {
        final double trailingStep = 11.1;

        setSLParams = SetSLParams
            .newBuilder(buyOrderEURUSD, newSL)
            .withOfferSide(OfferSide.ASK)
            .withTrailingStep(trailingStep)
            .build();

        assertThat(setSLParams.order(), equalTo(buyOrderEURUSD));
        assertThat(setSLParams.newSL(), equalTo(newSL));
        assertThat(setSLParams.offerSide(), equalTo(OfferSide.ASK));
        assertThat(setSLParams.trailingStep(), equalTo(trailingStep));
    }
}
