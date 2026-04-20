package com.tsh.starter.befw.lib.core.data.orm.common.access;

import java.lang.reflect.ParameterizedType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;

import com.tsh.starter.befw.lib.core.data.constant.UseStatCd;
import com.tsh.starter.befw.lib.core.data.orm.common.error.DataErrorResponse;
import com.tsh.starter.befw.lib.core.data.orm.common.error.JpaExceptionHandler;
import com.tsh.starter.befw.lib.core.data.orm.common.model.BaseModel;
import com.tsh.starter.befw.lib.core.data.orm.common.repo.BaseJpaRepository;
import com.tsh.starter.befw.lib.core.data.orm.common.tenant.TenantContext;
import com.tsh.starter.befw.lib.core.data.orm.common.uk.UkCrudService;
import com.tsh.starter.befw.lib.core.data.orm.common.uk.UkCrudSupport;
import com.tsh.starter.befw.lib.core.spec.ApMessageBody;
import com.tsh.starter.befw.lib.core.spec.process.ApCommonProcessVo;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractCrudService<M extends BaseModel, ID>
	implements CrudService<M, ID>, UkCrudService<M> {

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	private JpaExceptionHandler jpaExceptionHandler;

	@Autowired
	private UkCrudSupport ukCrudSupport;

	protected abstract BaseJpaRepository<M, ID> getRepository();

	@SuppressWarnings("unchecked")
	protected Class<M> getEntityClass() {
		ParameterizedType type = (ParameterizedType)getClass().getGenericSuperclass();
		return (Class<M>)type.getActualTypeArguments()[0];
	}

	protected String getEntityName() {
		return getEntityClass().getSimpleName();
	}

	protected void enableTenantFilter() {
		String tenant = TenantContext.get();
		Session session = entityManager.unwrap(Session.class);
		session.enableFilter("tenantFilter").setParameter("tenant", tenant);
	}

	@Override
	public M findById(ID id) {
		try {
			enableTenantFilter();
			return getRepository().findById(id)
				.orElseThrow(() -> new EntityNotFoundException("not found: " + id));
		} catch (Exception e) {
			DataErrorResponse response = jpaExceptionHandler.handle(e, getEntityName(), id, "findById");
			throw new RuntimeException(response.getErrorCode() + ": " + response.getMessage(), e);
		}
	}

	@Override
	public List<M> findAll(String tenant) {
		try {
			enableTenantFilter();
			return getRepository().findByTenantAndUseStatCd(tenant, UseStatCd.Usable);
		} catch (Exception e) {
			DataErrorResponse response = jpaExceptionHandler.handle(e, getEntityName(), tenant, "findAll");
			throw new RuntimeException(response.getErrorCode() + ": " + response.getMessage(), e);
		}
	}

	@Override
	public List<M> findAll(String tenant, LocalDateTime since) {
		try {
			enableTenantFilter();
			return getRepository().findRecentlyUpdated(tenant, UseStatCd.Usable, since);
		} catch (Exception e) {
			DataErrorResponse response = jpaExceptionHandler.handle(e, getEntityName(), tenant,
				"findAll(since=" + since + ")");
			throw new RuntimeException(response.getErrorCode() + ": " + response.getMessage(), e);
		}
	}

	@Override
	public List<M> findAllSoftDelete(String tenant) {
		try {
			enableTenantFilter();
			return getRepository().findByTenantAndUseStatCd(tenant, UseStatCd.Delete);
		} catch (Exception e) {
			DataErrorResponse response = jpaExceptionHandler.handle(e, getEntityName(), tenant, "findAllSoftDelete");
			throw new RuntimeException(response.getErrorCode() + ": " + response.getMessage(), e);
		}
	}

	@Override
	public List<M> findAllWrongData(String tenant) {
		try {
			enableTenantFilter();
			return getRepository().findByTenantAndUseStatCd(tenant, UseStatCd.UnUsable);
		} catch (Exception e) {
			DataErrorResponse response = jpaExceptionHandler.handle(e, getEntityName(), tenant, "findAllWrongData");
			throw new RuntimeException(response.getErrorCode() + ": " + response.getMessage(), e);
		}
	}

	@Override
	@Transactional
	public M create(M model) {
		try {
			return getRepository().saveAndFlush(model);
		} catch (Exception e) {
			DataErrorResponse response = jpaExceptionHandler.handle(e, getEntityName(), model, "create");
			throw new RuntimeException(response.getErrorCode() + ": " + response.getMessage(), e);
		}
	}

	// procVo 오버로드 - 메서드 레벨 제네릭으로 선언
	@Transactional
	public <T extends ApMessageBody> M create(M model, ApCommonProcessVo<T> procVo) {
		model.initFromProcessVo(procVo);
		return create(model);  // 기존 create 재사용
	}

	@Override
	@Transactional
	public M update(ID id, M model) {
		try {
			return getRepository().save(model);
		} catch (Exception e) {
			DataErrorResponse response = jpaExceptionHandler.handle(e, getEntityName(), id, "update");
			throw new RuntimeException(response.getErrorCode() + ": " + response.getMessage(), e);
		}
	}

	@Transactional
	public <T extends ApMessageBody> M update(ID id, M model, ApCommonProcessVo<T> procVo) {
		M existing = findById(id);
		model.updateFromProcessVo(procVo, existing);
		return update(id, model);
	}

	@Override
	@Transactional
	public void delete(ID id) {
		try {
			M model = findById(id);
			model.setUseStatCd(UseStatCd.Delete);   // Soft Delete
			getRepository().save(model);
		} catch (Exception e) {
			DataErrorResponse response = jpaExceptionHandler.handle(e, getEntityName(), id, "delete");
			throw new RuntimeException(response.getErrorCode() + ": " + response.getMessage(), e);
		}
	}

	// ── UK CRUD ──────────────────────────────────────────────────────────────

	@Override
	public M findByUk(String ukName, Map<String, Object> params) {
		try {
			enableTenantFilter();
			return ukCrudSupport.findByUk(getEntityClass(), ukName, params);
		} catch (Exception e) {
			DataErrorResponse response = jpaExceptionHandler.handle(e, getEntityName(), params,
				"findByUk(" + ukName + ")");
			throw new RuntimeException(response.getErrorCode() + ": " + response.getMessage(), e);
		}
	}

	@Override
	@Transactional
	public M updateByUk(String ukName, Map<String, Object> params, M model) {
		try {
			enableTenantFilter();
			M existing = ukCrudSupport.findByUk(getEntityClass(), ukName, params);
			Map<String, String> ukColumns = ukCrudSupport.resolveUkColumns(getEntityClass(), ukName);
			ukCrudSupport.copyNonNullFields(model, existing, ukColumns);
			return getRepository().save(existing);
		} catch (Exception e) {
			DataErrorResponse response = jpaExceptionHandler.handle(e, getEntityName(), params,
				"updateByUk(" + ukName + ")");
			throw new RuntimeException(response.getErrorCode() + ": " + response.getMessage(), e);
		}
	}

	@Transactional
	public <T extends ApMessageBody> M updateByUk(String ukName, Map<String, Object> params, M model,
		ApCommonProcessVo<T> procVo) {
		try {
			enableTenantFilter();
			M existing = ukCrudSupport.findByUk(getEntityClass(), ukName, params);
			Map<String, String> ukColumns = ukCrudSupport.resolveUkColumns(getEntityClass(), ukName);
			model.updateFromProcessVo(procVo, existing);
			ukCrudSupport.copyNonNullFields(model, existing, ukColumns);
			return getRepository().save(existing);
		} catch (Exception e) {
			DataErrorResponse response = jpaExceptionHandler.handle(e, getEntityName(), params,
				"updateByUk(" + ukName + ")");
			throw new RuntimeException(response.getErrorCode() + ": " + response.getMessage(), e);
		}
	}

	@Override
	@Transactional
	public void deleteByUk(String ukName, Map<String, Object> params) {
		try {
			enableTenantFilter();
			M existing = ukCrudSupport.findByUk(getEntityClass(), ukName, params);
			existing.setUseStatCd(UseStatCd.Delete);   // Soft Delete
			getRepository().save(existing);
		} catch (Exception e) {
			DataErrorResponse response = jpaExceptionHandler.handle(e, getEntityName(), params,
				"deleteByUk(" + ukName + ")");
			throw new RuntimeException(response.getErrorCode() + ": " + response.getMessage(), e);
		}
	}
}
