package com.webank.wedpr.components.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.webank.wedpr.components.user.entity.WedprGroupDetail;
import com.webank.wedpr.components.user.mapper.WedprGroupDetailMapper;
import com.webank.wedpr.components.user.response.WedprUserResponse;
import com.webank.wedpr.components.user.service.WedprGroupDetailService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 服务实现类
 *
 * @author caryliao
 * @since 2024-07-15
 */
@Service
public class WedprGroupDetailImplService
        extends ServiceImpl<WedprGroupDetailMapper, WedprGroupDetail>
        implements WedprGroupDetailService {

    @Autowired private WedprGroupDetailMapper wedprGroupDetailMapper;

    public IPage<WedprUserResponse> selectGroupUserPage(
            Page<WedprUserResponse> page, String groupId, String username) {
        return wedprGroupDetailMapper.selectGroupUserPage(page, groupId, username);
    }

    public Integer selectUserCount(List<String> adminGroupIds) {
        QueryWrapper<WedprGroupDetail> queryWrapper = new QueryWrapper<>();
        LambdaQueryWrapper<WedprGroupDetail> lambdaQueryWrapper =
                queryWrapper
                        .select("DISTINCT username")
                        .lambda()
                        .in(adminGroupIds.size() > 0, WedprGroupDetail::getGroupId, adminGroupIds);
        return wedprGroupDetailMapper.selectCount(lambdaQueryWrapper);
    }
}
