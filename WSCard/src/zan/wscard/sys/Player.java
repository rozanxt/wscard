package zan.wscard.sys;

import java.util.ArrayList;

import zan.wscard.card.CardData;

public abstract class Player {
	
	public static final int NO_CARD = -1;
	
	public static final int CS_NONE = 0;
	public static final int CS_STAND = 1;
	public static final int CS_REST = 2;
	public static final int CS_REVERSE = 3;
	
	protected PlayerInfo playerInfo;
	
	protected ArrayList<Integer> playerDeck;
	protected ArrayList<Integer> playerWaitingRoom;
	protected ArrayList<Integer> playerHand;
	protected int[] playerStage;
	protected int[] playerStageState;
	
	public Player() {
		playerInfo = null;
		playerDeck = new ArrayList<Integer>();
		playerWaitingRoom = new ArrayList<Integer>();
		playerHand = new ArrayList<Integer>();
		playerStage = new int[5];
		for (int i=0;i<5;i++) playerStage[i] = NO_CARD;
		playerStageState = new int[5];
		for (int i=0;i<5;i++) playerStageState[i] = CS_NONE;
	}
	
	public void setInfo(PlayerInfo info) {
		playerInfo = info;
	}
	
	public int getHandCard(int hand) {
		return playerHand.get(hand);
	}
	
	public void addToWaitingRoom(int card) {
		playerWaitingRoom.add(card);
	}
	
	public CardData getCardData(int card) {
		return playerInfo.getCardData(card);
	}
	
}
