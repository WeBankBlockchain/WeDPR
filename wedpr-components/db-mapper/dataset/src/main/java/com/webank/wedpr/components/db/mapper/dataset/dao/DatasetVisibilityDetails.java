package com.webank.wedpr.components.db.mapper.dataset.dao;

import java.util.List;
import lombok.Data;

@Data
public class DatasetVisibilityDetails {

    @Data
    public static class AgencyUser {
        private String agency;
        private String user;
    }

    // global visibility
    private boolean global = false;
    // self agency visibility
    private boolean selfAgency = false;
    // self user group visibility
    private boolean selfUserGroup = false;
    // user group list, effect when selfUserGroup is true
    private List<String> groupIdList;
    // agency list
    private List<String> agencyList;
    // user list
    private List<AgencyUser> userList;
}
