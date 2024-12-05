package com.webank.wedpr.components.scheduler.config;

import com.webank.wedpr.components.scheduler.core.SpdzConnections;
import com.webank.wedpr.sdk.jni.generated.Error;
import com.webank.wedpr.sdk.jni.transport.WeDPRTransport;
import com.webank.wedpr.sdk.jni.transport.handlers.GetPeersCallback;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpdzConnectionsConfig {

    private static final Logger logger = LoggerFactory.getLogger(SpdzConnectionsConfig.class);

    @Qualifier("weDPRTransport")
    @Autowired
    private WeDPRTransport weDPRTransport;

    @Qualifier("spdzConnections")
    @Autowired
    private SpdzConnections spdzConnections;

    @Bean
    public void initUpdateSpdzConnectionsTask() throws Exception {

        logger.info("init spdz connection update period task");

        ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(1);

        scheduledExecutorService.scheduleAtFixedRate(
                new Runnable() {
                    @Override
                    public void run() {
                        weDPRTransport.asyncGetPeers(
                                new GetPeersCallback() {
                                    @Override
                                    public void onPeers(Error error, String jsonStr) {
                                        spdzConnections.updateSpdzConnections(jsonStr);
                                    }
                                });
                    }
                },
                10,
                30,
                TimeUnit.SECONDS);
    }
}
