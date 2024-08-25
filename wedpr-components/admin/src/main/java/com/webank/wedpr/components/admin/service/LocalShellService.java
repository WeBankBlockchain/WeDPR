package com.webank.wedpr.components.admin.service;

/** Created by caryliao on 2024/8/24 20:16 */
public interface LocalShellService {
    boolean buildAuthorityCsrToCrt(String agencyName, String csrPath, long days);
}
