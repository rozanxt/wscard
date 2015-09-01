package zan.wscard.net;

import java.io.IOException;
import java.net.UnknownHostException;

public class NetworkManager {
	
	private static ServerThread server;
	private static SocketThread client;
	
	public static void init() {
		server = null;
		client = null;
	}
	
	public static boolean openServer(int port, int slot) {
		if (port > 60000) return false;
		if (isServerRunning()) closeServer();
		boolean done = false;
		try {
			server = new ServerThread(port, slot);
			done = true;
		} catch(IOException e) {
			e.printStackTrace();
		}
		return done;
	}
	public static boolean openClient(String address, int port) {
		if (port > 60000) return false;
		if (isClientRunning()) closeClient();
		boolean done = false;
		try {
			client = new SocketThread(address, port);
			if (client.isRunning()) done = true;
		} catch(UnknownHostException e) {
			e.printStackTrace();
		} catch(IOException e) {
			e.printStackTrace();
		}
		return done;
	}
	
	public static boolean closeServer() {
		if (!isServerRunning()) return false;
		return server.closeServer();
	}
	public static boolean closeClient() {
		if (!isClientRunning()) return false;
		return client.closeSocket();
	}
	
	public static void setServerWaiting(boolean sw) {
		if (!isServerRunning()) return;
		server.setWaitingClient(sw);
	}
	
	public static boolean writeToServer(String msg) {
		if (!isClientRunning()) return false;
		client.writeToServer(msg);
		return true;
	}
	public static boolean writeToClient(int cid, String msg) {
		if (!isServerRunning()) return false;
		server.writeToClient(cid, msg);
		return true;
	}
	public static boolean writeToAllClients(String msg) {
		if (!isServerRunning()) return false;
		server.writeToAllClients(msg);
		return true;
	}
	
	public static String getServerInbox() {
		if (!isServerRunning()) return null;
		return server.getInbox();
	}
	public static String getClientInbox() {
		if (!isClientRunning()) return null;
		return client.getInbox();
	}
	
	public static boolean isClientOnline(int cid) {
		if (!isServerRunning()) return false;
		return server.isClientOnline(cid);
	}
	
	public static boolean isServerRunning() {
		if (server == null) return false;
		return server.isRunning();
	}
	public static boolean isClientRunning() {
		if (client == null) return false;
		return client.isRunning();
	}
	
}
