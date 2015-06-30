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
	
	// TODO TEMPORARY METHOD
	public boolean isInShape(double sx, double sy) {
		if (sx > posX-100.0*(500.0/730.0) && sx < posX+100.0*(500.0/730.0) && sy > 350.0 && sy < 550.0) return true;
		return false;
	}
	
}
