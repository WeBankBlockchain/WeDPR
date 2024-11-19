package com.webank.wedpr.components.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.webank.wedpr.components.user.entity.WedprGroupDetail;
import com.webank.wedpr.components.user.response.WedprUserResponse;
import java.util.List;

/**
 * 服务类
 *
 * @author caryliao
 * @since 2024-07-15
 */
public interface WedprGroupDetailService extends IService<WedprGroupDetail> {
    IPage<WedprUserResponse> selectGroupUserPage(
            Page<WedprUserResponse> page, String groupId, String username);

    Long selectUserCount(List<String> adminGroupIds);
}
