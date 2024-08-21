package com.webank.wedpr.components.dataset.utils;

import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadPoolUtils {

    private static final Logger logger = LoggerFactory.getLogger(ThreadPoolUtils.class);

    private ThreadPoolUtils() {}

    /**
     * The thread pool executes tasks, catches exceptions, and prints error messages
     *
     * @param executor
     * @param taskName
     * @param taskId
     * @param task
     */
    public static void execute(Executor executor, String taskName, String taskId, Runnable task) {
        try {
            executor.execute(task);
        } catch (RejectedExecutionException rej) {
            logger.error(
                    "the task cannot be executed for the thread pool queue is full, taskName: {}, taskId: {}, thread pool state: ",
                    taskName,
                    taskId,
                    rej);
        } catch (Exception e) {
            logger.error(
                    "the thread pool failed to execute the task, with an exception being thrown, taskName: {}, taskId: {}, e: ",
                    taskName,
                    taskId,
                    e);
        }
    }
}
