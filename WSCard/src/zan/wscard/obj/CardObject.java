package zan.wscard.obj;

import zan.lib.gfx.obj.SpriteObject;
import zan.lib.gfx.sprite.Sprite;
import zan.wscard.card.CardData;

public class CardObject extends Sprite {
	
	private CardData cardData;
	
	public CardObject(CardData data, SpriteObject sprite) {
		super(sprite);
		cardData = data;
	}
	
	// TODO
	public boolean isInShape(double sx, double sy) {
		if (sx > posX-0.5*scaleX*(500.0/730.0) && sx < posX+0.5*scaleX*(500.0/730.0) && sy > posY-0.5*scaleY && sy < posY+0.5*scaleY) return true;
		return false;
	}
	
	public CardData getCardData() {
		return cardData;
	}
	
}
