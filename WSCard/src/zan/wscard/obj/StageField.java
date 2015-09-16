package zan.wscard.obj;

import static org.lwjgl.opengl.GL11.GL_LINE_LOOP;
import zan.lib.gfx.shader.DefaultShader;
import zan.lib.gfx.obj.VertexObject;

public class StageField extends CardField {

	protected int stageID;

	protected CardObject cardObj;

	protected VertexObject vObj;

	public StageField(int sid, double x, double y) {
		super(x, y);
		stageID = sid;

		cardObj = null;

		final int[] ind = {0, 1, 2, 3};
		final float[] ver = {
			-0.5f*(float)CardObject.cardRatio, -0.5f,
			0.5f*(float)CardObject.cardRatio, -0.5f,
			0.5f*(float)CardObject.cardRatio, 0.5f,
			-0.5f*(float)CardObject.cardRatio, 0.5f,
		};
		vObj = new VertexObject(ver, ind, 2, 0, 0, 0, GL_LINE_LOOP);
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
		sp.scale(size, size, 1.0);
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
