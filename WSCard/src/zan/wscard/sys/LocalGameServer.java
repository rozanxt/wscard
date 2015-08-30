package zan.wscard.sys;

import java.util.ArrayList;

import zan.wscard.sys.LocalGameClient;

public class LocalGameServer extends GameServer {
	
	protected ArrayList<LocalGameClient> clients;
	protected ArrayList<String> inbox;
	
	public LocalGameServer() {
		clients = new ArrayList<LocalGameClient>();
		inbox = new ArrayList<String>();
	}
	
	public void addClient(LocalGameClient client) {
		client.writeToClient("CID " + clients.size());
		clients.add(client);
	}
	
	public void writeToServer(String msg) {
		inbox.add(msg);
		//System.out.println("Inbox Server: " + msg);
	}
	
	@Override
	protected void writeToClient(int cid, String msg) {
		clients.get(cid).writeToClient(msg);
	}
	
	@Override
	protected void writeToAllClients(String msg) {
		for (int i=0;i<clients.size();i++) writeToClient(i, msg);
	}
	
	@Override
	protected String getInbox() {
		if (inbox.isEmpty()) return null;
		return inbox.remove(0);
	}
	
}
