package com.tsh.starter.befw.lib.core.data.orm.gnMptRoleDef;

import org.hibernate.annotations.Comment;
import org.hibernate.envers.Audited;

import com.tsh.starter.befw.lib.core.constant.GlobalTableName;
import com.tsh.starter.befw.lib.core.data.orm.common.model.BaseModel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(
	name = GlobalTableName.GN_MPT_ROLE_DEF,
	uniqueConstraints = {
		@UniqueConstraint(name = GnMptRoleDeffModel.UK01, columnNames = {"env", "sol_nm", "host", "port"})
	}
)
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@Audited    // History 자동 생성을 위함
public class GnMptRoleDeffModel extends BaseModel {
	public static final String UK01 = "uk_mpt_role_def_01";

	@Comment("company name")
	@Column(name = "COMPANY_NM", nullable = false, length = 100)
	private String companyNm;

	@Comment("division(부서) name")
	@Column(name = "DIVISION_NM", nullable = false, length = 100)
	private String divisionNm;

	@Comment("team(팀) name")
	@Column(name = "TEAM_NM", nullable = false, length = 100)
	private String teamNm;

	@Comment("System or separated job operate unit (단위 업무 수행)")
	@Column(name = "MODULE_NM", nullable = false, length = 100)
	private String moduleNm;

	@Comment("task name ex) system operation role.")
	@Column(name = "ROLE_NM", nullable = false)
	private String roleNm;

	@Comment("level of employee. 1: min ~ 10: max")
	@Min(value = 1, message = "level should be over 1.")
	@Max(value = 10, message = "level should be under 10.")
	@Column(name = "LEVEL", nullable = false)
	private int level;

	@Comment("context about this job considered it's level")
	@Column(name = "KNOWLEDGE_BACKGROUND", nullable = false)
	private String knowledgeBackground;

}
