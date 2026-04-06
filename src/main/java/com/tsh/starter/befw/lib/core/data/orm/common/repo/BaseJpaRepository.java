package com.tsh.starter.befw.lib.core.data.orm.common.repo;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

import com.tsh.starter.befw.lib.core.data.constant.UseStatCd;

@NoRepositoryBean
public interface BaseJpaRepository<M, ID> extends JpaRepository<M, ID> {

	List<M> findByTenantAndUseStatCd(String tenant, UseStatCd useStatCd);

	// 특정 시간 이후 + 최신순 정렬
	@Query("SELECT m FROM #{#entityName} m WHERE m.tenant = :tenant AND m.useStatCd = :useStatCd AND m.modifiedAt > :since ORDER BY m.modifiedAt DESC")
	List<M> findRecentlyUpdated(
		@Param("tenant") String tenant,
		@Param("useStatCd") UseStatCd useStatCd,
		@Param("since") LocalDateTime since
	);

}
