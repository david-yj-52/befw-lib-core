package com.tsh.starter.befw.lib.core.data.orm.mos.port;

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
	name = "MN_PORT_DEF",
	uniqueConstraints = {
		@UniqueConstraint(name = MnPortDef.UK01, columnNames = {"env", "sol_nm", "host", "port"})
	}
)
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@Audited    // History 자동 생성을 위함
public class MnPortDef extends BaseModel {
	public static final String UK01 = "uk_transfer_job_01";

	@Column(name = "PORT_ID")
	private String portId;

	@Column(name = "EQP_ID")
	private String eqpId;

	@Column(name = "CARR_ID")
	private String carrId;

	// TODO 추후 ENUM화
	@Column(name = "CARR_TYP")
	private String carrTyp;

	// TODO 추후 enum화
	@Column(name = "PORT_TYP")
	private String portTyp;

}
