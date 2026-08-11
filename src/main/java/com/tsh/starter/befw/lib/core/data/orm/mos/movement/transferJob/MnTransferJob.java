package com.tsh.starter.befw.lib.core.data.orm.mos.movement.transferJob;

import com.tsh.starter.befw.lib.core.data.constant.UseYn;
import com.tsh.starter.befw.lib.core.data.orm.common.model.BaseModel;
import com.tsh.starter.befw.lib.core.data.orm.mos.movement.common.LocationVo;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.envers.Audited;

@Entity
@Table(
	name = "MN_TRANSFER_JOB",
	uniqueConstraints = {
		@UniqueConstraint(name = MnTransferJob.UK01, columnNames = {"env", "sol_nm", "host", "port"})
	}
)
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@Audited    // History 자동 생성을 위함
public class MnTransferJob extends BaseModel {
	public static final String UK01 = "uk_transfer_job_01";

	@Column(name = "JOB_ID")
	private String jobId;

	@Column(name = "CARR_ID")
	private String carrId;

	@Column(name = "LOT_ID")
	private String lotId;

	@Column(name = "PRIORITY")
	private Long priority;

	@Column(name = "FROM_EQP_ID")
	private String fromEqpId;

	@Column(name = "FROM_PORT_ID")
	private String fromPortId;

	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "eqpId", column = @Column(name = "FROM_EQP_ID")),
			@AttributeOverride(name = "portId", column = @Column(name = "FROM_PORT_ID"))
	})
	private LocationVo fromLocInf;


	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "eqpId", column = @Column(name = "TO_EQP_ID")),
			@AttributeOverride(name = "portId", column = @Column(name = "TO_PORT_ID"))
	})
	private LocationVo toLocInf;

	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "eqpId", column = @Column(name = "CRNT_EQP_ID")),
			@AttributeOverride(name = "portId", column = @Column(name = "CRNT_PORT_ID"))
	})
	private LocationVo crntLocInf;



}
