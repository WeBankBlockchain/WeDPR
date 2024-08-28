package com.webank.wedpr.components.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.webank.wedpr.components.user.entity.WedprUserRole;
import com.webank.wedpr.components.user.entity.result.WedprUserRoleResult;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * Mapper 接口
 *
 * @author caryliao
 * @since 2024-07-15
 */
@Mapper
public interface WedprUserRoleMapper extends BaseMapper<WedprUserRole> {

    @Select(
            "SELECT u.username , u.role_id AS roleId , r.role_name AS roleName  FROM wedpr_user_role u LEFT JOIN wedpr_role_permission r "
                    + "ON u.role_id = r.role_id WHERE u.username = #{username}")
    List<WedprUserRoleResult> getWedprUserRoleByUsername(@Param("username") String username);
}
