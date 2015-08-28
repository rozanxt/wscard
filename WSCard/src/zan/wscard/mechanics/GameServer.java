package zan.wscard.mechanics;

import java.util.ArrayList;

import zan.wscard.card.CardData;

public abstract class GameServer {
	
	protected PlayerServer playerA, playerB;
	
	protected int playerTurn;
	
	public void init(ArrayList<CardData> deckA, ArrayList<CardData> deckB) {
		playerA = new PlayerServer(0, "Player A", deckA);
		playerB = new PlayerServer(1, "Player B", deckB);
		playerA.init();
		playerB.init();
		drawCard(0, 5);
		drawCard(1, 5);
		
		playerTurn = 0;
		writeToClient(playerTurn, "TURN " + playerTurn);
		getPlayerInTurn().doStandUp();
		drawCard(playerTurn, 1);
	}
	
	public void update() {
		String msg = getServerInbox();
		while (msg != null && !msg.isEmpty()) {
			String[] tkns = msg.split(" ");
			int cid = Integer.parseInt(tkns[0]);
			
			if (tkns[1].contentEquals("REQ_HAND")) {
				ArrayList<Integer> hand = getPlayer(cid).getPlayerHand();
				String m = "HAND";
				for (int i=0;i<hand.size();i++) m += " " + hand.get(i);
				writeToClient(cid, m);
			}
			if (tkns[1].contentEquals("ENDTURN")) {
				if (cid == playerTurn) {
					if (cid == 0) playerTurn = 1;
					else if (cid == 1) playerTurn = 0;
					writeToClient(0, "TURN " + playerTurn);
					writeToClient(1, "TURN " + playerTurn);
					writeToClient(playerTurn, "TURN " + playerTurn);
					getPlayerInTurn().doStandUp();
					drawCard(playerTurn, 1);
				}
			}
			
			msg = getServerInbox();
		}
	}
	
	protected void drawCard(int pid, int num) {
		ArrayList<Integer> drawn = getPlayer(pid).drawCard(num);
		String msg = "DRAW";
		for (int i=0;i<drawn.size();i++) msg += " " + drawn.get(i);
		writeToClient(pid, msg);
	}
	
	protected PlayerServer getPlayer(int pid) {
		if (playerA.getPlayerID() == pid) return playerA;
		if (playerB.getPlayerID() == pid) return playerB;
		return null;
	}
	
	protected PlayerServer getPlayerInTurn() {
		return getPlayer(playerTurn);
	}
	
	protected abstract String getServerInbox();
	public abstract void writeToClient(int cid, String msg);
	
}
