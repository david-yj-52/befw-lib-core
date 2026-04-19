package com.tsh.starter.befw.lib.core.data.orm.common.uk;

import jakarta.persistence.Column;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class UkCrudSupport {

	private final EntityManager entityManager;

	public UkCrudSupport(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	/**
	 * 엔티티 클래스에서 ukName에 해당하는 UniqueConstraint의 컬럼 목록을 반환한다.
	 * 컬럼명(DB)을 키, 필드명(Java)을 값으로 하는 Map으로 반환.
	 */
	public Map<String, String> resolveUkColumns(Class<?> entityClass, String ukName) {
		Table tableAnnotation = entityClass.getAnnotation(Table.class);
		if (tableAnnotation == null) {
			throw new IllegalArgumentException(
				"Entity " + entityClass.getSimpleName() + " has no @Table annotation");
		}

		UniqueConstraint[] constraints = tableAnnotation.uniqueConstraints();
		if (constraints == null || constraints.length == 0) {
			throw new IllegalArgumentException(
				"Entity " + entityClass.getSimpleName() + " has no @UniqueConstraint defined");
		}

		UniqueConstraint target = Arrays.stream(constraints)
			.filter(uc -> ukName.equals(uc.name()))
			.findFirst()
			.orElseThrow(() -> new IllegalArgumentException(
				"UniqueConstraint with name '" + ukName + "' not found in " + entityClass.getSimpleName()));

		// 컬럼명 -> 필드명 매핑
		Map<String, String> columnToField = buildColumnToFieldMap(entityClass);

		Map<String, String> result = new HashMap<>();
		for (String columnName : target.columnNames()) {
			String fieldName = columnToField.get(columnName.toUpperCase());
			if (fieldName == null) {
				// 컬럼명과 필드명이 같은 경우(어노테이션 없이 기본 매핑)도 고려
				fieldName = columnName;
			}
			result.put(columnName.toUpperCase(), fieldName);
		}
		return result;
	}

	/**
	 * 엔티티 클래스의 모든 필드를 순회하며 @Column(name=...) -> 필드명 매핑을 구성한다.
	 */
	private Map<String, String> buildColumnToFieldMap(Class<?> entityClass) {
		Map<String, String> map = new HashMap<>();
		Class<?> current = entityClass;
		while (current != null && current != Object.class) {
			for (Field field : current.getDeclaredFields()) {
				Column col = field.getAnnotation(Column.class);
				if (col != null && !col.name().isEmpty()) {
					map.put(col.name().toUpperCase(), field.getName());
				} else {
					// @Column 없는 경우 필드명 그대로 매핑 (대소문자 무시)
					map.put(field.getName().toUpperCase(), field.getName());
				}
			}
			current = current.getSuperclass();
		}
		return map;
	}

	/**
	 * UK 조건으로 단일 엔티티를 조회한다.
	 */
	public <M> M findByUk(Class<M> entityClass, String ukName, Map<String, Object> params) {
		Map<String, String> ukColumns = resolveUkColumns(entityClass, ukName);
		validateParams(ukColumns, params);

		// 컬럼명 기준 params를 필드명 기준으로 변환
		Map<String, Object> fieldParams = toFieldParams(ukColumns, params);

		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<M> query = cb.createQuery(entityClass);
		Root<M> root = query.from(entityClass);

		List<Predicate> predicates = new ArrayList<>();
		for (Map.Entry<String, Object> entry : fieldParams.entrySet()) {
			predicates.add(cb.equal(root.get(entry.getKey()), entry.getValue()));
		}
		query.where(predicates.toArray(new Predicate[0]));

		List<M> results = entityManager.createQuery(query).getResultList();

		if (results.isEmpty()) {
			throw new EntityNotFoundException(
				"Entity not found by UK '" + ukName + "' with params: " + params);
		}
		return results.get(0);
	}

	/**
	 * UK params를 필드명 기준으로 변환한다.
	 */
	private Map<String, Object> toFieldParams(Map<String, String> ukColumns, Map<String, Object> params) {
		Map<String, Object> fieldParams = new HashMap<>();
		for (Map.Entry<String, String> entry : ukColumns.entrySet()) {
			String columnName = entry.getKey();
			String fieldName = entry.getValue();
			// params는 컬럼명 또는 필드명으로 들어올 수 있음
			Object value = params.get(columnName);
			if (value == null) {
				value = params.get(fieldName);
			}
			if (value == null) {
				// 대소문자 무시 검색
				for (Map.Entry<String, Object> pe : params.entrySet()) {
					if (pe.getKey().equalsIgnoreCase(columnName) || pe.getKey().equalsIgnoreCase(fieldName)) {
						value = pe.getValue();
						break;
					}
				}
			}
			fieldParams.put(fieldName, value);
		}
		return fieldParams;
	}

	/**
	 * params의 키가 UK 컬럼 목록과 일치하는지 검증한다.
	 */
	private void validateParams(Map<String, String> ukColumns, Map<String, Object> params) {
		for (Map.Entry<String, String> entry : ukColumns.entrySet()) {
			String columnName = entry.getKey();
			String fieldName = entry.getValue();
			boolean found = params.containsKey(columnName)
				|| params.containsKey(fieldName)
				|| params.keySet().stream().anyMatch(k -> k.equalsIgnoreCase(columnName) || k.equalsIgnoreCase(fieldName));
			if (!found) {
				throw new IllegalArgumentException(
					"Missing UK param for column '" + columnName + "' (field: " + fieldName + ")");
			}
		}
	}

	/**
	 * model의 null이 아닌 필드를 target 엔티티에 복사한다. UK 필드는 제외한다.
	 */
	public <M> void copyNonNullFields(M source, M target, Map<String, String> ukColumns) {
		Class<?> current = source.getClass();
		while (current != null && current != Object.class) {
			for (Field field : current.getDeclaredFields()) {
				// UK 필드는 제외
				if (isUkField(field, ukColumns)) {
					continue;
				}
				field.setAccessible(true);
				try {
					Object value = field.get(source);
					if (value != null) {
						field.set(target, value);
					}
				} catch (IllegalAccessException e) {
					// 접근 불가 필드는 무시
				}
			}
			current = current.getSuperclass();
		}
	}

	private boolean isUkField(Field field, Map<String, String> ukColumns) {
		String fieldName = field.getName();
		Column col = field.getAnnotation(Column.class);
		String columnName = (col != null && !col.name().isEmpty()) ? col.name().toUpperCase() : fieldName.toUpperCase();
		return ukColumns.containsKey(columnName) || ukColumns.containsValue(fieldName);
	}

}
