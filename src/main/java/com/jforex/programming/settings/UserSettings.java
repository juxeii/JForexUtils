package com.jforex.programming.settings;

import org.aeonbits.owner.Config;

public interface UserSettings extends Config {

    @DefaultValue("2")
    public double ORDER_DEFAULT_SLIPPAGE();

    @DefaultValue("0")
    public double ORDER_DEFAULT_PRICE();

    @DefaultValue("0")
    public long ORDER_DEFAULT_GOOD_TILL_TIME();

    @DefaultValue("")
    public String ORDER_DEFAULT_COMMENT();

    @DefaultValue("M_")
    public String ORDER_MERGE_LABEL_PREFIX();

    @DefaultValue("true")
    public boolean ENABLE_WEEKEND_QUOTE_FILTER();
}
