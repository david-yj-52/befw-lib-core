package com.tsh.starter.befw.lib.core.data.orm.usersAndPermissions.gsUserRole;

import org.springframework.stereotype.Repository;

import com.tsh.starter.befw.lib.core.data.orm.common.repo.BaseJpaRepository;

@Repository
public interface GsUserRoleRepo extends BaseJpaRepository<GsUserRoleModel, String> {
}
