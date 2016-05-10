package com.jforex.programming.settings;

import org.aeonbits.owner.Config;

public interface UserSettings extends Config {

    @DefaultValue("2.0")
    public double defaultSlippage();

    @DefaultValue("0.0")
    public double defaultOpenPrice();

    @DefaultValue("0")
    public long defaultGTT();

    @DefaultValue("")
    public String defaultOrderComment();

    @DefaultValue("M_")
    public String defaultMergePrefix();

    @DefaultValue("true")
    public boolean enableWeekendQuoteFilter();
}
