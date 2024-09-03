/**
 * 
 */
package com.frontier.ports.assembler.catching;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import com.frontier.lib.validation.NumberValidation;
import com.frontier.lib.validation.ObjectValidator;
import com.frontier.ports.TimeProvider;
import com.frontier.ports.assembler.DataPacket;
import com.frontier.ports.assembler.PacketSerializer;

/**
 * @author mlcs05
 *
 */
public final class Catcher {
	
	private static final Logger LOGGER = Logger.getLogger(Catcher.class.getName());
	
	private final int catcherMapCapacity;
	
	private final long dataExpiryMilliseconds;
	
	private final PacketSerializer packetSerializer;
	
	private final TimeProvider timeProvider;
	
	private final Map<Long, List<DataPacket>> catcherMap = new HashMap<>();
	
	private final Map<Long, Long> catcherPurgeMap = new HashMap<>();
	
	private final DataPacketCompletionListener dataPacketsCompletedListener;
	
	private final DataPacketRejectedListener dataPacketRejectedListener;
	
	private final DataExpiredListener dataExpiredListener;
	
	private final Lock catcherMapLock = new ReentrantLock();
	
	private final Lock purgeMapLock = new ReentrantLock();
	
	public Catcher(
			PacketSerializer serializer,
			TimeProvider timer,
			DataPacketCompletionListener completionListener,
			DataPacketRejectedListener rejectionListener,
			DataExpiredListener expiredListener,
			int cacheMapCapacity,
			long dataExpiresMilliseconds) {
		LOGGER.trace("ctor()");
		ObjectValidator.raiseIfNull(serializer);
		ObjectValidator.raiseIfNull(timer);
		ObjectValidator.raiseIfNull(completionListener);
		ObjectValidator.raiseIfNull(rejectionListener);
		ObjectValidator.raiseIfNull(expiredListener);
		NumberValidation.raiseIfLessThanOrEqualTo(cacheMapCapacity, 0L);
		NumberValidation.raiseIfLessThanOrEqualTo(dataExpiresMilliseconds, 0L);
		this.packetSerializer = serializer;
		this.timeProvider = timer;
		this.dataPacketsCompletedListener = completionListener;
		this.dataPacketRejectedListener = rejectionListener;
		this.dataExpiredListener = expiredListener;
		this.catcherMapCapacity = cacheMapCapacity;
		this.dataExpiryMilliseconds = dataExpiresMilliseconds;
		if (LOGGER.isDebugEnabled()) {
			StringBuilder msg = new StringBuilder("Creating Catcher with following attributes: ");
			msg.append(String.format("- timeProvider=%s", this.timeProvider));
			msg.append(String.format("- packetCompletionListener=%s", this.dataPacketsCompletedListener));
			msg.append(String.format("- packetRejectionListener=%s", this.dataPacketRejectedListener));
			msg.append(String.format("- packetExpiredListener=%s", this.dataExpiredListener));
			msg.append(String.format("- cacheSize=%d", this.catcherMapCapacity));
			msg.append(String.format("- dataExpiryMilliseconds=%d", this.dataExpiryMilliseconds));
			LOGGER.debug(msg);
		}
	}

	public void receivePacket(byte[] dataPacket, InetAddress sender) throws Exception {
		LOGGER.trace("receivePacket()");
		if (catcherMap.size() >= catcherMapCapacity) {
			LOGGER.debug("rejected packet of size: " + dataPacket.length);
			firePacketRejected(dataPacket, sender);
			// maybe purging the cache of incomplete entries 
			// will help with this
			purge();
		} else {
			DataPacket packet = packetSerializer.unserialize(dataPacket);
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(String.format(
					"receiving packet into cache: signature=%s, index %d, packet size %d", 
					packet.dataSignature, packet.packetIndex, packet.packet.length
				));
			}
			List<DataPacket> packetList = addToCatcherMap(packet);
			checkCompletion(packetList, packet, sender);
		}
	}
	
	public void purge() {
		LOGGER.trace("purge()");
		LOGGER.debug("starting purge of cache...");
		purgeMapLock.lock(); // PURGE MAP LOCK
		try {
			long currentTime = timeProvider.currentTimestampMilliseconds();
			Iterator<Long> iterator = catcherPurgeMap.keySet().iterator();
			LOGGER.debug(String.format("purge map has %d entries", catcherPurgeMap.size()));
			while (iterator.hasNext()) {
				long signature = iterator.next();
				long dataEnteredStamp = catcherPurgeMap.get(signature);
				dataEnteredStamp += dataExpiryMilliseconds;
				if (currentTime >= dataEnteredStamp) {
					LOGGER.debug("purging entry");
					removeFromMap(signature, catcherPurgeMap);
					catcherMapLock.lock(); // CATCHER MAP LOCK
					try {
						fireDataExpired(removeFromMap(signature, catcherMap));
					} finally {
						catcherMapLock.unlock(); // UNLOCK
					}
				}
			}
		} finally {
			purgeMapLock.unlock(); // UNLOCK
		}
		LOGGER.debug("cache purge complete");
	}
	
	private List<DataPacket> addToCatcherMap(DataPacket dataPacket) {
		LOGGER.trace("addToCatcherMap()");
		List<DataPacket> packetList = null;
		catcherMapLock.lock(); // CATCHER MAP LOCK
		try {
			packetList = catcherMap.get(dataPacket.dataSignature);
			if (packetList == null) {
				LOGGER.debug("brand new packet entry received");
				packetList = new LinkedList<>();
				catcherMap.put(dataPacket.dataSignature, packetList);
				purgeMapLock.lock(); // PURGE MAP LOCK
				try {
					catcherPurgeMap.put(dataPacket.dataSignature, 
							timeProvider.currentTimestampMilliseconds());
				} finally {
					purgeMapLock.unlock(); // UNLOCK
				}
			}
			packetList.add(dataPacket);
		} finally {
			catcherMapLock.unlock(); // UNLOCK
		}
		return packetList;
	}
	
	private void checkCompletion(List<DataPacket> packetList, DataPacket packet, InetAddress address) {
		if (packet.packetTotalCount == packetList.size()) {
			LOGGER.debug("payload has been completely received");
			int packetSize = 0;
			for (DataPacket tmpPacket : packetList)
				packetSize += tmpPacket.packet.length;
			if (packetSize != packet.dataTotalSize) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.warn(String.format("assembled packet sizes are not equal :: Expected=%d Got=%d", packet.dataTotalSize, packetSize));
				}
			}
			firePacketsCompleted(packetList, address);
			catcherMapLock.lock();
			try {
				removeFromMap(packet.dataSignature, catcherMap);
			} finally {
				catcherMapLock.unlock();
			}
			purgeMapLock.lock();
			try {
				removeFromMap(packet.dataSignature, catcherPurgeMap);
			} finally {
				purgeMapLock.unlock();
			}
		}
	}
	
	private void firePacketsCompleted(List<DataPacket> packets, InetAddress sender) {
		// use this trace logging to determine
		// if a listener is taking too much time
		LOGGER.trace("firePacketsCompleted() START");
		if (dataPacketsCompletedListener != null) {
			dataPacketsCompletedListener.onDataPacketsComplete(packets, sender);
		}
		LOGGER.trace("firePacketsCompleted() END");
	}
	
	private void firePacketRejected(byte[] packet, InetAddress sender) {
		LOGGER.trace("firePacketRejected() START");
		if (dataPacketRejectedListener != null) {
			dataPacketRejectedListener.onRejected(packet, sender);
		}
		LOGGER.trace("firePacketRejected() END");
	}
	
	private void fireDataExpired(List<DataPacket> packetsBuilt) {
		LOGGER.trace("fireDataExpired() START");
		if (dataExpiredListener != null) {
			dataExpiredListener.onDataExpired(packetsBuilt);
		}
		LOGGER.trace("fireDataExpired() END");
	}
	
	private <T> T removeFromMap(long dataSignature, Map<Long, T> map) {
		return map.remove(dataSignature);
	}
}
