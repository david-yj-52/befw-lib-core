package com.tsh.starter.befw.lib.core.data.orm.common.access;

import java.time.LocalDateTime;
import java.util.List;

public interface CrudService<T, ID> {

	List<T> findAll(String tenant);

	List<T> findAll(String tenant, LocalDateTime since);

	List<T> findAllSoftDelete(String tenant);

	List<T> findAllWrongData(String tenant);

	T findById(ID id);

	T create(T model);

	T update(ID id, T model);

	void delete(ID id);
}
