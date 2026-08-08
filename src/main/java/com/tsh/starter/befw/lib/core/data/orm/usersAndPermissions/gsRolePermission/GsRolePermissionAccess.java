package com.tsh.starter.befw.lib.core.data.orm.usersAndPermissions.gsRolePermission;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tsh.starter.befw.lib.core.data.orm.common.access.AbstractCrudService;
import com.tsh.starter.befw.lib.core.data.orm.common.repo.BaseJpaRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class GsRolePermissionAccess extends AbstractCrudService<GsRolePermissionModel, String> {

	@Autowired
	GsRolePermissionRepo repo;

	@Override
	protected BaseJpaRepository<GsRolePermissionModel, String> getRepository() {
		return repo;
	}

}
