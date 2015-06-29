package zan.wscard.mechanics;

import java.util.ArrayList;
import java.util.Random;

import zan.wscard.card.CardData;

public abstract class GameServer {
	
	protected Random rnd;
	
	protected Player playerA, playerB;
	
	protected int playerTurn;
	protected int playerPhase;
	
	public GameServer() {
		rnd = new Random();
	}
	
	public void initialPhase(ArrayList<CardData> testDeck) {
		ArrayList<CardData> deckA = new ArrayList<CardData>();
		for (int i=0;i<50;i++) deckA.add(testDeck.get(rnd.nextInt(20)));
		ArrayList<CardData> deckB = new ArrayList<CardData>();
		for (int i=0;i<50;i++) deckB.add(testDeck.get(rnd.nextInt(20)));
		
		playerA = new Player(0, "Player A", deckA);
		playerB = new Player(1, "Player B", deckB);
		
		playerA.drawCard(5);
		playerB.drawCard(5);
		
		playerTurn = 0;
		playerPhase = 0;
	}
	
	public void update() {
		String msg = getServerInbox();
		if (msg != null && !msg.isEmpty()) {
			
		}
	}
	
	protected abstract String getServerInbox();
	
}
