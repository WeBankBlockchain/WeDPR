package com.webank.wedpr.components.scheduler.core;

import java.util.List;
import lombok.Data;

@Data
public class GetPeers {

    @Data
    public static class Meta {
        private List<ServiceInfo> serviceInfos;

        // getters and setters
    }

    @Data
    public static class Front {
        private List<String> components;
        private String endPoint;
        private String meta;
        private String nodeID;

        // getters and setters
    }

    @Data
    public static class Gateway {
        private String agency;
        private List<Front> frontList;
        private String gatewayNodeID;

        // getters and setters
    }

    @Data
    public static class Peer {
        private String agency;
        private List<Gateway> gateway;

        // getters and setters
    }

    @Data
    public static class ServiceInfo {
        private String entryPoint;
        private String serviceName;
        private List<String> components;

        // getters and setters
    }

    private String agency;
    private Gateway gateway;
    private String nodeID;
    private List<Peer> peers;

    // getters and setters
}
