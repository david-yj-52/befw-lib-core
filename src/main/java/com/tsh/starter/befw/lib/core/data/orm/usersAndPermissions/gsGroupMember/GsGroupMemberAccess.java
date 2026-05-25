package com.tsh.starter.befw.lib.core.data.orm.usersAndPermissions.gsGroupMember;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tsh.starter.befw.lib.core.data.orm.common.access.AbstractCrudService;
import com.tsh.starter.befw.lib.core.data.orm.common.repo.BaseJpaRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class GsGroupMemberAccess extends AbstractCrudService<GsGroupMemberModel, String> {

	@Autowired
	GsGroupMemberRepo repo;

	@Override
	protected BaseJpaRepository<GsGroupMemberModel, String> getRepository() {
		return repo;
	}

}
