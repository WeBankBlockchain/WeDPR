package com.webank.wedpr.components.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.webank.wedpr.components.user.entity.WedprUserRole;
import com.webank.wedpr.components.user.entity.result.WedprUserRoleResult;
import com.webank.wedpr.components.user.mapper.WedprUserRoleMapper;
import com.webank.wedpr.components.user.service.WedprUserRoleService;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * 服务实现类
 *
 * @author caryliao
 * @since 2024-07-15
 */
@Service
public class WedprUserRoleImplService extends ServiceImpl<WedprUserRoleMapper, WedprUserRole>
        implements WedprUserRoleService {
    @Override
    public List<WedprUserRoleResult> getWedprUserRoleByUsername(String username) {
        return this.baseMapper.getWedprUserRoleByUsername(username);
    }

    @Override
    public Boolean isRoleAssignToUserService(String roleId) {
        LambdaQueryWrapper<WedprUserRole> wrapperCount =
                new LambdaQueryWrapper<WedprUserRole>().eq(WedprUserRole::getRoleId, roleId);
        long queriedWedprUserRoleCount = this.count(wrapperCount);
        return queriedWedprUserRoleCount > 0;
    }
}
