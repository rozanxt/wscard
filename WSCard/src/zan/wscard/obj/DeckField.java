package zan.wscard.obj;

import static org.lwjgl.opengl.GL11.GL_LINE_LOOP;
import zan.lib.gfx.ShaderProgram;
import zan.lib.gfx.obj.VertexObject;
import zan.lib.gfx.texture.TextureManager;
import zan.wscard.gfx.CardSprite;

public class DeckField extends CardField {
	
	protected CardSprite deckSprite;
	
	protected VertexObject vObj;
	
	public DeckField() {
		deckSprite = new CardSprite(TextureManager.getTexture("CARDBACK"), TextureManager.getTexture("CARDBACK"));
		
		final int[] ind = {0, 1, 2, 3};
		final float[] ver = {
			-0.5f*(float)CardObject.cardRatio, -0.5f,
			0.5f*(float)CardObject.cardRatio, -0.5f,
			0.5f*(float)CardObject.cardRatio, 0.5f,
			-0.5f*(float)CardObject.cardRatio, 0.5f,
		};
		vObj = new VertexObject(ver, ind);
		vObj.setNumCoords(2);
		vObj.setDrawMode(GL_LINE_LOOP);
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
	public void renderField(ShaderProgram sp, double ip) {
		sp.enableTexture(false);
		
		sp.pushMatrix();
		sp.translate(posX, posY, 0.0);
		sp.scale(size, size, 1.0);
		sp.applyModelView();
		sp.popMatrix();
		
		if (highlight) sp.setColor(0.0, 1.0, 0.0, 1.0);
		else sp.setColor(1.0, 1.0, 1.0, 1.0);
		
		vObj.render(sp);
		
		sp.enableTexture(true);
		
		deckSprite.render(sp, ip);
	}
	
	@Override
	public void renderCards(ShaderProgram sp, double ip) {}
	
}
