package zan.wscard.mechanics;

import java.util.ArrayList;

import zan.wscard.card.CardData;

public class PlayerClient extends Player {
	
	private ArrayList<Integer> cardsToDraw;
	
	public PlayerClient(int id, String name, ArrayList<CardData> deck) {
		super();
		this.id = id;
		this.name = name;
		for (int i=0;i<50;i++) playerCards[i] = deck.get(i);
		cardsToDraw = new ArrayList<Integer>();
	}
	
	public void drawCard(ArrayList<Integer> cards) {
		cardsToDraw.addAll(cards);
	}
	
	public int getDrawnCard() {
		if (cardsToDraw.isEmpty()) return -1;
		return cardsToDraw.remove(0);
	}
	
}
