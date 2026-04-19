package com.tsh.starter.befw.lib.core.interfaces;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * 공통 CRUD Controller 인터페이스
 *
 * @param <REQ> 요청 DTO
 * @param <RES> 응답 DTO
 * @param <ID>  PK 타입
 */
public interface CrudController<REQ, RES, ID> {

    ResponseEntity<ApiResponse<List<RES>>> findAll(
            @RequestHeader("X-Tenant") String tenant);

    ResponseEntity<ApiResponse<RES>> findById(
            @PathVariable ID id);

    ResponseEntity<ApiResponse<RES>> create(
            @RequestBody REQ request);

    ResponseEntity<ApiResponse<RES>> update(
            @PathVariable ID id,
            @RequestBody REQ request);

    ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable ID id);
}
