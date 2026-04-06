package com.tsh.starter.befw.lib.core.data.orm.common.access;

import java.time.LocalDateTime;
import java.util.List;

import com.tsh.starter.befw.lib.core.data.constant.UseStatCd;
import com.tsh.starter.befw.lib.core.data.orm.common.model.BaseModel;
import com.tsh.starter.befw.lib.core.data.orm.common.repo.BaseJpaRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

public abstract class AbstractCrudService<M extends BaseModel, ID> implements CrudService<M, ID> {

	protected abstract BaseJpaRepository<M, ID> getRepository();

	@Override
	public M findById(ID id) {
		return getRepository().findById(id)
			.orElseThrow(() -> new EntityNotFoundException("not found: " + id));
	}

	@Override
	public List<M> findAll(String tenant) {
		return getRepository().findByTenantAndUseStatCd(tenant, UseStatCd.Usable);
	}

	@Override
	public List<M> findAll(String tenant, LocalDateTime since) {
		return getRepository().findRecentlyUpdated(tenant, UseStatCd.Usable, since);
	}

	@Override
	public List<M> findAllSoftDelete(String tenant) {
		return getRepository().findByTenantAndUseStatCd(tenant, UseStatCd.Delete);
	}

	@Override
	public List<M> findAllWrongData(String tenant) {
		return getRepository().findByTenantAndUseStatCd(tenant, UseStatCd.UnUsable);
	}

	@Override
	@Transactional
	public M create(M model) {
		return getRepository().save(model);
	}

	@Override
	@Transactional
	public M update(ID id, M model) {
		return getRepository().save(model);
	}

	@Override
	@Transactional
	public void delete(ID id) {
		M model = findById(id);
		model.setUseStatCd(UseStatCd.Delete);   // Soft Delete
	}
}
