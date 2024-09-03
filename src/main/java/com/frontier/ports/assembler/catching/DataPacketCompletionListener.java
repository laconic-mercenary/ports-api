/**
 * 
 */
package com.frontier.ports.assembler.catching;

import java.net.InetAddress;
import java.util.List;

import com.frontier.ports.assembler.DataPacket;

/**
 * @author mlcs05
 *
 */
public interface DataPacketCompletionListener {
	void onDataPacketsComplete(List<DataPacket> packets, InetAddress sender);
}
