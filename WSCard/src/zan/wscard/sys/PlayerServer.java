package zan.wscard.sys;

import java.util.ArrayList;
import java.util.Collections;

public class PlayerServer extends Player {

	public PlayerServer() {
		super();
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
		// TODO
	}

	public ArrayList<Integer> drawCards(int num) {
		ArrayList<Integer> drawnCards = new ArrayList<Integer>();
		for (int i=0;i<num;i++) {
			if (playerDeck.isEmpty()) reshuffleDeck();
			int drawn = playerDeck.remove(0);
			drawnCards.add(drawn);
			playerHand.add(drawn);
		}
		return drawnCards;
	}

	public void discardCard(int card) {
		for (int i=0;i<playerHand.size();i++) if (playerHand.get(i) == card) playerHand.remove(i);
		playerWaitingRoom.add(card);
	}

	public void placeCard(int card, int stage) {
		for (int i=0;i<playerHand.size();i++) if (playerHand.get(i) == card) playerHand.remove(i);
		playerStage[stage] = card;
	}

	public void moveCard(int stage1, int stage2) {
		int temp = playerStage[stage1];
		playerStage[stage1] = playerStage[stage2];
		playerStage[stage2] = temp;
	}

	public void clockCard(int card) {
		for (int i=0;i<playerHand.size();i++) if (playerHand.get(i) == card) playerHand.remove(i);
		playerClock.add(card);
	}

	public int triggerCard() {
		if (playerDeck.isEmpty()) reshuffleDeck();
		int trigger = playerDeck.remove(0);
		playerStock.add(trigger);
		return trigger;
	}

	public ArrayList<Integer> damageCards(int num) {
		ArrayList<Integer> damagedCards = new ArrayList<Integer>();
		for (int i=0;i<num;i++) {
			if (playerDeck.isEmpty()) reshuffleDeck();
			int drawn = playerDeck.remove(0);
			damagedCards.add(drawn);
			playerClock.add(drawn);
		}
		return damagedCards;
	}

}
