package com.tsh.starter.befw.lib.core.messaging.solace.util;

import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.solacesystems.jcsmp.JCSMPProperties;
import com.tsh.starter.befw.lib.core.messaging.MessagingConfManager;
import com.tsh.starter.befw.lib.core.messaging.solace.config.SolacePropertyHandler;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SolaceQueueDiscovery {

	private final RestTemplate restTemplate = new RestTemplate();
	@Value("${application.messaging.credential.solace}")
	String sempCredential;
	@Autowired
	MessagingConfManager messagingConfManager;
	private SolacePropertyHandler propertyHandler;
	private JCSMPProperties properties;

	@PostConstruct
	public void init() {

		this.propertyHandler = this.messagingConfManager.getSolaceDefaultHandler().getPropertyHandler();
		this.properties = this.messagingConfManager.getSolaceDefaultHandler().getPropertyHandler().getProperties();

	}

	/**
	 * Queue 이름 prefix 패턴으로 Queue 목록을 조회합니다.
	 * 예) findQueuesByPattern("befw.agent.*")
	 *   → ["befw.agent.user-1", "befw.agent.user-2", ...]
	 *
	 * @param pattern prefix 패턴 (와일드카드 * 지원)
	 * @return Queue 이름 목록
	 */
	public List<String> findQueuesByPattern(String pattern) {
		List<String> result = Collections.emptyList();
		String cursor = null;

		try {
			// SEMP v2는 페이징 처리 필요 — cursor 기반으로 전체 조회
			do {
				SempQueueResponse response = fetchQueuePage(pattern, cursor);

				if (response == null || response.getData() == null)
					break;

				List<String> pageResult = response.getData().stream()
					.map(SempQueueData::getQueueName)
					.collect(Collectors.toList());

				result = result.isEmpty() ? pageResult
					: concat(result, pageResult);

				// 다음 페이지 cursor 확인
				cursor = extractNextCursor(response);

				log.debug("[SEMP] Fetched {} queues, cursor: {}", pageResult.size(), cursor);

			} while (cursor != null);

			log.info("[SEMP] Found {} queues matching pattern: {}", result.size(), pattern);

		} catch (Exception e) {
			log.error("[SEMP] Failed to fetch queue list — pattern: {}", pattern, e);
			throw new RuntimeException("[SEMP] Queue discovery failed.", e);
		}

		return result;
	}

	// ──────────────────────────────────────────────
	// Internal
	// ──────────────────────────────────────────────

	private SempQueueResponse fetchQueuePage(String pattern, String cursor) {
		String sempUrl = propertyHandler.getSempUrl();

		String url = UriComponentsBuilder

			.fromHttpUrl(sempUrl)
			.path("/SEMP/v2/config/msgVpns/{vpnName}/queues")
			.queryParam("where", "queueName==" + pattern) // ✅ 브로커 레벨 패턴 필터
			.queryParam("select", "queueName")            // queueName만 조회 (경량화)
			.queryParam("count", 10)                      // 공식 권장 페이지 사이즈
			.queryParamIfPresent("cursor", java.util.Optional.ofNullable(cursor))
			.buildAndExpand(properties.getProperties().get(JCSMPProperties.VPN_NAME).toString())
			.toUriString();

		HttpEntity<Void> entity = new HttpEntity<>(buildAuthHeaders());

		ResponseEntity<SempQueueResponse> response = restTemplate.exchange(url, HttpMethod.GET, entity,
			SempQueueResponse.class);

		return response.getBody();
	}

	private HttpHeaders buildAuthHeaders() {
		String credentials = sempCredential;

		String encoded = Base64.getEncoder().encodeToString(credentials.getBytes());
		HttpHeaders headers = new HttpHeaders();
		headers.set(HttpHeaders.AUTHORIZATION, "Basic " + encoded);
		return headers;
	}

	private String extractNextCursor(SempQueueResponse response) {
		if (response.getMeta() == null
			|| response.getMeta().getPaging() == null
			|| response.getMeta().getPaging().getCursorQuery() == null) {
			return null;
		}
		return response.getMeta().getPaging().getCursorQuery();
	}

	private <T> List<T> concat(List<T> a, List<T> b) {
		return java.util.stream.Stream.concat(a.stream(), b.stream())
			.collect(Collectors.toList());
	}

	// ──────────────────────────────────────────────
	// SEMP Response DTO
	// ──────────────────────────────────────────────

	@Data
	@JsonIgnoreProperties(ignoreUnknown = true)
	static class SempQueueResponse {
		private List<SempQueueData> data;
		private SempMeta meta;
	}

	@Data
	@JsonIgnoreProperties(ignoreUnknown = true)
	static class SempQueueData {
		@JsonProperty("queueName")
		private String queueName;
	}

	@Data
	@JsonIgnoreProperties(ignoreUnknown = true)
	static class SempMeta {
		private SempPaging paging;
	}

	@Data
	@JsonIgnoreProperties(ignoreUnknown = true)
	static class SempPaging {
		private String cursorQuery;  // 다음 페이지 커서
		private String cursorUri;    // 다음 페이지 전체 URI
	}
}