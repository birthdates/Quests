package com.birthdates.quests.util;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Executor that will log any exceptions thrown by tasks
 */
public class VerboseExecutor extends ScheduledThreadPoolExecutor {
    private final Logger logger;

    public VerboseExecutor(Logger logger) {
        super(1);
        this.logger = logger;
    }

    /**
     * {@link java.util.concurrent.ThreadPoolExecutor#afterExecute(Runnable, Throwable)} is called with no exception always for {@link ScheduledThreadPoolExecutor}
     *
     * @param task the task to submit
     * @return the future
     */
    @NotNull
    @Override
    public Future<?> submit(@NotNull Runnable task) {
        return super.submit(() -> {
            try {
                task.run();
            } catch (Exception exception) {
                logger.log(Level.SEVERE, "Failed to execute task", exception);
            }
        });
    }
}
