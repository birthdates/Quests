package com.birthdates.quests.util;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
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

    private Runnable wrapTask(Runnable task) {
        return () -> {
            try {
                task.run();
            } catch (Exception exception) {
                logger.log(Level.SEVERE, "Failed to execute task", exception);
            }
        };
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
        return super.submit(wrapTask(task));
    }

    @Override
    public @NotNull ScheduledFuture<?> scheduleAtFixedRate(@NotNull Runnable command, long initialDelay, long period, @NotNull TimeUnit unit) {
        return super.scheduleAtFixedRate(wrapTask(command), initialDelay, period, unit);
    }
}
