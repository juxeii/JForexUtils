package com.jforex.programming.settings;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.Sources;

@Sources({ "file:PlatformSettings.properties" })
public interface PlatformSettings extends Config {

    @Key("order.minslpipdistance")
    @DefaultValue("10.0")
    public double minSLPipDistance();

    @Key("order.mintppipdistance")
    @DefaultValue("10.0")
    public double minTPPipDistance();

    @Key("order.noslprice")
    @DefaultValue("0.0")
    public double noSLPrice();

    @Key("order.notpprice")
    @DefaultValue("0.0")
    public double noTPPrice();

    @Key("order.minamount")
    @DefaultValue("0.001")
    public double minAmount();

    @Key("order.maxamount")
    @DefaultValue("1.0")
    public double maxAmount();

    @Key("order.defaultcloseslippage")
    @DefaultValue("5.0")
    public double defaultCloseSlippage();

    @Key("env.baseamount")
    @DefaultValue("1000000.0")
    public double baseAmount();

    @Key("env.maxexposure")
    @DefaultValue("25")
    public double maxExposure();

    @Key("math.amountprecision")
    @DefaultValue("6")
    public int amountPrecision();

    @Key("math.pipvalueprecision")
    @DefaultValue("4")
    public int pipValuePrecision();

    @Key("math.pipfraction")
    @DefaultValue("1")
    public int pipFraction();

    @Key("math.pipprecision")
    @DefaultValue("1")
    public int pipPrecision();

    @Key("env.strategythreadprefix")
    @DefaultValue("Strategy")
    public String strategyThreadPrefix();

    @Key("order.labelregex")
    @DefaultValue("\\w{1,256}")
    public String labelRegex();
}
