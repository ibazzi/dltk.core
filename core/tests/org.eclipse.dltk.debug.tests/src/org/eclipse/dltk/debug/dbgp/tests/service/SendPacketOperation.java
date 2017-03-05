package org.eclipse.dltk.debug.dbgp.tests.service;

import java.io.IOException;
import java.net.Socket;

class SendPacketOperation implements ISocketOperation {
	/**
	 * 
	 */
	private final byte[] packet;

	/**
	 * @param packet
	 */
	SendPacketOperation(byte[] packet) {
		this.packet = packet;
	}

	@Override
	public void execute(Socket socket) throws IOException {
		ClientPackets.send(socket, packet);
	}
}
