package com.tsh.starter.befw.lib.core.apService.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class DateTimeUtil {

	static final String SECOND_TIME_FORMAT = "yyyyMMddHHmmss";
	static final String M_SECOND_TIME_FORMAT = "yyyyMMddHHmmssSSS";
	static final String SECOND_UI_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
	static final String M_SECOND_UI_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";

	public static String getServerMilSecondTime() {
		return DateTimeUtil.getCurrentTime(M_SECOND_TIME_FORMAT);
	}

	public static String getUiMilSecondTime() {
		return DateTimeUtil.getCurrentTime(M_SECOND_UI_TIME_FORMAT);
	}

	public static String getServerSecondTime() {
		return DateTimeUtil.getCurrentTime(SECOND_TIME_FORMAT);
	}

	public static String getUiSecondTime() {
		return DateTimeUtil.getCurrentTime(SECOND_UI_TIME_FORMAT);
	}

	private static String getCurrentTime(String foramt) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(foramt);

		return LocalDateTime.ofInstant(
			Instant.ofEpochMilli(System.currentTimeMillis()),
			ZoneId.systemDefault()
		).format(formatter);
	}

}
