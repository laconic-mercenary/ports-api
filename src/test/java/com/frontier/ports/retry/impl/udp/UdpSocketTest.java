/**
 * 
 */
package com.frontier.ports.retry.impl.udp;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.Random;

import org.junit.Test;

import com.frontier.ports.RxDataEvent;
import com.frontier.ports.RxListener;
import com.frontier.ports.TimeProvider;
import com.frontier.ports.TxListener;
import com.frontier.ports.retry.RetryPortReceiveTask;

/**
 * @author mlcs05
 *
 */
public class UdpSocketTest {

	private UdpSocket tx = null;

	private UdpSocket rx = null;

	private static final class SysTime implements TimeProvider {
		public long currentTimestampMilliseconds() {
			return System.currentTimeMillis();
		}
	}
	
	private static final class Txl implements TxListener {

		public void onSent(byte[] data) {
			System.out.println("SENT::" + data.length);
		}

		public void onBeforeSend(byte[] data) {
			System.out.println("BEFORE SEND::" + data.length);
		}
	}
	
	private static final class Rxl implements RxListener {

		public void onReceived(RxDataEvent data) {
			System.out.println("RX::" + data.data.length + " -- " + data.senderOfData);
		}
		
	}

	@Test
	public void test() {
		final int txBindPort = 12322;
		final int rxBindPort = 12332;
		final int bufSize = 120000;
		Thread testThread = null;
		try {
			tx = new UdpSocket(new SysTime(), txBindPort,
					InetAddress.getLoopbackAddress(), bufSize);
			tx.setSendAddress(InetAddress.getLoopbackAddress());
			tx.setSendPort(rxBindPort);
			tx.setTxListener(new Txl());
			tx.prepare();
			
			try {
				rx = new UdpSocket(new SysTime(), rxBindPort,
						InetAddress.getLoopbackAddress(), bufSize);
				rx.setRxListener(new Rxl());
				rx.prepare();
				
				testThread = new Thread(new RetryPortReceiveTask(rx));
				testThread.start();
				
				try {
					Thread.currentThread().sleep(2000L);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}

				doTest(tx);
				try {
					Thread.currentThread().sleep(5000L);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			} finally {
				rx.close();
			}
		} finally {
			tx.close();
		}
	}
	
	private static void doTest(UdpSocket sender) {
		sender.send(genData());
		sender.send(genData());
		sender.send(genData());
	}

	private static byte[] genData() {
		int bufSize = Math.abs(new Random().nextInt()) % (Short.MAX_VALUE * 2);
		byte[] data = new byte[bufSize];
		Arrays.fill(data, (byte)4);
		return data;
	}
}
