package com.tsh.starter.befw.lib.core.data.orm.usersAndPermissions.gsUser;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tsh.starter.befw.lib.core.data.orm.common.access.AbstractCrudService;
import com.tsh.starter.befw.lib.core.data.orm.common.repo.BaseJpaRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class GsUserAccess extends AbstractCrudService<GsUserModel, String> {

	@Autowired
	GsUserRepo repo;

	@Override
	protected BaseJpaRepository<GsUserModel, String> getRepository() {
		return repo;
	}

	public Optional<GsUserModel> findByEmail(String email) {
		return repo.findByEmail(email);
	}

	public List<GsUserModel> findAllById(List<String> ids) {
		return repo.findAllById(ids);
	}

}
