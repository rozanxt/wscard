package zan.wscard.obj;

import zan.lib.gfx.ShaderProgram;

public abstract class CardField {
	
	protected double posX, posY;
	protected double size;
	
	protected boolean highlight;	// TODO
	
	public CardField() {
		posX = 0.0;
		posY = 0.0;
		size = CardObject.cardSize;
	}
	
	public void setPos(double sx, double sy) {posX = sx; posY = sy;}
	public void setSize(double size) {this.size = size;}
	
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
	public abstract void renderField(ShaderProgram sp, double ip);
	public abstract void renderCards(ShaderProgram sp, double ip);
	
}
