package zan.wscard.obj;

import zan.lib.gfx.shader.DefaultShader;
import zan.lib.gfx.texture.TextureManager;
import zan.lib.util.Utility;
import zan.wscard.card.CardData;
import zan.wscard.gfx.CardSprite;

public class CardObject {

	public static final double cardSize = 80.0;
	public static final double cardRatio = (500.0/730.0);

	protected int cardID;
	protected int cardState;

	protected CardData cardData;
	protected CardField cardField;

	protected CardSprite cardSprite;
	protected double posX, posY;
	protected double size;
	protected double anchorX, anchorY;
	protected boolean anchor;
	protected boolean held;
	protected boolean hide;

	public CardObject(int id, CardData data) {
		cardID = id;
		cardState = 0;	// TODO
		cardData = data;
		cardField = null;
		if (cardData == null) cardSprite = new CardSprite(TextureManager.getTexture("CARDBACK"), TextureManager.getTexture("CARDBACK"));
		else cardSprite = new CardSprite(TextureManager.getTexture(cardData.id), TextureManager.getTexture("CARDBACK"));
		posX = 0.0;
		posY = 0.0;
		size = cardSize;
		anchorX = 0.0;
		anchorY = 0.0;
		anchor = true;
		held = false;
		hide = false;
	}
	public CardObject() {
		this(-1, null);
		hide = true;
	}

	public void destroy() {
		cardSprite.destroy();
	}

	public void setCardState(int state) {cardState = state;}
	public void setCardField(CardField field) {
		cardField = field;
		if (cardField != null) setAnchor(cardField.getAnchorX(), cardField.getAnchorY());
	}

	public int getCardID() {return cardID;}
	public int getCardState() {return cardState;}
	public CardData getCardData() {return cardData;}
	public CardField getCardField() {return cardField;}

	public void setPos(double sx, double sy) {posX = sx; posY = sy;}
	public void setSize(double size) {this.size = size;}
	public void setAnchor(double sx, double sy) {anchorX = sx; anchorY = sy;}
	public void toggleAnchor(boolean anchor) {this.anchor = anchor;}
	public void toggleHeld(boolean held) {this.held = held;}
	public void toggleHide(boolean hide) {this.hide = hide;}

	public double getAnchorX() {return anchorX;}
	public double getAnchorY() {return anchorY;}
	public boolean isInAnchor() {
		double dx = anchorX-posX;
		double dy = anchorY-posY;
		double dist2 = dx*dx+dy*dy;
		if (dist2 < 10.0) return true;
		return false;
	}
	public boolean isInBound(double sx, double sy) {
		double sh = 0.5*size;
		double sw = sh*cardRatio;
		if (sx > posX-sw && sx < posX+sw && sy > posY-sh && sy < posY+sh) return true;
		return false;
	}
	public boolean isHeld() {return held;}
	public boolean isHidden() {return hide;}

	public void update() {
		if (anchor) setPos(Utility.interpolateLinear(posX, anchorX, 0.2), Utility.interpolateLinear(posY, anchorY, 0.2));
		cardSprite.setPos(posX, posY);
		cardSprite.setScale(size);
		cardSprite.hide(hide);
		cardSprite.update();
	}

	public void render(DefaultShader sp, double ip) {
		cardSprite.render(sp, ip);
	}

}
