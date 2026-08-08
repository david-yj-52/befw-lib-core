package com.tsh.starter.befw.lib.core.data.orm.usersAndPermissions.gsGroup;

import org.springframework.stereotype.Repository;

import com.tsh.starter.befw.lib.core.data.orm.common.repo.BaseJpaRepository;

@Repository
public interface GsGroupRepo extends BaseJpaRepository<GsGroupModel, String> {
}
