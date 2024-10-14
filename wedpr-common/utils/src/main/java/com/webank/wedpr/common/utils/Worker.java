/*
 * Copyright 2017-2025  [webank-wedpr]
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 */
package com.webank.wedpr.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Worker {
    private static final Logger logger = LoggerFactory.getLogger(Worker.class);
    private boolean running = false;
    private Thread workerThread = null;
    private final Object lock = new Object();
    private final String workerName;
    private final Integer idleWaitMs;

    public Worker(String workerName, Integer idleWaitMs) {
        this.workerName = workerName;
        this.idleWaitMs = idleWaitMs;
    }

    protected abstract void execute();

    public void startWorking() {
        // the worker has already started
        if (running || workerThread != null) {
            logger.warn("Worker {} already started!", workerName);
            return;
        }
        workerThread =
                new Thread(
                        new Runnable() {
                            @Override
                            public void run() {
                                runWorkLoop();
                            }
                        });
        workerThread.setDaemon(true);
        workerThread.setName(workerName);
        workerThread.start();
        this.running = true;
        logger.info("startWorking for {} success", workerName);
    }

    protected void runWorkLoop() {
        while (running) {
            synchronized (lock) {
                try {
                    lock.wait(idleWaitMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                try {
                    execute();
                } catch (Exception e) {
                    logger.error("{} execute error: ", workerName, e);
                }
            }
        }
    }

    public void stop() {
        logger.info("stop worker: {}", workerName);
        this.running = true;
    }

    public void wakeupWorker() {
        synchronized (lock) {
            lock.notifyAll();
        }
    }
}
