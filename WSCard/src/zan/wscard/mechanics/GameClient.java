package zan.wscard.mechanics;

import java.util.ArrayList;

import zan.wscard.card.CardData;

public abstract class GameClient {
	
	protected PlayerClient playerA, playerB;
	
	protected int clientID;
	protected boolean clientTurn;
	
	public GameClient(int cid) {
		clientID = cid;
		clientTurn = false;
	}
	
	public void init(ArrayList<CardData> deckA, ArrayList<CardData> deckB) {
		playerA = new PlayerClient(0, "Player A", deckA);
		playerB = new PlayerClient(1, "Player B", deckB);
	}
	
	public void update() {
		String msg = getClientInbox();
		while (msg != null && !msg.isEmpty()) {
			String[] tkns = msg.split(" ");
			
			if (tkns[0].contentEquals("DRAW")) {
				ArrayList<Integer> drawn = new ArrayList<Integer>();
				for (int i=1;i<tkns.length;i++) drawn.add(Integer.parseInt(tkns[i]));
				getClientPlayer().drawCard(drawn);
			}
			if (tkns[0].contentEquals("TURN")) {
				if (Integer.parseInt(tkns[1]) == clientID) clientTurn = true;
				else clientTurn = false;
			}
			
			msg = getClientInbox();
		}
	}
	
	public boolean isInTurn() {
		return clientTurn;
	}
	
	public PlayerClient getClientPlayer() {
		if (playerA.getPlayerID() == clientID) return playerA;
		if (playerB.getPlayerID() == clientID) return playerB;
		return null;
	}
	
	protected abstract String getClientInbox();
	public abstract void writeToServer(String msg);
	
}
