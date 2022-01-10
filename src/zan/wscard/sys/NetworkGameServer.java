package zan.wscard.sys;

import zan.lib.net.NetworkManager;

public class NetworkGameServer extends GameServer {

	private static boolean DEBUG = true;

	protected void writeToClient(int cid, String msg) {
		NetworkManager.writeToClient(cid, msg);
		if (DEBUG && msg != null) System.out.println("SERVER TO: " + cid + " " + msg);
	}

	protected void writeToAllClients(String msg) {
		NetworkManager.writeToAllClients(msg);
		if (DEBUG && msg != null) System.out.println("SERVER TO: A " + msg);
	}

	protected String getInbox() {
		String msg = NetworkManager.getServerInbox();
		if (DEBUG && msg != null) System.out.println("SERVER IN: " + msg);
		return msg;
	}

}
