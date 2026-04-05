package com.tsh.starter.befw.lib.core.data.orm.gnMsgSrvConn;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class GnMsgSrvConnAccess {

	@Autowired
	GnMsgSrvConnRepo repo;
}
