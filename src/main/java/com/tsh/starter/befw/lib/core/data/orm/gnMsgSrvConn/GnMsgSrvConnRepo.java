package com.tsh.starter.befw.lib.core.data.orm.gnMsgSrvConn;

import org.springframework.stereotype.Repository;

import com.tsh.starter.befw.lib.core.data.orm.common.repo.BaseJpaRepository;

@Repository
public interface GnMsgSrvConnRepo extends BaseJpaRepository<GnMsgSrvConnModel, String> {
}
