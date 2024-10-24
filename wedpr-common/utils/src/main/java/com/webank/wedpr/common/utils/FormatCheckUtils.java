package com.webank.wedpr.common.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Created by caryliao on 2024/10/24 14:44 */
public class FormatCheckUtils {

    /** 13位正常密码 */
    public static final String PHONE_NUMBER_PATTERN = "^[1]([3-9])[0-9]{9}$";
    /** ip或者域名：port形式 */
    public static final String GATEWAY_ENDPOINT_PATTERN =
            "^([a-zA-Z0-9.-]+|\\d{1,3}(\\.\\d{1,3}){3}):\\d{1,5}$";

    public static final String USERNAME_PATTERN = "^[a-zA-Z0-9_-]{3,18}$";

    public static final String EMAIL_PATTERN =
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";

    public static boolean checkParamFormat(String param, String patternStr) {
        Pattern pattern = Pattern.compile(patternStr);
        Matcher matcher = pattern.matcher(param);
        return matcher.matches();
    }
}
