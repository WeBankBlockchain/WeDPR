package com.webank.wedpr.components.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.webank.wedpr.components.user.entity.WedprRolePermission;
import com.webank.wedpr.components.user.entity.result.WedprRolePermissionResult;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * Mapper 接口
 *
 * @author caryliao
 * @since 2024-07-15
 */
public interface WedprRolePermissionMapper extends BaseMapper<WedprRolePermission> {
    @Select(
            "select r.role_id as roleId, r.role_name as roleName,r.permission_id as permissionId, p.permission_name as permissionName, p.permission_content as permissionContent\n"
                    + "        from wedpr_role_permission r , wedpr_permission p where r.permission_id = p.permission_id")
    List<WedprRolePermissionResult> selectRolePermissionAllPage(
            Page<WedprRolePermissionResult> page);

    @Select(
            "select r.role_id as roleId, r.role_name as roleName,r.permission_id as permissionId, p.permission_name as permissionName, p.permission_content as permissionContent\n"
                    + "        from wedpr_role_permission r , wedpr_permission p where r.permission_id = p.permission_id and r.role_id = #{roleId}")
    List<WedprRolePermissionResult> selectRolePermissionPageByRoleId(
            Page<WedprRolePermissionResult> page, @Param("roleId") String roleId);
}
