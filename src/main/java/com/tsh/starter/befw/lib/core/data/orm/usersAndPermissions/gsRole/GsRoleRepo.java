package com.tsh.starter.befw.lib.core.data.orm.usersAndPermissions.gsRole;

import org.springframework.stereotype.Repository;

import com.tsh.starter.befw.lib.core.data.orm.common.repo.BaseJpaRepository;

@Repository
public interface GsRoleRepo extends BaseJpaRepository<GsRoleModel, String> {
}
