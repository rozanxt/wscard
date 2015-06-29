package zan.wscard.mechanics;

import java.util.ArrayList;

import zan.wscard.card.CardData;

public class Player {
	
	private int playerID;
	private String playerName;
	
	private ArrayList<CardData> playerDeck;
	private ArrayList<CardData> playerWaitingRoom;
	private ArrayList<CardData> playerClock;
	private ArrayList<CardData> playerStock;
	private ArrayList<CardData> playerLevel;
	private ArrayList<CardData> playerCards;
	
	public Player(int id, String name, ArrayList<CardData> deck) {
		playerID = id;
		playerName = name;
		playerDeck = deck;
		playerWaitingRoom = new ArrayList<CardData>();
		playerClock = new ArrayList<CardData>();
		playerStock = new ArrayList<CardData>();
		playerLevel = new ArrayList<CardData>();
		playerCards = new ArrayList<CardData>();
	}
	
	public int getPlayerID() {return playerID;}
	public String getPlayerName() {return playerName;}
	
	public int drawCard(int num) {
		for (int i=0;i<num;i++) {
			if (playerDeck.isEmpty()) return num-i;
			playerCards.add(playerDeck.remove(0));
		}
		return 0;
	}
	
	public int getNumPlayerCards() {return playerCards.size();}
	
	public CardData getPlayerCard(int index) {return playerCards.get(index);}
	
}
