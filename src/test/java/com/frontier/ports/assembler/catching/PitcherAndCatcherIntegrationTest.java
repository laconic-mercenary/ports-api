package com.frontier.ports.assembler.catching;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.junit.Test;

import com.frontier.ports.RxDataEvent;
import com.frontier.ports.RxListener;
import com.frontier.ports.TimeProvider;
import com.frontier.ports.assembler.DataPacket;
import com.frontier.ports.assembler.DataPacketizer;
import com.frontier.ports.assembler.PacketSerializer;
import com.frontier.ports.assembler.impl.DefaultPacketSerializer;
import com.frontier.ports.assembler.pitching.DataSignatureGenerator;
import com.frontier.ports.assembler.pitching.Pitcher;
import com.frontier.ports.retry.AbstractRetryPortTask;
import com.frontier.ports.retry.RetryPortReceiveTask;
import com.frontier.ports.retry.impl.udp.UdpSocket;
import com.frontier.ports.util.DataFragmentor;

public class PitcherAndCatcherIntegrationTest {
	
	private final class SysTime implements TimeProvider {
		@Override public long currentTimestampMilliseconds() {
			return System.currentTimeMillis();
		}
	}
	
	private final class CompletionListener implements DataPacketCompletionListener {

		@Override public void onDataPacketsComplete(List<DataPacket> packets, InetAddress sender) {
			System.out.println("COMPLETE");
		}
	}
	
	private final class RejectionListener implements DataPacketRejectedListener {

		@Override public void onRejected(byte[] packet, InetAddress sender) {
			System.out.println("REJECTED");
		}
	}
	
	private final class ExpiredListener implements DataExpiredListener {

		@Override
		public void onDataExpired(List<DataPacket> packetsBuilt) {
			System.out.println("EXPIRED");
		}

	}
	
	private final class RxListenerRx implements RxListener {
		
		private Catcher catcher;
		
		public void setCatcher(Catcher catcher) {
			this.catcher = catcher;
		}

		@Override
		public void onReceived(RxDataEvent data) {
			try {
				catcher.receivePacket(data.data, data.senderOfData);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private static final class UUIDSignatureGenerator implements DataSignatureGenerator {
		@Override
		public String generateSignature() {
			return UUID.randomUUID().toString();
		}
	}
	
	@Test
	public void test() throws Exception {
		Thread rxThread = null;
		TimeProvider timeProvider = new SysTime();
		UdpSocket senderPort = new UdpSocket(timeProvider, 8999, InetAddress.getLoopbackAddress());
		DataPacketizer packetizer = new DataPacketizer(new DataFragmentor());
		PacketSerializer serializer = new DefaultPacketSerializer();
		
		Pitcher p = new Pitcher(packetizer,	serializer, senderPort);
		
		UdpSocket rxSocket = new UdpSocket(timeProvider, 8998, InetAddress.getLoopbackAddress(), 45000);
		
		RxListenerRx rx = new RxListenerRx();
		
		Catcher c = new Catcher(
				serializer, 
				timeProvider, 
				new CompletionListener(), 
				new RejectionListener(), 
				new ExpiredListener(), 
				100, 
				5000L);
		
		rx.setCatcher(c);
		
		try {
			rxSocket.setRxListener(rx);
			AbstractRetryPortTask rxTask = new RetryPortReceiveTask(rxSocket);
			rxThread = new Thread(rxTask);
			rxThread.start();
			
			senderPort.setSendAddress(InetAddress.getLoopbackAddress());
			senderPort.setSendPort(8998);
			senderPort.prepare();
			
			for (int i = 0; i < 11; i++) {
				System.out.println("SENDING...");
				byte[] data = genData();
				System.out.println("sending data of len = " + data.length);
				p.send(data, 10000);
				System.out.println("SENT");
				Thread.currentThread().sleep(1000L);
			}
		} finally {
			rxSocket.close();
			senderPort.close();
		}
	}
	
	private static byte[] genData() {
		int bufSize = Math.abs(new Random().nextInt()) % (Short.MAX_VALUE * 2);
		byte[] data = new byte[bufSize];
		Arrays.fill(data, (byte)4);
		return data;
	}

}
