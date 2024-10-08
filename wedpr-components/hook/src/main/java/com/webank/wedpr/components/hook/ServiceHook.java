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

package com.webank.wedpr.components.hook;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ServiceHook {
    private static final Logger logger = LoggerFactory.getLogger(UserHook.class);

    public interface ServiceCallback {
        boolean interruptOnException();

        void onPublish(Object serviceInfo) throws Exception;
    }

    public enum ServiceAction {
        PUBLISH
    }

    private Map<String, ServiceHook.ServiceCallback> callbacks = new HashMap<>();

    public synchronized void registerServiceCallback(String serviceType, ServiceCallback callback) {
        callbacks.put(serviceType, callback);
    }

    private synchronized void triggerCallback(ServiceHook.ServiceAction action, Object serviceInfo)
            throws Exception {
        if (callbacks.isEmpty()) {
            return;
        }
        for (String serviceType : callbacks.keySet()) {
            ServiceHook.ServiceCallback callback = callbacks.get(serviceType);
            try {
                switch (action) {
                    case PUBLISH:
                        {
                            callback.onPublish(serviceInfo);
                            continue;
                        }
                    default:
                        continue;
                }
            } catch (Exception e) {
                logger.warn("Trigger callback for module {} failed, reason: ", serviceType, e);
                if (callback.interruptOnException()) {
                    throw e;
                }
            }
        }
    }

    public synchronized void onPublish(Object publishedService) throws Exception {
        triggerCallback(ServiceAction.PUBLISH, publishedService);
    }
}
