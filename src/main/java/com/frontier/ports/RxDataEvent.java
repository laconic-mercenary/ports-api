/**
 * 
 */
package com.frontier.ports;

import java.net.InetAddress;

/**
 * @author mlcs05
 *
 */
public final class RxDataEvent {

	public final byte[] data;
	
	public final int dataLength;
	
	public final InetAddress senderOfData;
	
	public RxDataEvent(byte[] data, InetAddress sender, int dataLength) {
		this.data = data;
		this.senderOfData = sender;
		this.dataLength = dataLength;
	}
}
