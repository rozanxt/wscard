package zan.wscard.obj;

import zan.lib.gfx.shader.DefaultShader;
import zan.lib.gfx.texture.TextureManager;
import zan.lib.gfx.obj.SpriteObject;

public class StageField extends CardField {

	protected int stageID;

	protected CardObject cardObj;

	protected SpriteObject vObj;

	public StageField(int sid, double x, double y) {
		super(x, y);
		stageID = sid;

		cardObj = null;

		vObj = new SpriteObject(TextureManager.getTexture("CARDFIELD"));
	}

	public void destroy() {
		vObj.destroy();
	}

	public void setCard(CardObject card) {
		cardObj = card;
		if (cardObj != null) cardObj.setCardField(this);
	}
	public CardObject getCard() {return cardObj;}

	public int getStageID() {return stageID;}
	public boolean hasCard() {return (cardObj != null);}

	@Override
	public void update() {
		if (cardObj != null) cardObj.update();
	}

	@Override
	public void renderField(DefaultShader sp, double ip) {
		sp.pushMatrix();
		sp.translate(posX, posY, 0.0);
		sp.scale(size+2.0, size+2.0, 1.0);
		sp.applyModelMatrix();
		sp.popMatrix();

		if (highlight) sp.setColor(1.0, 1.0, 0.0, 1.0);
		else sp.setColor(1.0, 1.0, 1.0, 1.0);

		vObj.render(sp);
	}

	@Override
	public void renderCards(DefaultShader sp, double ip) {
		if (cardObj != null) if (!cardObj.isHeld()) cardObj.render(sp, ip);
	}

}
