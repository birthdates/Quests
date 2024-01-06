package com.birthdates.quests.util;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Executor for SQL tasks (will print errors to given logger)
 */
public class VerboseExecutor extends ScheduledThreadPoolExecutor {
    private final Logger logger;

    public VerboseExecutor(Logger logger) {
        super(1);
        this.logger = logger;
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        if (t != null) {
            logger.log(Level.SEVERE, "Failed to execute task", t);
        }
    }
}
