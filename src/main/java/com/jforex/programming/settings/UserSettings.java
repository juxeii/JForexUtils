package com.jforex.programming.settings;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.Sources;

@Sources({ "file:UserSettings.properties" })
public interface UserSettings extends Config {

    @Key("order.defaultslippage")
    @DefaultValue("2.0")
    public double defaultSlippage();

    @Key("order.defaultopenprice")
    @DefaultValue("0.0")
    public double defaultOpenPrice();

    @Key("order.defaultgtt")
    @DefaultValue("0")
    public long defaultGTT();

    @Key("order.defaultcomment")
    @DefaultValue("")
    public String defaultOrderComment();

    @Key("order.defaultmergeprefix")
    @DefaultValue("M_")
    public String defaultMergePrefix();

    @Key("history.maxretriesonhistoryfail")
    @DefaultValue("5")
    public int maxRetriesOnHistoryFail();

    @Key("history.retrydelayonhistoryfail")
    @DefaultValue("500")
    public long delayOnHistoryFailRetry();

    @Key("env.enableweekendquotefilter")
    @DefaultValue("true")
    public boolean enableWeekendQuoteFilter();

    @Key("env.dateformat")
    @DefaultValue("yyyy-MM-dd HH:mm:ss.SSS")
    public String dateFormat();
}
