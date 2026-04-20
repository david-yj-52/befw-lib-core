package com.tsh.starter.befw.lib.core.data.orm.common.model;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import org.hibernate.envers.Audited;

import com.tsh.starter.befw.lib.core.config.ApplicationProperties;
import com.tsh.starter.befw.lib.core.data.constant.UseStatCd;
import com.tsh.starter.befw.lib.core.spec.ApMessageBody;
import com.tsh.starter.befw.lib.core.spec.constant.ApMessageList;
import com.tsh.starter.befw.lib.core.spec.process.ApCommonProcessVo;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PreUpdate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor
@Audited
@SuperBuilder
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
	public static final String EVNT_NM = "EVNT_NM";
	public static final String PREV_EVNT_NMM = "PREV_EVNT_NM";
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

	@NotNull(message = "EventName is essential")
	@Column(name = EVNT_NM, length = 100, nullable = false)
	@Enumerated(EnumType.STRING)
	private ApMessageList evtNm;

	@NotNull(message = "Previous eventName is essential")
	@Column(name = PREV_EVNT_NMM, length = 100, nullable = false)
	@Enumerated(EnumType.STRING)
	private ApMessageList prevEvntNm;

	@Column(name = ACT_CM)
	private String actCm;

	@Column(name = ACT_CD)
	private String actCd;

	public <T extends ApMessageBody> void initFromProcessVo(ApCommonProcessVo<T> procVo) {

		srvId = ApplicationProperties.getApplicationServiceName();
		tenant = procVo.getTenant();
		traceId = procVo.getTraceId();
		useStatCd = UseStatCd.Usable;
		evtNm = procVo.getEventNm();
		prevEvntNm = ApMessageList.InitializeData;

	}

	public <T extends ApMessageBody> void updateFromProcessVo(ApCommonProcessVo<T> procVo, BaseModel existing) {

		prevEvntNm = evtNm;
		evtNm = procVo.getEventNm();
		traceId = procVo.getTraceId();

	}

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
