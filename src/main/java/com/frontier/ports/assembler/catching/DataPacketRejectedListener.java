/**
 * 
 */
package com.frontier.ports.assembler.catching;

import java.net.InetAddress;


/**
 * @author mlcs05
 *
 */
public interface DataPacketRejectedListener {
	void onRejected(byte[] packet, InetAddress sender);
}
