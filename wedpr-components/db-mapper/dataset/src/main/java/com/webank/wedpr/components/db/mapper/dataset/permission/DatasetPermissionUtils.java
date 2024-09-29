package com.webank.wedpr.components.db.mapper.dataset.permission;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class DatasetPermissionUtils {

    private static final String SubjectSplitChar = "|";

    private DatasetPermissionUtils() {}

    public static String toSubjectStr(String user, String agency) {
        // return user + SubjectSplitChar + agency;
        return agency + SubjectSplitChar + user;
    }

    public static List<String> toSubjectStrList(List<String> userGroupList, String agency) {
        List<String> resultList = new ArrayList<>();
        if (userGroupList != null && !userGroupList.isEmpty()) {
            for (String userGroup : userGroupList) {
                resultList.add(toSubjectStr(userGroup, agency));
            }
        }

        return resultList;
    }

    public static Pair<String, String> fromSubjectStr(String str) {
        String[] split = str.split("\\|");
        if (split.length == 2) {
            return new MutablePair<>(split[0], split[1]);
        }

        return new MutablePair<>(str, null);
    }
}
