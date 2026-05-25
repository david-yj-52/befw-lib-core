package com.tsh.starter.befw.lib.core.data.orm.usersAndPermissions.gsPermission;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tsh.starter.befw.lib.core.data.orm.common.access.AbstractCrudService;
import com.tsh.starter.befw.lib.core.data.orm.common.repo.BaseJpaRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class GsPermissionAccess extends AbstractCrudService<GsPermissionModel, String> {

	@Autowired
	GsPermissionRepo repo;

	@Override
	protected BaseJpaRepository<GsPermissionModel, String> getRepository() {
		return repo;
	}

}
