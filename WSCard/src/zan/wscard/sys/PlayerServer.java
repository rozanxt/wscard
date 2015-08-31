package zan.wscard.sys;

import java.util.ArrayList;
import java.util.Collections;

public class PlayerServer extends Player {
	
	public PlayerServer() {
		super();
		initDeck();
	}
	
	public void initDeck() {
		playerDeck.clear();
		for (int i=0;i<50;i++) playerDeck.add(i);
		shuffleDeck();
	}
	
	public void shuffleDeck() {
		Collections.shuffle(playerDeck);
	}
	
	public void reshuffleDeck() {
		// TODO
	}
	
	public ArrayList<Integer> drawCards(int num) {
		ArrayList<Integer> drawnCards = new ArrayList<Integer>();
		for (int i=0;i<num;i++) {
			if (playerDeck.isEmpty()) reshuffleDeck();
			int drawn = playerDeck.remove(0);
			drawnCards.add(drawn);
			playerHand.add(drawn);
		}
		return drawnCards;
	}
	
}
