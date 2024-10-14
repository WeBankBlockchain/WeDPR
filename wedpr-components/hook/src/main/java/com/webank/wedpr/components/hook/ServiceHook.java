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

import com.webank.wedpr.common.utils.WeDPRException;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ServiceHook {
    private static final Logger logger = LoggerFactory.getLogger(UserHook.class);

    public interface ServiceCallback {
        abstract void onPublish(Object serviceInfo) throws Exception;

        abstract void onInvoke(Object serviceInvokeInfo) throws Exception;
    }

    public enum ServiceAction {
        PUBLISH,
        INVOKE
    }

    private Map<String, ServiceHook.ServiceCallback> callbacks = new HashMap<>();

    public synchronized void registerServiceCallback(String serviceType, ServiceCallback callback) {
        callbacks.put(serviceType, callback);
    }

    private synchronized boolean triggerCallback(
            ServiceHook.ServiceAction action, String serviceType, Object serviceInfo)
            throws Exception {
        if (callbacks.isEmpty()) {
            return false;
        }
        if (!callbacks.containsKey(serviceType)) {
            return false;
        }
        ServiceHook.ServiceCallback callback = callbacks.get(serviceType);
        switch (action) {
            case PUBLISH:
                {
                    callback.onPublish(serviceInfo);
                    break;
                }
            case INVOKE:
                {
                    callback.onInvoke(serviceInfo);
                    break;
                }
            default:
                throw new WeDPRException("Unsupported action: " + action);
        }

        return true;
    }

    public synchronized boolean onPublish(String serviceType, Object publishedService)
            throws Exception {
        return triggerCallback(ServiceAction.PUBLISH, serviceType, publishedService);
    }

    public boolean onInvoke(String serviceType, Object invokeRecorder) throws Exception {
        return triggerCallback(ServiceAction.INVOKE, serviceType, invokeRecorder);
    }
}
