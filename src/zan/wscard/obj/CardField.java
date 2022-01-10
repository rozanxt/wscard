package zan.wscard.obj;

import zan.lib.gfx.shader.DefaultShader;

public abstract class CardField {

	protected double posX, posY;
	protected double size = CardObject.cardSize;

	protected boolean highlight = false;	// TODO

	public CardField(double x, double y) {
		posX = x;
		posY = y;
	}

	public double getAnchorX() {return posX;}
	public double getAnchorY() {return posY;}

	public boolean isInBound(double sx, double sy) {
		double sh = 0.5*size;
		double sw = sh*CardObject.cardRatio;
		if (sx > posX-sw && sx < posX+sw && sy > posY-sh && sy < posY+sh) return true;
		return false;
	}

	public void highlight(double sx, double sy) {highlight = isInBound(sx, sy);}

	public abstract void update();
	public abstract void renderField(DefaultShader sp, double ip);
	public abstract void renderCards(DefaultShader sp, double ip);

}
