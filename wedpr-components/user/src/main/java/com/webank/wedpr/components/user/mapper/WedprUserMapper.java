package com.webank.wedpr.components.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.webank.wedpr.components.user.entity.WedprUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * Mapper 接口
 *
 * @author caryliao
 * @since 2024-07-15
 */
@Mapper
public interface WedprUserMapper extends BaseMapper<WedprUser> {

    @Update(
            "update wedpr_user set try_count = #{tryCount}, allowed_timestamp = #{allowTimestamp} where username = #{username}")
    void updateAllowedTimeAndTryCount(
            @Param("username") String username,
            @Param("allowTimestamp") Long allowedTimestamp,
            @Param("tryCount") Integer tryCount);
}
