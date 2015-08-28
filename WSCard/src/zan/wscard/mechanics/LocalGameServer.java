package zan.wscard.mechanics;

import java.util.ArrayList;

public class LocalGameServer extends GameServer {
	
	private ArrayList<LocalGameClient> gameClient;
	
	private ArrayList<String> serverInbox;
	
	public LocalGameServer() {
		gameClient = new ArrayList<LocalGameClient>();
		serverInbox = new ArrayList<String>();
	}
	
	public void addClient(LocalGameClient client) {
		gameClient.add(client);
	}
	
	public void writeToServer(String msg) {
		serverInbox.add(msg);
	}
	
	@Override
	protected String getServerInbox() {
		if (serverInbox.isEmpty()) return null;
		return serverInbox.remove(0); 
	}
	
	@Override
	public void writeToClient(int cid, String msg) {
		gameClient.get(cid).writeToClient(msg);
	}
	
}
