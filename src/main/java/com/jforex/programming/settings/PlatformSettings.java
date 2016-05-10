package com.jforex.programming.settings;

import org.aeonbits.owner.Config;

public interface PlatformSettings extends Config {

    @DefaultValue("10.0")
    public double minSLPipDistance();

    @DefaultValue("10.0")
    public double minTPPipDistance();

    @DefaultValue("0.0")
    public double noSLPrice();

    @DefaultValue("0.0")
    public double noTPPrice();

    @DefaultValue("0.001")
    public double minAmount();

    @DefaultValue("1.0")
    public double maxAmount();

    @DefaultValue("1000000.0")
    public double baseAmount();

    @DefaultValue("6")
    public int amountPrecision();

    @DefaultValue("4")
    public int pipValuePrecision();

    @DefaultValue("1")
    public int pipFraction();

    @DefaultValue("1")
    public int pipPrecision();

    @DefaultValue("Strategy")
    public String strategyThreadPrefix();

    @DefaultValue("\\w{1,256}")
    public String labelRegex();

    @DefaultValue("3")
    public int maxRetriesOnOrderFail();

    @DefaultValue("1500")
    public long delayOnOrderFailRetry();

    @DefaultValue("100")
    public long terminationTimeoutExecutorService();
}
