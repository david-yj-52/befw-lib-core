package com.tsh.starter.befw.lib.core.data.orm.usersAndPermissions.gsUser;

import org.springframework.stereotype.Repository;

import com.tsh.starter.befw.lib.core.data.orm.common.repo.BaseJpaRepository;

@Repository
public interface GsUserRepo extends BaseJpaRepository<GsUserModel, String> {
}
