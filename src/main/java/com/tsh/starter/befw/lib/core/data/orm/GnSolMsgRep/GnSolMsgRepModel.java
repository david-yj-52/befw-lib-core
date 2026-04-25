package com.tsh.starter.befw.lib.core.data.orm.GnSolMsgRep;

import org.hibernate.envers.Audited;

import com.tsh.starter.befw.lib.core.constant.GlobalTableName;
import com.tsh.starter.befw.lib.core.data.constant.MsgRepStatCd;
import com.tsh.starter.befw.lib.core.data.orm.common.model.BaseModel;
import com.tsh.starter.befw.lib.core.spec.constant.ApMessageList;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(
	name = GlobalTableName.GN_SOL_MSG_REP,
	uniqueConstraints = {
		@UniqueConstraint(name = GnSolMsgRepModel.UK01, columnNames = {"reqSrvNm", "reqTraceId"})
	}
)
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@Audited
public class GnSolMsgRepModel extends BaseModel {

	public static final String UK01 = "uk_sol_msg_rep_01";

	@Column(name = "REQ_SRV_NM")
	private String reqSrvNm;

	@Column(name = "REQ_TRACE_ID")
	private String reqTraceId;

	@Column(name = "RECV_EVNT_NM")
	private ApMessageList recvEvntNm;

	@Column(name = "RECV_TOPIC_NM")
	private String recvTopicNm;

	@Column(name = "SELECTOR_KEY")
	private String selectorKey;

	@Column(name = "REP_STAT_CD")
	@Enumerated(EnumType.STRING)
	private MsgRepStatCd repStatCd;

}
