package zan.wscard.sys;

import java.util.ArrayList;

import zan.wscard.card.CardData;

public abstract class Player {
	
	public static final int NO_CARD = -1;
	
	public static final int SS_NONE = 0;
	public static final int SS_STAND = 1;
	public static final int SS_REST = 2;
	public static final int SS_REVERSE = 3;
	
	protected PlayerInfo info;
	
	protected ArrayList<Integer> deck;
	protected ArrayList<Integer> hand;
	protected int[] stage;
	protected int[] stageState;
	
	public Player() {
		info = null;
		deck = new ArrayList<Integer>();
		hand = new ArrayList<Integer>();
		stage = new int[5];
		for (int i=0;i<5;i++) stage[i] = NO_CARD;
		stageState = new int[5];
		for (int i=0;i<5;i++) stageState[i] = SS_NONE;
	}
	
	public void setInfo(PlayerInfo info) {
		this.info = info;
	}
	
	public CardData getCardData(int card) {
		return info.getCardData(card);
	}
	
}
