package com.tsh.starter.befw.lib.core.data.orm.usersAndPermissions.gsPermission;

import org.springframework.stereotype.Repository;

import com.tsh.starter.befw.lib.core.data.orm.common.repo.BaseJpaRepository;

@Repository
public interface GsPermissionRepo extends BaseJpaRepository<GsPermissionModel, String> {
}
