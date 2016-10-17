package com.jforex.programming.init;

import com.dukascopy.api.IAccount;
import com.dukascopy.api.IContext;
import com.dukascopy.api.IDataService;
import com.dukascopy.api.IEngine;
import com.dukascopy.api.IHistory;
import com.jforex.programming.misc.DateTimeUtil;
import com.jforex.programming.misc.HistoryUtil;

public class ContextUtil {

    private final IContext context;
    private final IEngine engine;
    private final IAccount account;
    private final IHistory history;
    private final HistoryUtil historyUtil;
    private final IDataService dataService;

    public ContextUtil(final IContext context) {
        this.context = context;
        engine = context.getEngine();
        account = context.getAccount();
        history = context.getHistory();
        dataService = context.getDataService();
        historyUtil = new HistoryUtil(history);
    }

    public IContext context() {
        return context;
    }

    public IEngine engine() {
        return engine;
    }

    public IAccount account() {
        return account;
    }

    public IHistory history() {
        return history;
    }

    public HistoryUtil historyUtil() {
        return historyUtil;
    }

    public boolean isMarketClosed() {
        return isMarketClosed(DateTimeUtil.localMillisNow());
    }

    public boolean isMarketClosed(final long time) {
        return dataService.isOfflineTime(time);
    }
}
