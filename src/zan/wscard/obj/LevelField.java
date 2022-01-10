package zan.wscard.obj;

import java.util.ArrayList;

import zan.lib.gfx.obj.SpriteObject;
import zan.lib.gfx.shader.DefaultShader;
import zan.lib.gfx.texture.TextureManager;

public class LevelField extends CardField {

	protected ArrayList<CardObject> levelCards;

	protected SpriteObject field;

	public LevelField(double x, double y) {
		super(x, y);
		levelCards = new ArrayList<CardObject>();

		field = new SpriteObject(TextureManager.getTexture("CARDFIELD"));
	}

	public void destroy() {
		field.destroy();
	}

	public void addCard(CardObject card) {
		card.setCardField(this);
		levelCards.add(card);
	}
	public void removeCard(CardObject card) {levelCards.remove(card);}

	public CardObject getCard(int card) {return levelCards.get(card);}

	public int getNumCards() {return levelCards.size();}

	@Override
	public boolean isInBound(double sx, double sy) {
		double sh = 0.5*size;
		double sw = sh*CardObject.cardRatio;
		if (sx > posX-sw && sx < posX+sw && sy > posY-sh && sy < posY+sh) return true;
		return false;
	}

	@Override
	public void update() {
		for (int i=0;i<levelCards.size();i++) {
			levelCards.get(i).setAnchor(posX+5.0*i, posY+5.0*i);
			levelCards.get(i).update();
		}
	}

	@Override
	public void renderCards(DefaultShader sp, double ip) {
		for (int i=0;i<levelCards.size();i++) {
			sp.pushMatrix();
			sp.translate(posX+5.0*i, posY+5.0*i, 0.0);
			sp.scale(size, size, 1.0);
			sp.applyModelMatrix();
			sp.popMatrix();

			levelCards.get(i).render(sp, ip);
		}
	}

	@Override
	public void renderField(DefaultShader sp, double ip) {
		if (highlight) sp.setColor(1.0, 0.0, 1.0, 1.0);
		else sp.setColor(1.0, 1.0, 1.0, 1.0);

		sp.pushMatrix();
		sp.translate(posX, posY, 0.0);
		sp.scale(size+2.0, size+2.0, 1.0);
		sp.applyModelMatrix();
		sp.popMatrix();

		field.render(sp);
	}

}
