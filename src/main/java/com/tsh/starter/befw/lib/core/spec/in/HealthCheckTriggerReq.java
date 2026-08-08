package com.tsh.starter.befw.lib.core.spec.in;

import com.tsh.starter.befw.lib.core.ApMessage;
import com.tsh.starter.befw.lib.core.spec.ApMessageBody;
import com.tsh.starter.befw.lib.core.spec.constant.ApMessageList;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class HealthCheckTriggerReq extends ApMessage {

	public static final ApMessageList eventNm = ApMessageList.HealthCheckTriggerReq;

	@Valid
	@NotNull(message = "body는 필수값 입니다.")
	private Body body;

	@Data
	@SuperBuilder
	@NoArgsConstructor
	@ToString(callSuper = true)
	@EqualsAndHashCode(callSuper = true)
	public static class Body extends ApMessageBody {

	}

}
