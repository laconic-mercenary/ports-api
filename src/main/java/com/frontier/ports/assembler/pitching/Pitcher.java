/**
 * 
 */
package com.frontier.ports.assembler.pitching;

import org.apache.log4j.Logger;

import com.frontier.lib.threading.ThreadUtils;
import com.frontier.lib.validation.ObjectValidator;
import com.frontier.ports.Port;
import com.frontier.ports.assembler.DataPacket;
import com.frontier.ports.assembler.DataPacketizer;
import com.frontier.ports.assembler.PacketSerializer;

/**
 * @author mlcs05
 *
 */
public final class Pitcher {
	
	private static final Logger LOGGER = Logger.getLogger(Pitcher.class.getName());

	private static final byte NODATATYPE = -1;
	
	private final DataPacketizer dataPacketizer;

	private final PacketSerializer packetSerializer;

	private final Port dataPort;
	
	public Pitcher(
			DataPacketizer packetizer, 
			PacketSerializer serializer,
			Port port) {
		LOGGER.trace("ctor()");
		ObjectValidator.raiseIfNull(packetizer);
		ObjectValidator.raiseIfNull(serializer);
		ObjectValidator.raiseIfNull(port);
		this.dataPacketizer = packetizer;
		this.packetSerializer = serializer;
		this.dataPort = port;
	}

	public void send(byte[] payload, int chunkSize) throws Exception {
		send(payload, chunkSize, 0L);
	}
	
	// the delay version is high recommended for sending large payloads
	public void send(byte[] payload, int chunkSize, long delayMillis) throws Exception {
		send(NODATATYPE, payload, chunkSize, delayMillis);
	}
	
	public void send(byte dataType, byte[] payload, int chunkSize, long delayMillis) throws Exception {
		LOGGER.trace("send()");
		ObjectValidator.raiseIfNull(payload);
		boolean delay = delayMillis != 0;
		DataPacket[] packets = dataPacketizer.packetize(payload, chunkSize);
		LOGGER.debug("generating signature");
		long signature = generateSignature(packets.hashCode());
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(String.format(
				"will send off %d packets, signature %s, payload size %d, chunk size %d", 
				packets.length, signature, payload.length, chunkSize
			));
			LOGGER.debug("actual payload size sent will be different due to serialization");
		}
		for (DataPacket packet : packets) {
			packet.dataSignature = signature;
			packet.dataType = dataType;
			byte[] data = packetSerializer.serialize(packet);
			if (delay) {
				ThreadUtils.sleep(delayMillis);
			}
			dataPort.send(data);
		}
	}
	
	private static long generateSignature(int extraHash) {
		long sig = System.currentTimeMillis();
		sig += extraHash;
		return sig;
	}
}
