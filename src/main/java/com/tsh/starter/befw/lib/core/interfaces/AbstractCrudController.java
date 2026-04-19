package com.tsh.starter.befw.lib.core.interfaces;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.tsh.starter.befw.lib.core.data.orm.common.access.CrudService;
import com.tsh.starter.befw.lib.core.data.orm.common.model.BaseModel;

import lombok.extern.slf4j.Slf4j;

/**
 * 공통 CRUD Controller 추상 클래스
 * Service 위임, 응답 래핑, HTTP 상태코드 매핑을 담당
 *
 * @param <REQ> 요청 DTO
 * @param <RES> 응답 DTO
 * @param <M>   JPA Entity (BaseModel 상속)
 * @param <ID>  PK 타입
 */
@Slf4j
public abstract class AbstractCrudController<REQ, RES, M extends BaseModel, ID>
	implements CrudController<REQ, RES, ID> {

	// ── 하위 클래스에서 반드시 구현 ─────────────────────────────────────────

	/** 사용할 Service 반환 */
	protected abstract CrudService<M, ID> getService();

	/** 요청 DTO → Entity 변환 */
	protected abstract M toModel(REQ request);

	/** Entity → 응답 DTO 변환 */
	protected abstract RES toResponse(M model);

	// ── 공통 CRUD 구현 ──────────────────────────────────────────────────────

	@Override
	@GetMapping
	public ResponseEntity<ApiResponse<List<RES>>> findAll(
		@RequestHeader("X-Tenant") String tenant) {

		List<RES> result = getService().findAll(tenant)
			.stream()
			.map(this::toResponse)
			.collect(Collectors.toList());

		return ResponseEntity.ok(ApiResponse.ok(result));
	}

	@Override
	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<RES>> findById(
		@PathVariable ID id) {

		RES result = toResponse(getService().findById(id));
		return ResponseEntity.ok(ApiResponse.ok(result));
	}

	@Override
	@PostMapping
	public ResponseEntity<ApiResponse<RES>> create(
		@RequestBody REQ request) {

		try {
			M saved = getService().create(toModel(request));
			return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.created(toResponse(saved)));

		} catch (Exception e) {
			log.error("e :{}", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
				ApiResponse.error(e.getClass().getName(), e.getMessage())
			);
		}
	}

	@Override
	@PutMapping("/{id}")
	public ResponseEntity<ApiResponse<RES>> update(
		@PathVariable ID id,
		@RequestBody REQ request) {

		M updated = getService().update(id, toModel(request));
		return ResponseEntity.ok(ApiResponse.ok(toResponse(updated)));
	}

	@Override
	@DeleteMapping("/{id}")
	public ResponseEntity<ApiResponse<Void>> delete(
		@PathVariable ID id) {

		getService().delete(id);
		return ResponseEntity.ok(ApiResponse.noContent());
	}
}
