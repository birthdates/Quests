package com.birthdates.quests.sql;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SQLExecutor extends ThreadPoolExecutor {

    private final Logger logger;

    public SQLExecutor(Logger logger) {
        super(1, 1, 1, TimeUnit.MINUTES, new LinkedBlockingQueue<>());
        this.logger = logger;
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        if (t != null) {
            logger.log(Level.SEVERE, "Failed to execute SQL task", t);
        }
    }
}
