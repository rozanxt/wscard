package zan.wscard.obj;

import java.util.ArrayList;

import zan.lib.gfx.obj.SpriteObject;
import zan.lib.gfx.shader.DefaultShader;
import zan.lib.gfx.texture.TextureManager;

public class ClockField extends CardField {

	protected ArrayList<CardObject> clockCards;

	protected SpriteObject field;

	public ClockField(double x, double y) {
		super(x, y);
		clockCards = new ArrayList<CardObject>();

		field = new SpriteObject(TextureManager.getTexture("CARDCLOCK"));
	}

	public void destroy() {
		field.destroy();
	}

	public void addCard(CardObject card) {
		card.setCardField(this);
		clockCards.add(card);
	}
	public void removeCard(CardObject card) {clockCards.remove(card);}

	public ArrayList<CardObject> removeCards(int num) {
		ArrayList<CardObject> removed = new ArrayList<CardObject>();
		for (int i=clockCards.size()-num;i<clockCards.size();i++) removed.add(clockCards.get(i));
		clockCards.removeAll(removed);
		return removed;
	}

	public CardObject getCard(int card) {return clockCards.get(card);}

	public int getNumCards(){return clockCards.size();}

	@Override
	public boolean isInBound(double sx, double sy) {
		double sh = 0.5*size;
		double sw = sh*CardObject.cardRatio+60.0;
		if (sx > posX-sw && sx < posX+sw && sy > posY-sh && sy < posY+sh) return true;
		return false;
	}

	@Override
	public void update() {
		for (int i=0;i<clockCards.size();i++) {
			clockCards.get(i).setAnchor(posX-60.0+i*20, posY);
			clockCards.get(i).update();
		}
	}

	@Override
	public void renderCards(DefaultShader sp, double ip) {
		for (int i=0;i<clockCards.size();i++) {
			sp.pushMatrix();
			sp.translate(posX-60.0+i*20, posY, 0.0);
			sp.scale(size, size, 1.0);
			sp.applyModelMatrix();
			sp.popMatrix();

			clockCards.get(i).render(sp, ip);
		}
	}

	@Override
	public void renderField(DefaultShader sp, double ip) {
		if (highlight) sp.setColor(1.0, 0.5, 0.0, 1.0);
		else sp.setColor(1.0, 1.0, 1.0, 1.0);

		sp.pushMatrix();
		sp.translate(posX, posY, 0.0);
		sp.scale(size, size+2.0, 1.0);
		sp.applyModelMatrix();
		field.render(sp);
		sp.popMatrix();
	}

}
