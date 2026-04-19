package com.tsh.starter.befw.lib.core.data.orm.common.uk;

import com.tsh.starter.befw.lib.core.data.constant.UseStatCd;
import com.tsh.starter.befw.lib.core.data.orm.common.model.BaseModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UkCrudSupportTest {

	@Mock
	private EntityManager entityManager;

	private UkCrudSupport support;

	@BeforeEach
	void setUp() {
		support = new UkCrudSupport(entityManager);
	}

	// ── 테스트용 엔티티 ──────────────────────────────────────────────────────

	@Entity
	@Table(
		name = "TEST_ENTITY",
		uniqueConstraints = {
			@UniqueConstraint(name = "uk_test", columnNames = {"COL_A", "COL_B"})
		}
	)
	@Getter
	@Setter
	static class TestEntity extends BaseModel {
		@Column(name = "COL_A")
		private String colA;

		@Column(name = "COL_B")
		private String colB;

		private String other;
	}

	@Entity
	@Table(name = "NO_UK_ENTITY")
	@Getter
	@Setter
	static class NoUkEntity extends BaseModel {
		private String value;
	}

	// ── resolveUkColumns 테스트 ──────────────────────────────────────────────

	@Test
	void resolveUkColumns_returns_column_to_field_mapping() {
		Map<String, String> columns = support.resolveUkColumns(TestEntity.class, "uk_test");

		assertThat(columns).containsKey("COL_A");
		assertThat(columns).containsKey("COL_B");
		assertThat(columns.get("COL_A")).isEqualTo("colA");
		assertThat(columns.get("COL_B")).isEqualTo("colB");
	}

	@Test
	void resolveUkColumns_throws_when_entity_has_no_uniqueConstraint() {
		assertThatThrownBy(() -> support.resolveUkColumns(NoUkEntity.class, "uk_test"))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("no @UniqueConstraint");
	}

	@Test
	void resolveUkColumns_throws_when_ukName_not_found() {
		assertThatThrownBy(() -> support.resolveUkColumns(TestEntity.class, "uk_nonexistent"))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("uk_nonexistent");
	}

	// ── findByUk 테스트 ──────────────────────────────────────────────────────

	@SuppressWarnings("unchecked")
	@Test
	void findByUk_throws_when_result_is_empty() {
		CriteriaBuilder cb = mock(CriteriaBuilder.class);
		CriteriaQuery<TestEntity> cq = mock(CriteriaQuery.class);
		Root<TestEntity> root = mock(Root.class);
		Path<Object> pathA = mock(Path.class);
		Path<Object> pathB = mock(Path.class);
		Predicate predA = mock(Predicate.class);
		Predicate predB = mock(Predicate.class);

		when(entityManager.getCriteriaBuilder()).thenReturn(cb);
		when(cb.createQuery(TestEntity.class)).thenReturn(cq);
		when(cq.from(TestEntity.class)).thenReturn(root);
		when(root.get("colA")).thenReturn(pathA);
		when(root.get("colB")).thenReturn(pathB);
		when(cb.equal(pathA, "valA")).thenReturn(predA);
		when(cb.equal(pathB, "valB")).thenReturn(predB);
		when(cq.where(any(Predicate[].class))).thenReturn(cq);

		var typedQuery = mock(jakarta.persistence.TypedQuery.class);
		when(entityManager.createQuery(cq)).thenReturn(typedQuery);
		when(typedQuery.getResultList()).thenReturn(List.of());

		Map<String, Object> params = Map.of("COL_A", "valA", "COL_B", "valB");

		assertThatThrownBy(() -> support.findByUk(TestEntity.class, "uk_test", params))
			.isInstanceOf(EntityNotFoundException.class)
			.hasMessageContaining("uk_test");
	}

	@Test
	void findByUk_throws_when_missing_param() {
		Map<String, Object> params = Map.of("COL_A", "valA"); // COL_B 누락

		assertThatThrownBy(() -> support.findByUk(TestEntity.class, "uk_test", params))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("COL_B");
	}

	// ── copyNonNullFields 테스트 ──────────────────────────────────────────────

	@Test
	void copyNonNullFields_copies_non_null_and_skips_uk_fields() {
		TestEntity source = new TestEntity();
		source.setColA("newA");   // UK 필드 - 복사 제외
		source.setColB("newB");   // UK 필드 - 복사 제외
		source.setOther("updatedOther");

		TestEntity target = new TestEntity();
		target.setColA("origA");
		target.setColB("origB");
		target.setOther("origOther");

		Map<String, String> ukColumns = support.resolveUkColumns(TestEntity.class, "uk_test");
		support.copyNonNullFields(source, target, ukColumns);

		// UK 필드는 변경되지 않아야 함
		assertThat(target.getColA()).isEqualTo("origA");
		assertThat(target.getColB()).isEqualTo("origB");
		// 일반 필드는 복사되어야 함
		assertThat(target.getOther()).isEqualTo("updatedOther");
	}

	@Test
	void copyNonNullFields_skips_null_source_fields() {
		TestEntity source = new TestEntity();
		source.setOther(null);  // null 필드는 복사하지 않음

		TestEntity target = new TestEntity();
		target.setOther("origOther");

		Map<String, String> ukColumns = support.resolveUkColumns(TestEntity.class, "uk_test");
		support.copyNonNullFields(source, target, ukColumns);

		assertThat(target.getOther()).isEqualTo("origOther");
	}

	// ── Soft Delete 테스트 (동작 확인용) ──────────────────────────────────────

	@Test
	void soft_delete_uses_UseStatCd_Delete() {
		TestEntity entity = new TestEntity();
		entity.setUseStatCd(UseStatCd.Usable);

		entity.setUseStatCd(UseStatCd.Delete);  // Soft Delete 적용

		assertThat(entity.getUseStatCd()).isEqualTo(UseStatCd.Delete);
	}

}
