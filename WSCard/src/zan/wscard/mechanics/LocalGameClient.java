package zan.wscard.mechanics;

import java.util.ArrayList;

public class LocalGameClient extends GameClient {
	
	private LocalGameServer gameServer;
	
	private ArrayList<String> clientInbox;
	
	public LocalGameClient(LocalGameServer server, int cid) {
		super(cid);
		gameServer = server;
		clientInbox = new ArrayList<String>();
	}
	
	public void writeToClient(String msg) {
		clientInbox.add(msg);
	}
	
	@Override
	protected String getClientInbox() {
		if (clientInbox.isEmpty()) return null;
		return clientInbox.remove(0);
	}
	
	@Override
	public void writeToServer(String msg) {
		gameServer.writeToServer(clientID + " " + msg);
	}
	
}
