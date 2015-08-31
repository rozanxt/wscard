package zan.wscard.obj;

import zan.lib.gfx.ShaderProgram;
import zan.lib.gfx.texture.TextureInfo;
import zan.lib.gfx.texture.TextureManager;
import zan.lib.util.Utility;
import zan.wscard.card.CardData;
import zan.wscard.gfx.CardSprite;
import zan.wscard.sys.Player;

public class CardObject {
	
	protected int cardID;
	protected int cardState;
	
	protected CardData cardData;
	protected CardField cardField;
	
	protected CardSprite cardSprite;
	
	protected double posX, posY;
	protected double size;
	
	protected boolean anchor;
	protected double anchorX, anchorY;
	
	protected boolean hide;
	
	public CardObject(int id, CardData data, TextureInfo texture) {
		cardID = id;
		cardState = Player.CS_NONE;
		cardData = data;
		cardField = null;
		cardSprite = new CardSprite(texture, TextureManager.getTexture("CARDBACK"));
		posX = 0.0;
		posY = 0.0;
		size = 1.0;
		anchor = false;
		anchorX = 0.0;
		anchorY = 0.0;
		hide = false;
	}
	
	public void destroy() {
		cardSprite.destroy();
	}
	
	public void setCardState(int state) {cardState = state;}
	
	public int getCardID() {return cardID;}
	public int getCardState() {return cardState;}
	
	public void setField(CardField cardField) {
		this.cardField = cardField;
		if (cardField != null && cardField instanceof StageField) {
			StageField sf = (StageField)cardField;
			setAnchor(sf.getPosX(), sf.getPosY());
		}
	}
	public CardField getField() {return cardField;}
	public CardData getCardData() {return cardData;}
	
	public void setPos(double posX, double posY) {
		this.posX = posX;
		this.posY = posY;
	}
	public void setSize(double size) {this.size = size;}
	public void toggleAnchor(boolean anchor) {this.anchor = anchor;}
	public void setAnchor(double anchorX, double anchorY) {
		this.anchorX = anchorX;
		this.anchorY = anchorY;
	}
	
	public double getAnchorX() {return anchorX;}
	public double getAnchorY() {return anchorY;}
	
	public boolean isInAnchor() {
		double dx = anchorX-posX;
		double dy = anchorY-posY;
		double dist2 = dx*dx+dy*dy;
		if (dist2 < 10.0) return true;
		return false;
	}
	
	// TODO
	public boolean isInBound(double sx, double sy) {
		if (sx > posX-0.5*size*(500.0/730.0) && sx < posX+0.5*size*(500.0/730.0) && sy > posY-0.5*size && sy < posY+0.5*size) return true;
		return false;
	}
	
	public void update() {
		if (anchor) setPos(Utility.interpolateLinear(posX, anchorX, 0.2), Utility.interpolateLinear(posY, anchorY, 0.2));
		cardSprite.setPos(posX, posY);
		cardSprite.setScale(size);
		cardSprite.hide((cardID == -1));
		cardSprite.update();
	}
	
	public void render(ShaderProgram sp, double ip) {
		cardSprite.render(sp, ip);
	}
	
}
