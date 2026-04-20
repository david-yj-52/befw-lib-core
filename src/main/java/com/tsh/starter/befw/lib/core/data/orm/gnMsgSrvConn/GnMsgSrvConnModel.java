package com.tsh.starter.befw.lib.core.data.orm.gnMsgSrvConn;

import org.hibernate.envers.Audited;

import com.tsh.starter.befw.lib.core.constant.GlobalTableName;
import com.tsh.starter.befw.lib.core.data.constant.MessagingSolutionType;
import com.tsh.starter.befw.lib.core.data.orm.common.model.BaseModel;

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
	name = GlobalTableName.GN_MSG_SRV_CONN,
	uniqueConstraints = {
		@UniqueConstraint(name = "uk_msg_srv_conn_01", columnNames = {"env", "sol_nm", "host", "port"})
	}
)
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@Audited    // History 자동 생성을 위함
public class GnMsgSrvConnModel extends BaseModel {

	@Enumerated(EnumType.STRING)
	@Column(name = "SOL_NM")
	private MessagingSolutionType solNm;

	@Column(name = "ENV")
	private String env;

	@Column(name = "HOST")
	private String host;

	@Column(name = "PORT")
	private int port;

	@Column(name = "CONN_USER")
	private String connUser;

	@Column(name = "PWD")
	private String pwd;

	@Column(name = "DOMAIN")
	private String domain;

}
