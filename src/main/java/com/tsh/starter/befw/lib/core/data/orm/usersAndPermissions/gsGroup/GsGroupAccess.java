package com.tsh.starter.befw.lib.core.data.orm.usersAndPermissions.gsGroup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tsh.starter.befw.lib.core.data.orm.common.access.AbstractCrudService;
import com.tsh.starter.befw.lib.core.data.orm.common.repo.BaseJpaRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class GsGroupAccess extends AbstractCrudService<GsGroupModel, String> {

	@Autowired
	GsGroupRepo repo;

	@Override
	protected BaseJpaRepository<GsGroupModel, String> getRepository() {
		return repo;
	}

}
