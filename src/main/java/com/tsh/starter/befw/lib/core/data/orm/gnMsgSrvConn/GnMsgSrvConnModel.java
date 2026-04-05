package com.tsh.starter.befw.lib.core.data.orm.gnMsgSrvConn;

import org.hibernate.envers.Audited;

import com.tsh.starter.befw.lib.core.constant.GlobalTableName;
import com.tsh.starter.befw.lib.core.data.constant.MessagingSolutionType;
import com.tsh.starter.befw.lib.core.data.orm.BaseModel;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = GlobalTableName.GN_MSG_SRV_CONN)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Audited    // History 자동 생성을 위함
public class GnMsgSrvConnModel extends BaseModel {

	@Enumerated(EnumType.STRING)
	private MessagingSolutionType solNm;

	private String env;
	private String host;
	private int port;
	private String user;
	private String pwd;
	private String domain;

}
