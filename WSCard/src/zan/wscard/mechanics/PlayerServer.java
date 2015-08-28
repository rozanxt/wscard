package zan.wscard.mechanics;

import java.util.ArrayList;
import java.util.Collections;

import zan.wscard.card.CardData;

public class PlayerServer extends Player {
	
	public PlayerServer(int id, String name, ArrayList<CardData> deck) {
		super();
		this.id = id;
		this.name = name;
		for (int i=0;i<50;i++) playerCards[i] = deck.get(i);
	}
	
	public void init() {
		for (int i=0;i<50;i++) playerDeck.add(i);
		shuffleDeck();
	}
	
	public void shuffleDeck() {
		Collections.shuffle(playerDeck);
	}
	
	public ArrayList<Integer> drawCard(int num) {
		ArrayList<Integer> drawnCards = new ArrayList<Integer>();
		for (int i=0;i<num;i++) {
			if (playerDeck.isEmpty()) {
				// TODO Reshuffle mechanism
			}
			int drawn = playerDeck.remove(0);
			drawnCards.add(drawn);
			playerHand.add(drawn);
		}
		return drawnCards;
	}
	
}
