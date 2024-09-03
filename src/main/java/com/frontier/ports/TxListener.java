package com.frontier.ports;

public interface TxListener {
	void onSent(byte[] data);
	void onBeforeSend(byte[] data);
}
