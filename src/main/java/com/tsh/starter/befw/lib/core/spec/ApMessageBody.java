package com.tsh.starter.befw.lib.core.spec;

import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@Valid
public class ApMessageBody {

	@NotNull(message = "필수값 누락")
	@JsonInclude(JsonInclude.Include.ALWAYS)
	private String userId;

	@NotNull(message = "필수값 누락")
	@JsonInclude(JsonInclude.Include.ALWAYS)
	private String tenant;

}
