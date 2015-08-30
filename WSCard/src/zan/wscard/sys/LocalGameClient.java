package zan.wscard.sys;

import java.util.ArrayList;

public class LocalGameClient extends GameClient {
	
	private LocalGameServer server;
	private ArrayList<String> inbox;
	
	public LocalGameClient(LocalGameServer server) {
		this.server = server;
		inbox = new ArrayList<String>();
	}
	
	public void writeToClient(String msg) {
		inbox.add(msg);
		System.out.println("Inbox Client " + clientID + ": " + msg);
	}
	
	@Override
	protected void writeToServer(String msg) {
		server.writeToServer(clientID + " " + msg);
	}
	
	@Override
	protected String getInbox() {
		if (inbox.isEmpty()) return null;
		return inbox.remove(0);
	}
	
}
