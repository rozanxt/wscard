package zan.wscard.obj;

import static org.lwjgl.opengl.GL11.GL_LINE_LOOP;

import java.util.ArrayList;

import zan.lib.gfx.ShaderProgram;
import zan.lib.gfx.obj.VertexObject;

public class WaitingRoomField extends CardField {
	
	protected ArrayList<CardObject> waitingRoomCards;
	
	protected VertexObject vObj;
	
	public WaitingRoomField() {
		waitingRoomCards = new ArrayList<CardObject>();
		
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
	
	public void addCard(CardObject card) {waitingRoomCards.add(card);}
	public void removeCard(CardObject card) {waitingRoomCards.remove(card);}
	
	public CardObject getCard(int card) {return waitingRoomCards.get(card);}
	
	@Override
	public void update() {
		for (int i=0;i<waitingRoomCards.size();i++) waitingRoomCards.get(i).update();
	}
	
	@Override
	public void renderField(ShaderProgram sp, double ip) {
		sp.enableTexture(false);
		
		sp.pushMatrix();
		sp.translate(posX, posY, 0.0);
		sp.scale(size, size, 1.0);
		sp.applyModelView();
		sp.popMatrix();
		
		if (highlight) sp.setColor(1.0, 0.0, 0.0, 1.0);
		else sp.setColor(1.0, 1.0, 1.0, 1.0);
		
		vObj.render(sp);
		
		sp.enableTexture(true);
	}
	
	@Override
	public void renderCards(ShaderProgram sp, double ip) {
		for (int i=0;i<waitingRoomCards.size();i++) if (!waitingRoomCards.get(i).isHeld()) waitingRoomCards.get(i).render(sp, ip);
	}
	
}
