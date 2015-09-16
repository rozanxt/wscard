package zan.wscard.obj;

import java.util.ArrayList;

import zan.lib.gfx.shader.DefaultShader;

public class HandField extends CardField {

	protected ArrayList<CardObject> handCards;

	public HandField(double x, double y) {
		super(x, y);
		handCards = new ArrayList<CardObject>();
	}

	public void addCard(CardObject card) {
		card.setCardField(this);
		handCards.add(card);
	}
	public void removeCard(CardObject card) {handCards.remove(card);}

	public CardObject getCard(int card) {return handCards.get(card);}

	public int getNumCards(){return handCards.size();}

	@Override
	public void update() {
		for (int i=0;i<handCards.size();i++) {
			handCards.get(i).setAnchor(-(30.0*(handCards.size()-1))+60.0*i, posY);
			handCards.get(i).update();
		}
	}

	@Override
	public void renderField(DefaultShader sp, double ip) {}

	@Override
	public void renderCards(DefaultShader sp, double ip) {
		for (int i=0;i<handCards.size();i++) if (!handCards.get(i).isHeld()) handCards.get(i).render(sp, ip);
	}

}
