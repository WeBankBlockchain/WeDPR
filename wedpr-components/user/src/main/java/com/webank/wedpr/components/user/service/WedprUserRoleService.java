package com.webank.wedpr.components.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.webank.wedpr.components.user.entity.WedprUserRole;
import com.webank.wedpr.components.user.entity.result.WedprUserRoleResult;
import java.util.List;

/**
 * 服务类
 *
 * @author caryliao
 * @since 2024-07-15
 */
public interface WedprUserRoleService extends IService<WedprUserRole> {

    List<WedprUserRoleResult> getWedprUserRoleByUsername(String username);

    /** roleId 是否被设置给其他人员 */
    Boolean isRoleAssignToUserService(String roleId);
}
