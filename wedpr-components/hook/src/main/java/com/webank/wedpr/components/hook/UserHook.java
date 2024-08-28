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

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class UserHook {
    private static final Logger logger = LoggerFactory.getLogger(UserHook.class);

    public interface UserCallback {
        boolean interruptOnException();

        void onCreated(String user) throws Exception;

        void onUpdated(String user) throws Exception;

        void onDeleted(String user) throws Exception;
    }

    public enum Action {
        CREATE_USER,
        UPDATE_USER,
        DELETE_USER
    }

    private Map<String, UserCallback> callbacks;

    public synchronized void registerUserCallback(String module, UserCallback callback) {
        callbacks.put(module, callback);
    }

    private synchronized void triggerCallback(Action action, String user) throws Exception {
        if (callbacks.isEmpty()) {
            return;
        }
        for (String module : callbacks.keySet()) {
            UserCallback callback = callbacks.get(module);
            try {
                switch (action) {
                    case CREATE_USER:
                        {
                            callback.onCreated(user);
                            continue;
                        }
                    case UPDATE_USER:
                        {
                            callback.onUpdated(user);
                            continue;
                        }
                    case DELETE_USER:
                        {
                            callback.onDeleted(user);
                            continue;
                        }
                    default:
                        continue;
                }
            } catch (Exception e) {
                logger.warn("Trigger callback for module {} failed, reason: ", module, e);
                if (callback.interruptOnException()) {
                    throw e;
                }
            }
        }
    }

    public synchronized void onUserCreated(String user) throws Exception {
        triggerCallback(Action.CREATE_USER, user);
    }

    public synchronized void onUserUpdated(String user) throws Exception {
        triggerCallback(Action.UPDATE_USER, user);
    }

    public synchronized void onUserDeleted(String user) throws Exception {
        triggerCallback(Action.DELETE_USER, user);
    }
}
