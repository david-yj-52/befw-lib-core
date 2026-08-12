package com.tsh.starter.befw.lib.core.data.orm.mos.lot;

import org.hibernate.envers.Audited;

import com.tsh.starter.befw.lib.core.constant.GlobalTableName;
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
	name = GlobalTableName.MN_LOT_STAT,
	uniqueConstraints = {
		@UniqueConstraint(name = MnLotStat.UK01, columnNames = {"env", "sol_nm", "host", "port"})
	}
)
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@Audited    // History 자동 생성을 위함
public class MnLotStat extends BaseModel {
	public static final String UK01 = "uk_transfer_job_01";

	@Column(name = "LOT_ID")
	private String lotId;

	@Column(name = "PROD_ID")
	private String prodId;

	@Column(name = "FLOW_ID")
	private String flowId;

	@Column(name = "STEP_ID")
	private String stepId;

}
