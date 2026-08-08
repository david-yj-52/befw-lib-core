package com.tsh.starter.befw.lib.core.spec.process;

import com.tsh.starter.befw.lib.core.spec.ApMessageBody;
import com.tsh.starter.befw.lib.core.spec.ApMessageHead;
import com.tsh.starter.befw.lib.core.spec.constant.ApSystemList;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
public class ApMessageVo<T extends ApMessageBody> {

	ApMessageHead head;
	T body;
	long timestamp;
	private ApSystemList src;
	private ApSystemList tgt;

}
