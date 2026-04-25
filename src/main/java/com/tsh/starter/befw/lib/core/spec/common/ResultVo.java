package com.tsh.starter.befw.lib.core.spec.common;

import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public class ResultVo {

	// TODO enum 화 필요
	String resultCode;
	String resultComment;
}
