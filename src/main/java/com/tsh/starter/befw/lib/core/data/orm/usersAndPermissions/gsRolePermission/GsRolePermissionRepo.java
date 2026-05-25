package com.tsh.starter.befw.lib.core.data.orm.usersAndPermissions.gsRolePermission;

import org.springframework.stereotype.Repository;

import com.tsh.starter.befw.lib.core.data.orm.common.repo.BaseJpaRepository;

@Repository
public interface GsRolePermissionRepo extends BaseJpaRepository<GsRolePermissionModel, String> {
}
