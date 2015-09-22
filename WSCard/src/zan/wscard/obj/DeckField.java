package zan.wscard.obj;

import java.util.ArrayList;

import zan.lib.gfx.shader.DefaultShader;
import zan.lib.gfx.obj.SpriteObject;
import zan.lib.gfx.texture.TextureManager;

public class DeckField extends CardField {

	protected ArrayList<CardObject> deckCards;

	protected SpriteObject deckSprite;
	protected SpriteObject vObj;

	public DeckField(double x, double y) {
		super(x, y);
		deckCards = new ArrayList<CardObject>();
		deckSprite = new SpriteObject(TextureManager.getTexture("CARDBACK"));
		vObj = new SpriteObject(TextureManager.getTexture("CARDFIELD"));
	}

	public void destroy() {
		vObj.destroy();
	}

	public void addCard(CardObject card) {
		card.setCardField(this);
		deckCards.add(card);
	}

	@Override
	public void update() {
		if (!deckCards.isEmpty()) {
			ArrayList<CardObject> remove = new ArrayList<CardObject>();
			for (int i=0;i<deckCards.size();i++) {
				if (deckCards.get(i).isInAnchor()) remove.add(deckCards.get(i));
				deckCards.get(i).hide = true;
				deckCards.get(i).update();
			}
			if (!remove.isEmpty()) deckCards.remove(remove);
		}
	}

	@Override
	public void renderField(DefaultShader sp, double ip) {
		if (highlight) sp.setColor(0.0, 1.0, 0.0, 1.0);
		else sp.setColor(1.0, 1.0, 1.0, 1.0);

		sp.pushMatrix();
		sp.translate(posX, posY, 0.0);
		sp.scale(size+2.0, size+2.0, 1.0);
		sp.applyModelMatrix();
		vObj.render(sp);
		sp.popMatrix();

		sp.setColor(1.0, 1.0, 1.0, 1.0);

		sp.pushMatrix();
		sp.translate(posX, posY, 0.0);
		sp.scale(size, size, 1.0);
		sp.applyModelMatrix();
		deckSprite.render(sp);
		sp.popMatrix();
	}

	@Override
	public void renderCards(DefaultShader sp, double ip) {
		for (int i=0;i<deckCards.size();i++) deckCards.get(i).render(sp, ip);
	}

}
