/**
 * 
 */
package com.frontier.ports.retry;

/**
 * @author mlcs05
 *
 */
public interface TxDataThroughputListener {
	void onDataSubmittedForSend(byte[] data, int queueCapacity);
	void onDataRejectedForSend(byte[] data, AbstractRetryPortTask portTask);
}
