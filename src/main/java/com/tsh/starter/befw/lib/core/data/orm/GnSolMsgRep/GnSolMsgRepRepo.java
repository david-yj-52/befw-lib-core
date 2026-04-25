package com.tsh.starter.befw.lib.core.data.orm.GnSolMsgRep;


import com.tsh.starter.befw.lib.core.data.constant.UseStatCd;
import com.tsh.starter.befw.lib.core.data.orm.common.repo.BaseJpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GnSolMsgRepRepo extends BaseJpaRepository<GnSolMsgRepModel, String> {

    GnSolMsgRepModel findByTenantAndTraceIdAndUseStatCd(String tenant, String traceId, UseStatCd useStatCd);


}
