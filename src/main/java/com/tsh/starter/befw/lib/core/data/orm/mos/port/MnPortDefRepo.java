package com.tsh.starter.befw.lib.core.data.orm.mos.port;

import org.springframework.stereotype.Repository;

import com.tsh.starter.befw.lib.core.data.orm.common.repo.BaseJpaRepository;

@Repository
public interface MnPortDefRepo extends BaseJpaRepository<MnPortDef, String> {
}
