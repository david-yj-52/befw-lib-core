package com.tsh.starter.befw.lib.core.spec.process;

import java.util.ArrayList;

import com.tsh.starter.befw.lib.core.ApMessage;
import com.tsh.starter.befw.lib.core.interfaces.InterfaceType;
import com.tsh.starter.befw.lib.core.spec.ApMessageBody;
import com.tsh.starter.befw.lib.core.spec.constant.ApMessageList;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@NoArgsConstructor
public abstract class ApCommonProcessVo<T extends ApMessageBody> {

	InterfaceType interfaceType;
	ApMessageList eventNm;
	String tenant;
	String userId;
	String traceId;

	ApMessageVo<T> receiveMsgInfo;
	ArrayList<ApMessageVo<?>> sendMsgInfoList;

	protected void initCommon(InterfaceType interfaceType, ApMessage ivo, T body) {
		log.info("init common process vo.");
		this.interfaceType = interfaceType;
		this.eventNm = ivo.getHead().getEventNm();
		this.traceId = ivo.getHead().getTraceId();
		this.tenant = body.getTenant();
		this.userId = body.getUserId();
		this.receiveMsgInfo = ApMessageVo.<T>builder()
			.head(ivo.getHead())
			.timestamp(System.currentTimeMillis())
			.src(ivo.getHead().getSrc())
			.tgt(ivo.getHead().getTgt())
			.body(body)
			.build();
	}

	public abstract ApCommonProcessVo<T> init(ApMessage ivo);

	protected void validate() {
		log.info("validate processVo value.");
		// TODO null Check util 추가 필요
		// TODO custom Exception 정의 필요
		if (receiveMsgInfo == null) {
			throw new IllegalArgumentException("Msg Info는 필수");
		}
	}

}
