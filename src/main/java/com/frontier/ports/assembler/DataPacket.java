/**
 * 
 */
package com.frontier.ports.assembler;

import java.io.Serializable;

/**
 * @author mlcs05
 *
 */
public final class DataPacket implements Serializable {

	private static final long serialVersionUID = 2015121716502L;

	public byte[] packet = null;
	
	public byte dataType = -1;
	
	public long dataTotalSize = -1L;
	
	public short packetIndex = -1;
	
	public short packetTotalCount = -1;
	
	public long dataSignature = 0;
}
