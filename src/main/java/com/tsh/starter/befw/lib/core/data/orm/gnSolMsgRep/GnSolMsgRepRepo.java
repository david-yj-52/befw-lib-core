package com.tsh.starter.befw.lib.core.data.orm.gnSolMsgRep;

import org.springframework.stereotype.Repository;

import com.tsh.starter.befw.lib.core.data.constant.UseStatCd;
import com.tsh.starter.befw.lib.core.data.orm.common.repo.BaseJpaRepository;

@Repository
public interface GnSolMsgRepRepo extends BaseJpaRepository<GnSolMsgRepModel, String> {

	GnSolMsgRepModel findByTenantAndTraceIdAndUseStatCd(String tenant, String traceId, UseStatCd useStatCd);

}
