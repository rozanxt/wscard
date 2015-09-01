package zan.wscard.sys;

import java.util.ArrayList;

import zan.wscard.card.CardData;

public class PlayerInfo {
	
	protected String playerName;
	protected CardData[] playerCards;
	
	public PlayerInfo(String name, ArrayList<CardData> cards) {
		playerName = name;
		playerCards = new CardData[50];
		for (int i=0;i<50;i++) playerCards[i] = cards.get(i);
	}
	
	public String getPlayerName() {return playerName;}
	public CardData getCardData(int card) {return playerCards[card];}
	
}
