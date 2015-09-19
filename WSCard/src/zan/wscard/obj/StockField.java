package zan.wscard.obj;

import static org.lwjgl.opengl.GL11.GL_LINE_LOOP;

import java.util.ArrayList;

import zan.lib.gfx.obj.VertexObject;
import zan.lib.gfx.shader.DefaultShader;
import zan.lib.gfx.text.TextManager;

public class StockField extends CardField {

	protected ArrayList<CardObject> stockCards;

	protected VertexObject field;

	public StockField(double x, double y) {
		super(x, y);
		stockCards = new ArrayList<CardObject>();

		final int[] ind = {0, 1, 2, 3};
		final float[] ver = {
			-0.5f*(float)CardObject.cardRatio, -0.5f,
			0.5f*(float)CardObject.cardRatio, -0.5f,
			0.5f*(float)CardObject.cardRatio, 0.5f,
			-0.5f*(float)CardObject.cardRatio, 0.5f,
		};
		field = new VertexObject(ver, ind, 2, 0, 0, 0, GL_LINE_LOOP);
	}

	public void destroy() {
		field.destroy();
	}

	public void addCard(CardObject card) {
		card.setCardField(this);
		stockCards.add(card);
	}
	public void removeCard(CardObject card) {stockCards.remove(card);}

	public CardObject getCard(int card) {return stockCards.get(card);}

	public int getNumCards() {return stockCards.size();}

	@Override
	public boolean isInBound(double sx, double sy) {
		double sh = 0.5*size;
		double sw = sh*CardObject.cardRatio;
		if (sx > posX-sw && sx < posX+sw && sy > posY-sh && sy < posY+sh) return true;
		return false;
	}

	@Override
	public void update() {
		for (int i=0;i<stockCards.size();i++) {
			stockCards.get(i).setAnchor(posX, posY);
			if (stockCards.get(i).isInAnchor()) stockCards.get(i).hide = true;
			stockCards.get(i).update();
		}
	}

	@Override
	public void renderCards(DefaultShader sp, double ip) {
		for (int i=0;i<stockCards.size();i++) stockCards.get(i).render(sp, ip);
	}

	@Override
	public void renderField(DefaultShader sp, double ip) {
		if (highlight) sp.setColor(0.0, 0.0, 1.0, 1.0);
		else sp.setColor(1.0, 1.0, 1.0, 1.0);

		sp.pushMatrix();
		sp.translate(posX, posY, 0.0);
		sp.scale(size, size, 1.0);
		sp.applyModelMatrix();
		field.render(sp);
		sp.popMatrix();

		sp.pushMatrix();
		sp.translate(posX-0.05*size, posY-0.7*size, 0.0);
		sp.scale(0.15*size, 0.15*size, 1.0);
		sp.applyModelMatrix();
		TextManager.renderText(sp, String.valueOf(stockCards.size()), "defont");
		sp.popMatrix();
	}

}
