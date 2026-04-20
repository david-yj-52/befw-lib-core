package com.tsh.starter.befw.lib.core.messaging;

public abstract class AbstractMessageSessionManager {

	protected abstract void startSession();

	protected abstract void stopSession();

	protected abstract void checkSession();
}
