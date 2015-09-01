package zan.wscard.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientThread extends Thread {
	
	private ServerThread server;
	private Socket client;
	
	private BufferedReader socketIn;
	private PrintWriter socketOut;
	
	private int clientID;
	
	private boolean running;
	
	public ClientThread(ServerThread ss, Socket sc, int id) throws IOException {
		server = ss;
		client = sc;
		socketIn = new BufferedReader(new InputStreamReader(client.getInputStream()));
		socketOut = new PrintWriter(client.getOutputStream(), true);
		clientID = id;
		running = true;
		start();
	}
	
	public void writeToClient(String msg) {
		socketOut.println(msg);
	}
	
	public boolean closeClient() {
		try {
			writeToClient("DISCONNECT");
			client.close();
			running = false;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return !running;
	}
	
	@Override
	public void run() {
		while (running) {
			try {
				String msg;
				while ((msg = socketIn.readLine()) != null) {
					server.addMessage(clientID + " " + msg);
					if (msg.contentEquals("DISCONNECT")) closeClient();
				}
			} catch(IOException e) {
				running = false;
			}
		}
		server.removeClient(clientID);
	}
	
	public boolean isRunning() {
		return running;
	}
	
}
