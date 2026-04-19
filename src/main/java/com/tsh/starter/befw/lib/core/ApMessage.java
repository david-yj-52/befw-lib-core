package com.tsh.starter.befw.lib.core;

import com.tsh.starter.befw.lib.core.spec.ApMessageHead;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
public class ApMessage {

	@Schema(description = "메시지 헤디")
	private ApMessageHead head;

}
