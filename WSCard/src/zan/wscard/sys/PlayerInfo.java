package zan.wscard.sys;

import java.util.ArrayList;

import zan.wscard.card.CardData;

public class PlayerInfo {
	
	protected String name;
	protected CardData[] cards;
	
	public PlayerInfo(String name, ArrayList<CardData> cards) {
		this.name = name;
		this.cards = new CardData[50];
		for (int i=0;i<50;i++) this.cards[i] = cards.get(i);
	}
	
	public String getName() {return name;}
	public CardData getCardData(int card) {return cards[card];}
	
}
