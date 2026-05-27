package com.tsh.starter.befw.lib.core.data.orm.usersAndPermissions.gsUser;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tsh.starter.befw.lib.core.data.orm.common.repo.BaseJpaRepository;

@Repository
public interface GsUserRepo extends BaseJpaRepository<GsUserModel, String> {

	Optional<GsUserModel> findByEmail(String email);

	@Query("SELECT u FROM GsUserModel u WHERE LOWER(u.userNm) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))")
	List<GsUserModel> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

}
