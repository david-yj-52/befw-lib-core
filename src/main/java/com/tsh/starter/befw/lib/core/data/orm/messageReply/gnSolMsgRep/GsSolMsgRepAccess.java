package com.tsh.starter.befw.lib.core.data.orm.messageReply.gnSolMsgRep;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tsh.starter.befw.lib.core.data.orm.common.access.AbstractCrudService;
import com.tsh.starter.befw.lib.core.data.orm.common.repo.BaseJpaRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class GsSolMsgRepAccess extends AbstractCrudService<GsSolMsgRepModel, String> {

	@Autowired
	GsSolMsgRepRepo repo;

	@Override
	protected BaseJpaRepository<GsSolMsgRepModel, String> getRepository() {
		return repo;
	}
}
