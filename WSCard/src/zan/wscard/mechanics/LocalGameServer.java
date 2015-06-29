package zan.wscard.mechanics;

import java.util.ArrayList;

public class LocalGameServer extends GameServer {
	
	private ArrayList<String> serverInbox;
	
	public LocalGameServer() {
		super();
		serverInbox = new ArrayList<String>();
	}
	
	// TODO FOR DEBUGGING
	public Player getPlayer(int id) {
		if (id == 0) return playerA;
		else if (id == 1) return playerB;
		return null;
	}
	
	protected String getServerInbox() {
		if (serverInbox.isEmpty()) return null;
		return serverInbox.remove(0);
	}
	
}
