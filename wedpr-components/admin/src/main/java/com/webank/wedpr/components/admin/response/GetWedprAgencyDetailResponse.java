package com.webank.wedpr.components.admin.response;

import lombok.Data;

/** Created by caryliao on 2024/8/22 23:23 */
@Data
public class GetWedprAgencyDetailResponse {
    private String agencyId;
    private String agencyName;
    private String agencyDesc;
    private String agencyContact;
    private String contactPhone;
    private String gatewayEndpoint;
}