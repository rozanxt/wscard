package zan.wscard.obj;

import java.util.ArrayList;

import zan.lib.gfx.shader.DefaultShader;
import zan.lib.gfx.texture.TextureManager;
import zan.lib.gfx.obj.SpriteObject;

public class WaitingRoomField extends CardField {

	protected ArrayList<CardObject> waitingRoomCards;

	protected SpriteObject vObj;

	public WaitingRoomField(double x, double y) {
		super(x, y);
		waitingRoomCards = new ArrayList<CardObject>();

		vObj = new SpriteObject(TextureManager.getTexture("CARDFIELD"));
	}

	public void destroy() {
		vObj.destroy();
	}

	public void addCard(CardObject card) {
		card.setCardField(this);
		waitingRoomCards.add(card);
	}
	public void removeCard(CardObject card) {waitingRoomCards.remove(card);}

	public void clearWaitingRoom() {waitingRoomCards.clear();}

	public CardObject getCard(int card) {return waitingRoomCards.get(card);}

	public int getNumCards() {return waitingRoomCards.size();}

	@Override
	public void update() {
		for (int i=0;i<waitingRoomCards.size();i++) {
			waitingRoomCards.get(i).hide = false;
			waitingRoomCards.get(i).update();
		}
	}

	@Override
	public void renderField(DefaultShader sp, double ip) {
		sp.pushMatrix();
		sp.translate(posX, posY, 0.0);
		sp.scale(size+2.0, size+2.0, 1.0);
		sp.applyModelMatrix();
		sp.popMatrix();

		if (highlight) sp.setColor(1.0, 0.0, 0.0, 1.0);
		else sp.setColor(1.0, 1.0, 1.0, 1.0);

		vObj.render(sp);
	}

	@Override
	public void renderCards(DefaultShader sp, double ip) {
		for (int i=0;i<waitingRoomCards.size();i++) waitingRoomCards.get(i).render(sp, ip);
	}

}
