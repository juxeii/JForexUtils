package com.jforex.programming.settings;

import org.aeonbits.owner.Config;

public interface PlatformSettings extends Config {

    @DefaultValue("10")
    public double MIN_SL_TP_DISTANCE_PIPS();

    @DefaultValue("0")
    public double NO_STOP_LOSS_PRICE();

    @DefaultValue("0")
    public double NO_TAKE_PROFIT_PRICE();

    @DefaultValue("0.001")
    public double AMOUNT_MIN();

    @DefaultValue("1")
    public double AMOUNT_MAX();

    @DefaultValue("1000000")
    public double AMOUNT_BASE();

    @DefaultValue("6")
    public int AMOUNT_PRECISION();

    @DefaultValue("4")
    public int PIPVALUE_PRECISION();

    @DefaultValue("1")
    public int PIP_FRACTION();

    @DefaultValue("1")
    public int PIP_PRECISION();

    @DefaultValue("Strategy")
    public String STRATEGY_THREAD_PREFIX();

    @DefaultValue("\\w{1,256}")
    public String VALID_LABEL_REGEX();

    @DefaultValue("6")
    public String Strategy();

    @DefaultValue("Connected")
    public String CONNECTED_STRING();

    @DefaultValue("Disconnected")
    public String DISCONNECTED_STRING();

    @DefaultValue("3")
    public int MAX_NUM_RETRIES_ON_FAIL();

    @DefaultValue("1500")
    public long ON_FAIL_RETRY_WAITING_TIME();

    @DefaultValue("100")
    public long EXECUTORSERVICE_AWAITTERMINATION_TIMEOUT();
}
