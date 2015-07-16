package zan.wscard.obj;

import zan.lib.gfx.obj.SpriteObject;
import zan.lib.gfx.sprite.Sprite;
import zan.lib.util.Utility;
import zan.wscard.card.CardData;

public class CardObject extends Sprite {
	
	protected boolean anchor;
	protected double anchorX, anchorY;
	
	protected CardData cardData;
	
	public CardObject(CardData data, SpriteObject sprite) {
		super(sprite);
		cardData = data;
		anchor = false;
		anchorX = 0.0;
		anchorY = 0.0;
	}
	
	public void toggleAnchor(boolean anchor) {this.anchor = anchor;}
	public void setAnchor(double anchorX, double anchorY) {
		this.anchorX = anchorX;
		this.anchorY = anchorY;
	}
	
	// TODO
	public double getAnchorX() {return anchorX;}
	public double getAnchorY() {return anchorY;}
	
	@Override
	public void update() {
		if (anchor) setPos(Utility.interpolateLinear(posX, anchorX, 0.2), Utility.interpolateLinear(posY, anchorY, 0.2));
		super.update();
	}
	
	// TODO
	public boolean isInBound(double sx, double sy) {
		if (sx > posX-0.5*scaleX*(500.0/730.0) && sx < posX+0.5*scaleX*(500.0/730.0) && sy > posY-0.5*scaleY && sy < posY+0.5*scaleY) return true;
		return false;
	}
	
	public CardData getCardData() {
		return cardData;
	}
	
}
