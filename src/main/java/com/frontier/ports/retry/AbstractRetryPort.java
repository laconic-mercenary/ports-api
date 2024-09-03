/**
 * 
 */
package com.frontier.ports.retry;

import com.frontier.lib.validation.NumberValidation;
import com.frontier.lib.validation.ObjectValidator;
import com.frontier.ports.AbstractListenerPort;
import com.frontier.ports.TimeProvider;

/**
 * @author mlcs05
 *
 */
public abstract class AbstractRetryPort extends AbstractListenerPort {

	private long retryInterval = 0L;
	private long nextRetry = 0L;
	
	private final TimeProvider timeProvider;
	
	public AbstractRetryPort(TimeProvider timeProvider) {
		ObjectValidator.raiseIfNull(timeProvider);
		this.timeProvider = timeProvider;
	}
	
	public boolean isRetryReady() {
		return check();
	}
	
	public void setRetryInterval(long retryInterval) {
		NumberValidation.raiseIfLessThan(retryInterval, 0L);
		this.retryInterval = retryInterval;
		long current = timeProvider.currentTimestampMilliseconds();
		nextRetry = current + retryInterval;
	}
	
	private boolean check() {
		boolean retryReady = false;
		long current = timeProvider.currentTimestampMilliseconds();
		if (current >= nextRetry) {
			retryReady = true;
			nextRetry = current + retryInterval;
		}
		return retryReady;
	}
}
