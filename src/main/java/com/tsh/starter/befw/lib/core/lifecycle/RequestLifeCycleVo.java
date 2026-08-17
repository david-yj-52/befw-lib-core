package com.tsh.starter.befw.lib.core.lifecycle;

import com.tsh.starter.befw.lib.core.exception.ApplicationException;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString(callSuper = true)
public class RequestLifeCycleVo {

	// elapsed time management

	// request result management

	// extra data management

	// interface management

	// processing status

	// exception management
	ApplicationException applicationException;

}
