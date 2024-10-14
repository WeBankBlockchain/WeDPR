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

package com.webank.wedpr.components.authorization.core;

import com.webank.wedpr.common.utils.WeDPRException;
import com.webank.wedpr.components.authorization.dao.AuthorizationDO;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthNotifier {
    private static final Logger logger = LoggerFactory.getLogger(AuthNotifier.class);

    public static class ExecuteArgs {
        private AuthorizationDO authData;

        public ExecuteArgs() {}

        public ExecuteArgs(AuthorizationDO authData) {
            setAuthData(authData);
        }

        public AuthorizationDO getAuthData() {
            return authData;
        }

        public void setAuthData(AuthorizationDO authData) {
            this.authData = authData;
        }
    }

    @FunctionalInterface
    public interface NotifyHandler {
        void execute(ExecuteArgs args) throws WeDPRException;
    }

    private HashMap<String, NotifyHandler> notifyHandlers = new HashMap<>();
    private List<String> applyTypeListWithHandler = new ArrayList<>();

    public synchronized void registerNotifyHandler(String authType, NotifyHandler notifyHandler) {
        if (!notifyHandlers.containsKey(authType)) {
            notifyHandlers.put(authType, notifyHandler);
            applyTypeListWithHandler.add(authType);
            logger.info("registerNotifyHandler for {}", authType);
        }
    }

    public NotifyHandler getNotifyHandler(String authType) {
        if (notifyHandlers.containsKey(authType)) {
            return notifyHandlers.get(authType);
        }
        return null;
    }

    public List<String> getApplyTypeListWithHandler() {
        return applyTypeListWithHandler;
    }
}
