package zan.wscard.sys;

import java.util.ArrayList;
import java.util.Collections;

public class PlayerServer extends Player {
	
	public PlayerServer() {
		super();
		initDeck();
	}
	
	public void initDeck() {
		deck.clear();
		for (int i=0;i<50;i++) deck.add(i);
		shuffleDeck();
	}
	
	public void shuffleDeck() {
		Collections.shuffle(deck);
	}
	
	public void reshuffleDeck() {
		// TODO
	}
	
	public ArrayList<Integer> drawCards(int num) {
		ArrayList<Integer> drawnCards = new ArrayList<Integer>();
		for (int i=0;i<num;i++) {
			if (deck.isEmpty()) reshuffleDeck();
			int drawn = deck.remove(0);
			drawnCards.add(drawn);
			hand.add(drawn);
		}
		return drawnCards;
	}
	
}
