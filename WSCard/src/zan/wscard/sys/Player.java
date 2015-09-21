package zan.wscard.sys;

import java.util.ArrayList;
import java.util.Collections;

import zan.wscard.card.CardData;
import static zan.wscard.sys.GameSystem.*;

public class Player {

	private PlayerInfo playerInfo = null;

	private ArrayList<Integer> playerDeck = new ArrayList<Integer>();
	private ArrayList<Integer> playerWaitingRoom = new ArrayList<Integer>();
	private ArrayList<Integer> playerClock = new ArrayList<Integer>();
	private ArrayList<Integer> playerLevel = new ArrayList<Integer>();
	private ArrayList<Integer> playerStock = new ArrayList<Integer>();
	private ArrayList<Integer> playerHand = new ArrayList<Integer>();
	private int[] playerStages = new int[NUM_STAGES];
	private int[] playerCardState = new int[NUM_STAGES];

	public Player() {
		for (int i=0;i<NUM_STAGES;i++) playerStages[i] = CARD_NONE;
		for (int i=0;i<NUM_STAGES;i++) playerCardState[i] = CS_NONE;
	}

	public void setInfo(PlayerInfo info) {
		playerInfo = info;
		initDeck();
	}

	public void initDeck() {
		playerDeck.clear();
		for (int i=0;i<NUM_DECKCARDS;i++) playerDeck.add(i);
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

	public int drawCard() {
		return playerDeck.remove(0);
	}

	public void addToHand(int card) {
		playerHand.add(card);
	}

	public void addToWaitingRoom(int card) {
		playerWaitingRoom.add(card);
	}

	public void addToClock(int card) {
		playerClock.add(card);
	}

	public void addToStock(int card) {
		playerStock.add(card);
	}

	public void addToLevel(int card) {
		playerLevel.add(card);
	}

	public void placeOnStage(int card, int stage) {
		playerStages[stage] = card;
	}

	public void swapOnStage(int stage1, int stage2) {
		int card = playerStages[stage1];
		playerStages[stage1] = playerStages[stage2];
		playerStages[stage2] = card;
	}

	public void setCardState(int stage, int state) {
		playerCardState[stage] = state;
	}

	public void doStandUp() {
		for (int i=0;i<NUM_STAGES;i++) {
			if (playerStages[i] != CARD_NONE && playerCardState[i] == CS_REST) {
				playerCardState[i] = CS_STAND;
			}
		}
	}

	public void doCleanUp() {
		for (int i=0;i<NUM_STAGES;i++) {
			if (playerStages[i] != CARD_NONE && playerCardState[i] == CS_REVERSE) {
				addToWaitingRoom(playerStages[i]);
				playerStages[i] = CARD_NONE;
				playerCardState[i] = CS_NONE;
			}
		}
	}

	public void doLevelUp(int card) {
		for (int i=0;i<7;i++) {
			int removed = playerClock.remove(0);
			if (removed == card) playerLevel.add(removed);
			else playerWaitingRoom.add(removed);
		}
	}

	public void payStock(int cost) {
		for (int i=0;i<cost;i++) {
			int paid = playerStock.remove(playerStock.size()-1);
			addToWaitingRoom(paid);
		}
	}

	public boolean removeFromHand(int card) {
		for (int i=0;i<playerHand.size();i++) {
			if (playerHand.get(i) == card) {
				playerHand.remove(i);
				return true;
			}
		}
		return false;
	}

	public boolean readyForLevelUp() {return (playerClock.size() >= 7);}

	public boolean hasReversedCards() {
		for (int i=0;i<3;i++) if (playerCardState[i] == CS_REVERSE) return true;
		return false;
	}

	public int getHandCard(int hand) {return playerHand.get(hand);}
	public int getStageCard(int stage) {return playerStages[stage];}

	public CardData getCardData(int card) {return playerInfo.getCardData(card);}
	public CardData getHandCardData(int hand) {return getCardData(getHandCard(hand));}
	public CardData getStageCardData(int stage) {return getCardData(getStageCard(stage));}

}
