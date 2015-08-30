package zan.wscard.obj;

import java.util.ArrayList;

public class HandField extends CardField {
	
	protected ArrayList<CardObject> handCards;
	
	protected double posY;
	
	public HandField() {
		handCards = new ArrayList<CardObject>();
	}
	
	public void setPosY(double posY) {this.posY = posY;}
	
	public void anchorCards() {
		for (int i=0;i<handCards.size();i++) {
			handCards.get(i).setAnchor(-(30.0*(handCards.size()-1))+60.0*i, posY);
		}
	}
	
	public void addCard(CardObject card) {handCards.add(card);}
	public void removeCard(CardObject card) {handCards.remove(card);}
	
	public ArrayList<Integer> getHandCards() {
		ArrayList<Integer> hand = new ArrayList<Integer>();
		for (int i=0;i<handCards.size();i++) hand.add(handCards.get(i).getCardID());
		return hand;
	}
	
}
