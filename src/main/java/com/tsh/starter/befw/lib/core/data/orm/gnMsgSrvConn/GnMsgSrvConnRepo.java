package com.tsh.starter.befw.lib.core.data.orm.gnMsgSrvConn;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GnMsgSrvConnRepo extends JpaRepository<GnMsgSrvConnModel, String> {
}
