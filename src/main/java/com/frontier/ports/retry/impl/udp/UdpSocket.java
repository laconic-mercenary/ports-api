/**
 * 
 */
package com.frontier.ports.retry.impl.udp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import com.frontier.lib.validation.NumberValidation;
import com.frontier.lib.validation.ObjectValidator;
import com.frontier.ports.RxDataEvent;
import com.frontier.ports.TimeProvider;
import com.frontier.ports.retry.AbstractRetryPort;

/**
 * @author mlcs05
 *
 */
public class UdpSocket extends AbstractRetryPort {
	
	private static final Logger LOGGER = Logger.getLogger(UdpSocket.class.getName());

	private DatagramSocket udpSocket = null;

	private InetAddress sendAddress = null;

	private int sendPort = -1;

	private final InetAddress bindAddress;

	private final int bindPort;

	private final Lock socketLock = new ReentrantLock();
	
	private final int receiveBufferSize;
	
	private final DatagramPacket receiveBuffer = new DatagramPacket(new byte[0], 0);

	/**
	 * Use this if meant to be a sending socket.
	 */
	public UdpSocket(
			TimeProvider timeProvider, 
			int port,
			InetAddress bindAddress) {
		this(timeProvider, port, bindAddress, 0);
	}

	/**
	 * Use this if meant to be a receiving or sendng socket.
	 */
	public UdpSocket(
			TimeProvider timeProvider, 
			int port,
			InetAddress bindAddress, 
			int receiveBufferSize) {
		super(timeProvider);
		LOGGER.trace("ctor()");
		ObjectValidator.raiseIfNull(bindAddress);
		NumberValidation.raiseIfLessThan(port, 0L);
		NumberValidation.raiseIfLessThan(receiveBufferSize, 0L);
		this.bindAddress = bindAddress;
		this.bindPort = port;
		this.receiveBufferSize = receiveBufferSize;
	}

	public void prepare() {
		LOGGER.trace("prepare()");
		LOGGER.debug(String.format(
			"binding socket to %s:%d, buffer-size:%d", bindAddress, bindPort, receiveBufferSize
		));
		socketLock.lock();
		try {
			if (udpSocket != null) {
				throw new IllegalStateException("Socket already prepared.");
			}
			udpSocket = new DatagramSocket(bindPort, bindAddress);
		} catch (SocketException se) {
			wrapThrow(se);
		} finally {
			socketLock.unlock();
		}
	}

	public boolean isPrepared() {
		return (udpSocket != null);
	}

	public boolean isFinished() {
		return false; // allow caller to decide
	}

	public void close() {
		LOGGER.trace("close()");
		LOGGER.debug("closing socket");
		socketLock.lock();
		try {
			if (udpSocket != null) {
				udpSocket.close();
				udpSocket = null;
			}
		} finally {
			socketLock.unlock();
		}
	}

	public void setSendAddress(InetAddress sendAddress) {
		ObjectValidator.raiseIfNull(sendAddress);
		this.sendAddress = sendAddress;
	}

	public void setSendPort(int sendPort) {
		NumberValidation.raiseIfLessThan(sendPort, 0L);
		this.sendPort = sendPort;
	}

	@Override
	protected void doSend(byte[] data) {
		LOGGER.trace("doSend()");
		DatagramPacket packet = new DatagramPacket(
			data, 0, data.length,
			sendAddress, sendPort);
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(String.format("sending data of length %d to %s", data.length, sendAddress));
		}
		try {
			udpSocket.send(packet);
		} catch (Exception e) {
			wrapThrow(e);
		}
	}

	@Override
	protected RxDataEvent doReceive() {
		// obsevation: sockets sending to this socket that have NO delay will be very fast
		// so it's very important that this receive operation also be very quick
		// minimize logging as much as possible
		receiveBuffer.setData(
			new byte[this.receiveBufferSize], 
			0, 
			this.receiveBufferSize
		);
		try {
			// if close is called while this is blocking, an exception is thrown
			udpSocket.receive(receiveBuffer);
		} catch (Exception e) {
			wrapThrow(e);
		}
		InetAddress sender = receiveBuffer.getAddress();
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(String.format("RX DATA: %s, %d bytes", sender, receiveBuffer.getLength()));
		}
		// be aware: the buffer contained in getData() is not the trimmed down 
		// version of the buffer, which is why we pass in the receiveBuffer length
		// which contains the actual amount of data that was received
		return new RxDataEvent(receiveBuffer.getData(), sender, receiveBuffer.getLength());
	}

	private static void wrapThrow(Throwable t) {
		throw new RuntimeException(t);
	}
}
