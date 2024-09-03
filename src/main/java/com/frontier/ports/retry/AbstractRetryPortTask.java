/**
 * 
 */
package com.frontier.ports.retry;

import org.apache.log4j.Logger;

import com.frontier.lib.validation.ObjectValidator;

/**
 * @author mlcs05
 *
 */
public abstract class AbstractRetryPortTask implements Runnable {
	
	private static final Logger LOGGER = Logger.getLogger(AbstractRetryPortTask.class.getName());

	private volatile boolean stop = false;
	
	protected final AbstractRetryPort retryPort;
	
	public AbstractRetryPortTask(AbstractRetryPort retryPort) {
		ObjectValidator.raiseIfNull(retryPort);
		this.retryPort = retryPort;
	}
	
	public void stop() {
		LOGGER.debug("stop signaled");
		stop = true;
	}

	public void run() {
		LOGGER.trace("run()");
		final boolean YES = false;
		boolean cycle = true;
		try {
			LOGGER.debug("entering task loop");
			while(cycle == true) {
				if (retryPort.isPrepared() == false) {
					if (retryPort.isRetryReady() == true) {
						LOGGER.debug("preparing port");
						retryPort.prepare();
					} else {
						continue;
					}
				}
				doTask();
				cycle = (stop == YES) || (retryPort.isFinished() == YES);
			}
		} finally {
			LOGGER.debug("closing port");
			retryPort.close();
		}
	}
	
	protected abstract void doTask();
}
