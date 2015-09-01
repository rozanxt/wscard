package zan.wscard.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class SocketThread extends Thread {
	
	private Socket socket;
	
	private BufferedReader socketIn;
	private PrintWriter socketOut;
	
	private ArrayList<String> inbox;
	
	private boolean running;
	
	public SocketThread(String address, int port) throws UnknownHostException, IOException {
		socket = new Socket();
		socket.connect(new InetSocketAddress(address, port), 1000);
		socketIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		socketOut = new PrintWriter(socket.getOutputStream(), true);
		inbox = new ArrayList<String>();
		
		if (socketIn.readLine().contentEquals("ACCEPTED")) {
			running = true;
		} else {
			running = false;
			socket.close();
		}
		start();
	}
	
	public void writeToServer(String msg) {
		socketOut.println(msg);
	}
	
	public boolean closeSocket() {
		try {
			writeToServer("DISCONNECT");
			socket.close();
			inbox.clear();
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
					inbox.add(msg);
					if (msg.contentEquals("DISCONNECT")) closeSocket();
				}
			} catch (IOException e) {
				inbox.clear();
				running = false;
			}
		}
	}
	
	public String getInbox() {
		if (inbox.isEmpty()) return null;
		return inbox.remove(0);
	}
	
	public boolean isRunning() {
		return running;
	}
	
}
