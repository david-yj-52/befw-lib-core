package com.tsh.starter.befw.lib.core.data.orm.common.uk;

import java.util.Map;

public interface UkCrudService<M> {

	M findByUk(String ukName, Map<String, Object> params);

	M updateByUk(String ukName, Map<String, Object> params, M model);

	void deleteByUk(String ukName, Map<String, Object> params);

}
