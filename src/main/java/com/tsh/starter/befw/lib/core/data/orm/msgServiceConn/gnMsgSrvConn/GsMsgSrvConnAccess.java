package com.tsh.starter.befw.lib.core.data.orm.msgServiceConn.gnMsgSrvConn;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tsh.starter.befw.lib.core.data.constant.UseStatCd;
import com.tsh.starter.befw.lib.core.data.orm.common.access.AbstractCrudService;
import com.tsh.starter.befw.lib.core.data.orm.common.repo.BaseJpaRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class GsMsgSrvConnAccess extends AbstractCrudService<GsMsgSrvConnModel, String> {

	@Autowired
	GsMsgSrvConnRepo repo;

	@Override
	protected BaseJpaRepository<GsMsgSrvConnModel, String> getRepository() {
		return repo;
	}

	public List<GsMsgSrvConnModel> findByTenantAndEnv(String tenant, String env) {
		return this.repo.findByTenantAndEnvAndUseStatCd(tenant, env, UseStatCd.Usable);
	}

}
