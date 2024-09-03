/**
 * 
 */
package com.frontier.ports.assembler;

import java.util.Arrays;
import java.util.List;

import com.frontier.lib.validation.ObjectValidator;
import com.frontier.ports.util.DataFragmentor;

/**
 * @author mlcs05
 *
 */
public final class DataPacketizer {

	private final DataFragmentor fragmentor;

	public DataPacketizer(DataFragmentor fragmentor) {
		ObjectValidator.raiseIfNull(fragmentor);
		this.fragmentor = fragmentor;
	}

	public DataPacket[] packetize(byte[] data, int chunkSize) {
		List<int[]> fragIndices = fragmentor.determineIndices(data, chunkSize);
		DataPacket[] packets = new DataPacket[fragIndices.size()];
		final short packetTotalCount = (short) fragIndices.size();
		final int dataSize = data.length;
		for (short i = 0; i < packetTotalCount; i++) {
			DataPacket subPacket = new DataPacket();
			subPacket.dataTotalSize = dataSize;
			subPacket.packetIndex = i;
			subPacket.packetTotalCount = packetTotalCount;
			int startIndex = fragIndices.get(i)[0];
			int finalIndex = fragIndices.get(i)[1] + 1; // last index is EXCLUSIVE in copyOfRange()
			subPacket.packet = Arrays.copyOfRange(
					data, 
					startIndex,
					finalIndex);
			packets[i] = subPacket;
		}
		return packets;
	}

}
