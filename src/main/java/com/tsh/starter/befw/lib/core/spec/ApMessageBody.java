package com.tsh.starter.befw.lib.core.spec;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
public class ApMessageBody {

	private String userId;
	private String tenant;

}
