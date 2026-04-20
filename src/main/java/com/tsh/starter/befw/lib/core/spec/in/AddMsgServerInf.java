package com.tsh.starter.befw.lib.core.spec.in;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.tsh.starter.befw.lib.core.ApMessage;
import com.tsh.starter.befw.lib.core.data.constant.MessagingSolutionType;
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
public class AddMsgServerInf extends ApMessage {

	public static final ApMessageList eventNm = ApMessageList.AddMsgServerInfoIvo;

	@Valid
	@NotNull(message = "body는 필수값입니다.")
	private AddMsgServerInf.Body body;

	@Data
	@SuperBuilder
	@NoArgsConstructor
	@ToString(callSuper = true)
	@EqualsAndHashCode(callSuper = true)
	public static class Body extends ApMessageBody {

		@NotNull(message = "필수값 누락")
		@JsonInclude(JsonInclude.Include.ALWAYS)
		private MessagingSolutionType solNm;

		@NotNull(message = "필수값 누락")
		private String conUserId;

		@NotNull(message = "필수값 누락")
		private String pwd;
		
		@NotNull(message = "필수값 누락")
		private String domain;

		@NotNull(message = "필수값 누락")
		private String env;

		@NotNull(message = "필수값 누락")
		private String host;

		@NotNull(message = "필수값 누락")
		private int port;
	}

}
