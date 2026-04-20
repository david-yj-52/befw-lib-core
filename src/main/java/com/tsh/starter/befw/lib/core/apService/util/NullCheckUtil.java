package com.tsh.starter.befw.lib.core.apService.util;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NullCheckUtil {

	public static List<String> detectBlank(Map<String, String> paramMap) {
		return paramMap.entrySet().stream()
			.filter(e -> e.getValue() == null || e.getValue().isBlank())
			.map(Map.Entry::getKey)
			.collect(Collectors.toList());
	}

	private static boolean requireNonBlank(String argNm, String arg, boolean throwException) {
		if (arg == null || arg.isBlank()) {

			String message = String.format("%s is blank.", argNm);

			// TODO Custom Exception 고민 필요
			if (throwException) {
				throw new IllegalArgumentException(message);
			} else {
				return false;
			}
		}
		return true;
	}
}
