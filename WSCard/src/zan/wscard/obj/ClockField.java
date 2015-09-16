package zan.wscard.obj;

import static org.lwjgl.opengl.GL11.GL_LINE_LOOP;

import java.util.ArrayList;

import zan.lib.gfx.obj.VertexObject;
import zan.lib.gfx.shader.DefaultShader;

public class ClockField extends CardField {

	protected ArrayList<CardObject> clockCards;

	protected VertexObject field;

	public ClockField(double x, double y) {
		super(x, y);
		clockCards = new ArrayList<CardObject>();

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
		clockCards.add(card);
	}

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

		for (int i=0;i<7;i++) {
			sp.pushMatrix();
			sp.translate(posX-60.0+i*20, posY, 0.0);
			sp.scale(size, size, 1.0);
			sp.applyModelMatrix();
			sp.popMatrix();

			field.render(sp);
		}
	}

}
