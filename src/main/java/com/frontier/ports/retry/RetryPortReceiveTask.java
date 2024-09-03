package com.frontier.ports.retry;

public final class RetryPortReceiveTask extends AbstractRetryPortTask {

	public RetryPortReceiveTask(AbstractRetryPort retryPort) {
		super(retryPort);
	}
	
	@Override
	protected void doTask() {
		retryPort.receive();
	}
}
