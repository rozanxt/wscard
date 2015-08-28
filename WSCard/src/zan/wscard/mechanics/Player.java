package zan.wscard.mechanics;

import java.util.ArrayList;

import zan.wscard.card.CardData;

public abstract class Player {
	
	protected int id;
	protected String name;
	
	protected CardData[] playerCards;
	protected ArrayList<Integer> playerDeck;
	protected ArrayList<Integer> playerHand;
	protected int[] playerStage;
	protected int[] playerStageState;
	
	public Player() {
		playerCards = new CardData[50];
		playerDeck = new ArrayList<Integer>();
		playerHand = new ArrayList<Integer>();
		playerStage = new int[5];
		playerStageState = new int[5];
		for (int i=0;i<5;i++) {
			playerStage[i] = -1;
			playerStageState[i] = -1;
		}
	}
	
	public void doStandUp() {
		for (int i=0;i<5;i++) playerStageState[i] = 0;
	}
	
	public CardData getCardData(int card) {return playerCards[card];}
	
	public ArrayList<Integer> getPlayerHand() {return playerHand;}
	public int[] getPlayerStage() {return playerStage;}
	public int[] getPlayerStageState() {return playerStageState;}
	
	public int getPlayerID() {return id;}
	public String getPlayerName() {return name;}
	
}
