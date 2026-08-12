package com.tsh.starter.befw.lib.core.data.orm.mos.eqp;

import org.hibernate.envers.Audited;

import com.tsh.starter.befw.lib.core.data.orm.common.model.BaseModel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(
	name = "MN_EQP_STAT",
	uniqueConstraints = {
		@UniqueConstraint(name = MnEqpStat.UK01, columnNames = {"env", "sol_nm", "host", "port"})
	}
)
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@Audited    // History 자동 생성을 위함
public class MnEqpStat extends BaseModel {
	public static final String UK01 = "uk_transfer_job_01";

	@Column(name = "EQP_ID")
	private String eqpId;

	// TODO 추후 ENUM화
	@Column(name = "EQP_STAT")
	private String eqpStat;

}
