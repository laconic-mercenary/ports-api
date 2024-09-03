package com.frontier.ports;

/**
 * 
 * @author mlcs05
 *
 */
public abstract class AbstractListenerPort implements Port {

	private boolean hasTx = false;
	private boolean hasRx = false;
	private RxListener rx = null;
	private TxListener tx = null;
	
	public void send(byte[] data) {
		fireBeforeSend(data);
		doSend(data);
		fireSent(data);
	}
	
	public void receive() {
		RxDataEvent data = doReceive();
		fireReceived(data);
	}
	
	public void setRxListener(RxListener rx) {
		this.rx = rx;
		this.hasRx = rx != null;
	}
	
	public void setTxListener(TxListener tx) {
		this.tx = tx;
		this.hasTx = tx != null;
	}
	
	protected abstract void doSend(byte[] data);
	protected abstract RxDataEvent doReceive();
	
	private void fireReceived(RxDataEvent data) {
		if (hasRx)
			rx.onReceived(data);
	}
	
	private void fireBeforeSend(byte[] data) {
		if (hasTx)
			tx.onBeforeSend(data);
	}
	
	private void fireSent(byte[] data) {
		if (hasTx)
			tx.onSent(data);
	}
}
