package com.tsh.starter.befw.lib.core.data.orm.gnMsgSrvConn;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tsh.starter.befw.lib.core.data.orm.common.access.AbstractCrudService;
import com.tsh.starter.befw.lib.core.data.orm.common.repo.BaseJpaRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class GnMsgSrvConnAccess extends AbstractCrudService<GnMsgSrvConnModel, String> {

	@Autowired
	GnMsgSrvConnRepo repo;

	@Override
	protected BaseJpaRepository<GnMsgSrvConnModel, String> getRepository() {
		return repo;
	}

}
