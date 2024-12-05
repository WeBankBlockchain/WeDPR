package com.webank.wedpr.components.scheduler.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.webank.wedpr.common.utils.ObjectMapperFactory;
import com.webank.wedpr.common.utils.WeDPRException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("spdzConnections")
@Data
public class SpdzConnections {

    private static final Logger logger = LoggerFactory.getLogger(SpdzConnections.class);
    private static final String SPDZ_SERVICE_NAME = "SPDZ";

    @Data
    public static class Connection {
        private String ip;
        private int port;

        public Connection(String ip, int port) {
            this.ip = ip;
            this.port = port;
        }
    };

    private final Map<String, Connection> agency2Connection = new ConcurrentHashMap<>();

    public Connection getConnection(String agency) throws WeDPRException {
        Connection connection = agency2Connection.get(agency);
        if (connection == null) {
            logger.error("cannot find spdz connection info, agency: {}", agency);
            throw new WeDPRException("cannot find spdz connection info, agency: " + agency);
        }
        logger.info("get spdz connection: {}", connection);
        return connection;
    }

    public boolean updateConnection(String agency, String spdzEndpoint) {
        try {
            String[] split = spdzEndpoint.split(":");

            if (split.length != 2) {
                throw new IllegalArgumentException(
                        "invalid endpoint format, endpoint: " + spdzEndpoint);
            }

            String ip = split[0].trim();
            int port = Integer.parseInt(split[1].trim());

            Connection connection = new Connection(ip, port);

            Connection oldConnection = agency2Connection.get(agency);
            if (oldConnection == null || !oldConnection.equals(connection)) {
                agency2Connection.remove(agency);
                agency2Connection.put(agency, connection);

                logger.info(
                        "update spdz connection, agency: {}, endpoint: {}", agency, spdzEndpoint);
            } else {
                logger.debug(
                        "update spdz connection, agency: {}, endpoint: {}", agency, spdzEndpoint);
            }

            return true;
        } catch (Exception e) {
            logger.error(
                    "update spdz connection failed, agency: {}, endpoint: {}, e:",
                    agency,
                    spdzEndpoint,
                    e);
        }

        return false;
    }

    public void updateSpdzConnections(GetPeers getPeers) throws JsonProcessingException {
        for (GetPeers.Peer peer : getPeers.getPeers()) {
            String agency = peer.getAgency();

            boolean updateResult = false;
            for (GetPeers.Gateway gateway : peer.getGateway()) {
                for (GetPeers.Front front : gateway.getFrontList()) {
                    String strMeta = front.getMeta();

                    if (strMeta == null || !strMeta.contains(SPDZ_SERVICE_NAME)) {
                        continue;
                    }

                    if (logger.isDebugEnabled()) {
                        logger.debug("agency: {}, meta: {}", agency, strMeta);
                    }

                    GetPeers.Meta meta =
                            ObjectMapperFactory.getObjectMapper()
                                    .readValue(strMeta, GetPeers.Meta.class);

                    if (meta == null) {
                        continue;
                    }

                    for (GetPeers.ServiceInfo serviceInfo : meta.getServiceInfos()) {
                        String serviceName = serviceInfo.getServiceName();
                        String entryPoint = serviceInfo.getEntryPoint();
                        if (serviceName == null || !serviceName.contains(SPDZ_SERVICE_NAME)) {
                            continue;
                        }
                        // SPDZ
                        logger.debug("serviceName: {}, entryPoint: {}", serviceName, entryPoint);

                        updateResult = updateConnection(agency, entryPoint);
                        if (updateResult) {
                            break;
                        }
                    }

                    if (updateResult) {
                        break;
                    }
                }
            }
        }
    }

    public void updateSpdzConnections(String jsonStr) {
        if (jsonStr == null || jsonStr.isEmpty()) {
            return;
        }

        try {
            GetPeers getPeers =
                    ObjectMapperFactory.getObjectMapper().readValue(jsonStr, GetPeers.class);
            updateSpdzConnections(getPeers);
        } catch (Exception e) {
            logger.error("e: ", e);
        }
    }
}
