/**
 * 
 */
package com.frontier.ports.assembler.catching;

import java.util.List;

import com.frontier.ports.assembler.DataPacket;

/**
 * @author mlcs05
 *
 */
public interface DataExpiredListener {
	void onDataExpired(List<DataPacket> packetsBuilt);
}
