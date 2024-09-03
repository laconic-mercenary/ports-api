/**
 * 
 */
package com.frontier.ports.assembler.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.frontier.ports.assembler.DataPacket;
import com.frontier.ports.assembler.PacketSerializer;

/**
 * @author mlcs05
 *
 */
public class DefaultPacketSerializer implements PacketSerializer {

	@Override
	public byte[] serialize(DataPacket dataPacket) throws IOException {
		byte[] result = null;
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			try (ObjectOutputStream out = new ObjectOutputStream(baos)) {
				out.writeObject(dataPacket);
			}
			result = baos.toByteArray();
		}
        return result;
	}

	@Override
	public DataPacket unserialize(byte[] data) throws Exception {
		DataPacket result = null;
		try (ByteArrayInputStream bais = new ByteArrayInputStream(data)) {
			try (ObjectInputStream in = new ObjectInputStream(bais)) {
				result = (DataPacket)in.readObject();
			}
		}
		return result;
	}
}
