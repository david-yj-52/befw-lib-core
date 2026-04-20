package com.tsh.starter.befw.lib.core.data.orm.gnMsgSrvConn;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.tsh.starter.befw.lib.core.data.constant.UseStatCd;
import com.tsh.starter.befw.lib.core.data.orm.common.repo.BaseJpaRepository;

@Repository
public interface GnMsgSrvConnRepo extends BaseJpaRepository<GnMsgSrvConnModel, String> {

	List<GnMsgSrvConnModel> findByTenantAndEnvAndUseStatCd(String tenant, String env, UseStatCd useStatCd);
}
