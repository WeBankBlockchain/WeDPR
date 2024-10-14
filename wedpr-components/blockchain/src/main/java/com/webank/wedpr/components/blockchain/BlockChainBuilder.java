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
package com.webank.wedpr.components.blockchain;

import com.moandjiezana.toml.Toml;
import com.webank.wedpr.common.utils.WeDPRException;
import java.io.InputStream;
import lombok.SneakyThrows;
import org.fisco.bcos.sdk.v3.BcosSDK;
import org.fisco.bcos.sdk.v3.client.Client;
import org.fisco.bcos.sdk.v3.codec.ContractCodec;
import org.fisco.bcos.sdk.v3.codec.EventEncoder;
import org.fisco.bcos.sdk.v3.config.ConfigOption;
import org.fisco.bcos.sdk.v3.config.model.ConfigProperty;
import org.fisco.bcos.sdk.v3.eventsub.EventSubscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlockChainBuilder {
    private static final Logger logger = LoggerFactory.getLogger(BlockChainBuilder.class);
    private static ConfigOption configOption;

    static {
        try {
            InputStream configStream =
                    BlockChainBuilder.class
                            .getClassLoader()
                            .getResourceAsStream(BlockChainConfig.getChainConfigPath());
            if (configStream == null) {
                throw new WeDPRException(
                        "Load blockchain config for empty config! configFile: "
                                + BlockChainConfig.getChainConfigPath());
            }
            ConfigProperty configProperty = new Toml().read(configStream).to(ConfigProperty.class);
            configOption = new ConfigOption(configProperty);
        } catch (Exception e) {
            logger.error(
                    "BlockChainBuilder: load configuration failed, configPath: {}, error: ",
                    BlockChainConfig.getChainConfigPath(),
                    e);
            System.exit(-1);
        }
    }

    public static BcosSDK getSDK() {
        return new BcosSDK(configOption);
    }

    public static Client getClient() {
        return getSDK().getClient(BlockChainConfig.getGroupId());
    }

    @SneakyThrows(Exception.class)
    public static ContractCodec createContractCodec(Client client) {
        return new ContractCodec(client.getCryptoSuite(), client.isWASM());
    }

    @SneakyThrows(Exception.class)
    public static EventSubscribe buildEventSubscribe(Client client) {
        return EventSubscribe.build(client);
    }

    public static EventEncoder buildEventEncoder(Client client) {
        return new EventEncoder(client.getCryptoSuite());
    }
}
