package com.tsh.starter.befw.lib.core.annotation.message;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.tsh.starter.befw.lib.core.ApMessage;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ReplyWith {
	Class<? extends ApMessage> value();
}