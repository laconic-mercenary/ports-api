/**
 * 
 */
package com.frontier.ports;

/**
 * @author mlcs05
 *
 */
public interface Port {
	void prepare();
	boolean isPrepared();
	void send(byte[] data);
	void receive();
	boolean isFinished();
	void close();
}
