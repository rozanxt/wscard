package zan.wscard.obj;

import static org.lwjgl.opengl.GL11.GL_LINE_LOOP;
import zan.lib.gfx.shader.DefaultShader;
import zan.lib.gfx.obj.VertexObject;
import zan.lib.gfx.texture.TextureManager;
import zan.wscard.gfx.CardSprite;

public class DeckField extends CardField {

	protected CardSprite deckSprite;

	protected VertexObject vObj;

	public DeckField(double x, double y) {
		super(x, y);
		deckSprite = new CardSprite(TextureManager.getTexture("CARDBACK"), TextureManager.getTexture("CARDBACK"));

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

	@Override
	public void update() {
		deckSprite.setPos(posX, posY);
		deckSprite.setScale(size);
		deckSprite.update();
	}

	@Override
	public void renderField(DefaultShader sp, double ip) {
		sp.pushMatrix();
		sp.translate(posX, posY, 0.0);
		sp.scale(size, size, 1.0);
		sp.applyModelMatrix();
		sp.popMatrix();

		if (highlight) sp.setColor(0.0, 1.0, 0.0, 1.0);
		else sp.setColor(1.0, 1.0, 1.0, 1.0);

		vObj.render(sp);

		deckSprite.render(sp, ip);
	}

	@Override
	public void renderCards(DefaultShader sp, double ip) {}

}
