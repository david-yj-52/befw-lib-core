package com.tsh.starter.befw.lib.core.spec;

import com.tsh.starter.befw.lib.core.spec.constant.ApMessageList;
import com.tsh.starter.befw.lib.core.spec.constant.ApSystemList;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
public class ApMessageHead {

	private ApSystemList src;
	private ApSystemList tgt;
	private String traceId;
	private ApMessageList eventNm;

}
