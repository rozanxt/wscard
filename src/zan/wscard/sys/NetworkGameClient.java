package zan.wscard.sys;

import zan.lib.net.NetworkManager;

public class NetworkGameClient extends GameClient {

	public NetworkGameClient() {super();}

	protected void writeToServer(String msg) {
		if (!NetworkManager.writeToServer(msg)) System.err.println("MESSAGE NOT SENT");	// TODO
	}

	protected String getInbox() {
		return NetworkManager.getClientInbox();
	}

}
