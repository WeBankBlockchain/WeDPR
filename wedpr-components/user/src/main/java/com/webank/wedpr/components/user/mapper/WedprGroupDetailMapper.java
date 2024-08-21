package com.webank.wedpr.components.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.webank.wedpr.components.user.entity.WedprGroupDetail;
import com.webank.wedpr.components.user.response.WedprUserResponse;
import org.apache.ibatis.annotations.Param;

/**
 * Mapper 接口
 *
 * @author caryliao
 * @since 2024-07-15
 */
public interface WedprGroupDetailMapper extends BaseMapper<WedprGroupDetail> {

    IPage<WedprUserResponse> selectGroupUserPage(
            Page<WedprUserResponse> page,
            @Param("groupId") String groupId,
            @Param("username") String username);
}
