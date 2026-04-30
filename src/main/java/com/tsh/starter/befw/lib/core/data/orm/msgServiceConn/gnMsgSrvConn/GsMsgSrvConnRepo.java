package com.tsh.starter.befw.lib.core.data.orm.msgServiceConn.gnMsgSrvConn;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.tsh.starter.befw.lib.core.data.constant.UseStatCd;
import com.tsh.starter.befw.lib.core.data.orm.common.repo.BaseJpaRepository;

@Repository
public interface GsMsgSrvConnRepo extends BaseJpaRepository<GsMsgSrvConnModel, String> {

	List<GsMsgSrvConnModel> findByTenantAndEnvAndUseStatCd(String tenant, String env, UseStatCd useStatCd);
}
