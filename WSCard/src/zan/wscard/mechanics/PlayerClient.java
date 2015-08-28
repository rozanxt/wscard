package zan.wscard.mechanics;

import java.util.ArrayList;

import zan.wscard.card.CardData;

public class PlayerClient extends Player {
	
	public PlayerClient(int id, String name, ArrayList<CardData> deck) {
		super();
		this.id = id;
		this.name = name;
		for (int i=0;i<50;i++) playerCards[i] = deck.get(i);
	}
	
	public void syncHand(ArrayList<Integer> cards) {
		playerHand.clear();
		playerHand.addAll(cards);
	}
	
}
