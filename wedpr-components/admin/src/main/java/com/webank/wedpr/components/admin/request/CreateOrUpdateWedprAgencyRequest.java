package com.webank.wedpr.components.admin.request;

import javax.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/** Created by caryliao on 2024/8/22 16:58 */
@Data
public class CreateOrUpdateWedprAgencyRequest {

    @Length(max = 64, message = "agencyId at most 64 chars")
    private String agencyId;

    @NotBlank(message = "agencyName is not be empty")
    @Length(max = 64, message = "agencyName at most 64 chars")
    private String agencyName;

    @Length(max = 1000, message = "agencyDesc at most 1000 chars")
    private String agencyDesc;

    @NotBlank(message = "agencyContact is not be empty")
    @Length(max = 64, message = "agencyContact at most 64 chars")
    private String agencyContact;

    @NotBlank(message = "phone is not be empty")
    private String contactPhone;

    @NotBlank(message = "gatewayEndpoint is not be empty")
    @Length(max = 64, message = "gatewayEndpoint at most 64 chars")
    private String gatewayEndpoint;
}
