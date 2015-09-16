package zan.wscard.sys;

import java.util.ArrayList;
import java.util.Collections;

import zan.wscard.card.CardData;

public class Player {

	public static final int NO_CARD = -1;

	private PlayerInfo playerInfo = null;

	private ArrayList<Integer> playerDeck = new ArrayList<Integer>();
	private ArrayList<Integer> playerWaitingRoom = new ArrayList<Integer>();
	private ArrayList<Integer> playerClock = new ArrayList<Integer>();
	private ArrayList<Integer> playerLevel = new ArrayList<Integer>();
	private ArrayList<Integer> playerStock = new ArrayList<Integer>();
	private ArrayList<Integer> playerHand = new ArrayList<Integer>();
	private int[] playerStages = new int[5];

	public Player() {
		for (int i=0;i<5;i++) playerStages[i] = NO_CARD;
	}

	public void setInfo(PlayerInfo info) {
		playerInfo = info;
		initDeck();
	}

	public void initDeck() {
		playerDeck.clear();
		for (int i=0;i<50;i++) playerDeck.add(i);
		shuffleDeck();
	}

	public void shuffleDeck() {
		Collections.shuffle(playerDeck);
	}

	public void reshuffleDeck() {
		for (int i=0;i<playerWaitingRoom.size();i++) playerDeck.add(playerWaitingRoom.get(i));
		playerWaitingRoom.clear();
		shuffleDeck();
	}

	public int reshuffleCost() {
		int cost = playerDeck.remove(0);
		playerClock.add(cost);
		return cost;
	}

	public int takeCard() {
		return playerDeck.remove(0);
	}

	public int drawCard() {
		int drawn = playerDeck.remove(0);
		playerHand.add(drawn);
		return drawn;
	}

	public boolean discardCard(int card) {
		for (int i=0;i<playerHand.size();i++) {
			if (playerHand.get(i) == card) {
				playerHand.remove(i);
				playerWaitingRoom.add(card);
				return true;
			}
		}
		return false;
	}

	public boolean placeCard(int card, int stage) {
		for (int i=0;i<playerHand.size();i++) {
			if (playerHand.get(i) == card) {
				playerHand.remove(i);
				playerStages[stage] = card;
				return true;
			}
		}
		return false;
	}

	public void swapCard(int stage1, int stage2) {
		int temp = playerStages[stage1];
		playerStages[stage1] = playerStages[stage2];
		playerStages[stage2] = temp;
	}

	public int clockCard(int card) {
		for (int i=0;i<playerHand.size();i++) {
			if (playerHand.get(i) == card) {
				playerHand.remove(i);
				playerClock.add(card);
				return playerClock.size();
			}
		}
		return NO_CARD;
	}

	public int triggerCard() {
		int trigger = playerDeck.remove(0);
		playerStock.add(trigger);
		return trigger;
	}

	public int damageCard(int card) {
		playerClock.add(card);
		return playerClock.size();
	}

	public void levelUp(int card) {
		for (int i=0;i<7;i++) {
			if (playerClock.get(i) == card) {
				playerLevel.add(card);
			} else {
				playerWaitingRoom.add(playerClock.get(i));
			}
		}
		for (int i=0;i<7;i++) playerClock.remove(0);
	}

	public boolean isDeckEmpty() {return playerDeck.isEmpty();}

	public int getLevel() {return playerLevel.size();}

	public int getHandCard(int hand) {return playerHand.get(hand);}
	public int getStageCard(int stage) {return playerStages[stage];}

	public CardData getCardData(int card) {return playerInfo.getCardData(card);}
	public CardData getHandCardData(int hand) {return getCardData(getHandCard(hand));}
	public CardData getStageCardData(int stage) {return getCardData(getStageCard(stage));}

}
