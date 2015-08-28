package zan.wscard.obj;

import java.util.ArrayList;

public class HandField extends CardField {
	
	public ArrayList<CardObject> handCards;
	
	public HandField() {
		handCards = new ArrayList<CardObject>();
	}
	
	public void anchorCards() {
		for (int i=0;i<handCards.size();i++) {
			handCards.get(i).setAnchor(-(30.0*(handCards.size()-1))+60.0*i, -240.0);
		}
	}
	
	public void addCard(CardObject card) {handCards.add(card);}
	public void removeCard(CardObject card) {handCards.remove(card);}
	
}
