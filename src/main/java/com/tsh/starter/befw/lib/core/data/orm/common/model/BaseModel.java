package com.tsh.starter.befw.lib.core.data.orm.common.model;

import com.tsh.starter.befw.lib.core.data.constant.UseStatCd;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PreUpdate;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

@Slf4j
@MappedSuperclass
@Getter
@Setter
@FilterDef(
	name = "tenantFilter",
	parameters = @ParamDef(name = "tenant", type = String.class)
)
@Filter(name = "tenantFilter", condition = "TENANT = :tenant")
public class BaseModel extends BasicAudit {

	public static final String SRV_ID = "SRV_ID";
	public static final String TENANT = "TENANT";
	public static final String TRACE_ID = "TRACE_ID";
	public static final String USE_STAT_CD = "USE_STAT_CD";
	public static final String EVENT_NM = "EVENT_NM";
	public static final String PREV_EVENT_NM = "PREV_EVENT_NM";
	public static final String ACT_CM = "ACT_CM";
	public static final String ACT_CD = "ACT_CD";

	@NotBlank(message = "ServiceId is essential")
	@Column(name = SRV_ID, length = 50, nullable = false)
	private String srvId;

	@NotBlank(message = "Tenant is essential")
	@Column(name = TENANT, length = 50, nullable = false)
	private String tenant;

	@NotBlank(message = "TraceId is essential")
	@Column(name = TRACE_ID, length = 100, nullable = false)
	private String traceId;

	@Column(name = USE_STAT_CD, length = 40, nullable = false)
	@Enumerated(EnumType.STRING)
	private UseStatCd useStatCd;

	@NotBlank(message = "EventName is essential")
	@Column(name = EVENT_NM, length = 100, nullable = false)
	private String evetNm;

	@NotBlank(message = "Previous eventName is essential")
	@Column(name = PREV_EVENT_NM, length = 100, nullable = false)
	private String prevEventNm;

	@Column(name = ACT_CM)
	private String actCm;

	@Column(name = ACT_CD)
	private String actCd;

	protected void onDataInsert() {

		log.info("called when insert data at the very first.");
		if (this.useStatCd == null) {
			log.info("warn useStatCd is null. set defulat Usable");
			this.useStatCd = UseStatCd.Usable;
		}

	}

	@PreUpdate
	protected void onPreUpdate() {

		log.info("called when record has been updated.");
	}

}
