package zan.wscard.net;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ServerThread extends Thread {
	
	private ServerSocket server;
	private ClientThread client[];
	
	private ArrayList<String> inbox;
	
	private boolean waitingClient;
	private boolean running;
	
	public ServerThread(int port, int slot) throws IOException {
		server = new ServerSocket(port);
		server.setSoTimeout(10000);
		client = new ClientThread[slot];
		for (int i=0;i<client.length;i++) client[i] = null;
		inbox = new ArrayList<String>();
		waitingClient = true;
		running = true;
		start();
	}
	
	public void setWaitingClient(boolean sw) {
		waitingClient = sw;
	}
	
	public void writeToClient(int cid, String msg) {
		if (isClientOnline(cid)) client[cid].writeToClient(msg);
	}
	public void writeToAllClients(String msg) {
		for (int i=0;i<client.length;i++) writeToClient(i, msg);
	}
	
	public boolean closeServer() {
		try {
			for (int i=0;i<client.length;i++) if (isClientOnline(i)) client[i].closeClient();
			server.close();
			inbox.clear();
			running = false;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return !running;
	}
	
	public void removeClient(int cid) {
		client[cid] = null;
	}
	
	@Override
	public void run() {
		while(running) {
			if (!server.isClosed()) {
				try {
					Socket socket = server.accept();
					PrintWriter os = new PrintWriter(socket.getOutputStream(), true);
					
					boolean slotfull = true;
					for (int i=0;i<client.length;i++) if (client[i] == null) slotfull = false;
					
					if (waitingClient && !slotfull) {
						for (int i=0;i<client.length;i++) {
							if (isClientOnline(i)) continue;
							os.println("ACCEPTED");
							client[i] = new ClientThread(this, socket, i);
							// TODO
							client[i].writeToClient("CID " + i);
							break;
						}
					} else {
						os.println("REFUSED");
						os.close();
					}
				} catch(IOException e) {}
			}
		}
	}
	
	public void addMessage(String msg) {
		inbox.add(msg);
	}
	
	public String getInbox() {
		if (inbox.isEmpty()) return null;
		return inbox.remove(0);
	}
	
	public boolean isClientOnline(int cid) {
		return (client[cid] != null);
	}
	
	public boolean isRunning() {
		return running;
	}
	
}
