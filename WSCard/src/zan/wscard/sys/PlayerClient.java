package zan.wscard.sys;

import java.util.ArrayList;

import zan.wscard.card.CardData;

public class PlayerClient extends Player {
	
	protected ArrayList<Integer> cardsToDraw;
	
	public PlayerClient() {
		super();
		cardsToDraw = new ArrayList<Integer>();
	}
	
	public void drawCards(ArrayList<Integer> drawn) {
		cardsToDraw.addAll(drawn);
	}
	
	public int getDrawnCard() {
		if (cardsToDraw.isEmpty()) return -1;
		return cardsToDraw.remove(0);
	}
	
	public CardData getCardData(int card) {
		return info.getCardData(card);
	}
	
}
