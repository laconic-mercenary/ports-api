package com.frontier.ports.assembler;

import java.io.IOException;

/**
 * @author mlcs05
 *
 */
public interface PacketSerializer {
	byte[] serialize(DataPacket dataPacket) throws IOException;
	DataPacket unserialize(byte[] data) throws Exception;
}
