package com.tsh.starter.befw.lib.core.data.orm.messageReply.gsSolMsgRep;

import org.springframework.stereotype.Repository;

import com.tsh.starter.befw.lib.core.data.constant.UseStatCd;
import com.tsh.starter.befw.lib.core.data.orm.common.repo.BaseJpaRepository;

@Repository
public interface GsSolMsgRepRepo extends BaseJpaRepository<GsSolMsgRepModel, String> {

	GsSolMsgRepModel findByTenantAndTraceIdAndUseStatCd(String tenant, String traceId, UseStatCd useStatCd);

}
