package zan.wscard.gfx;

import zan.lib.gfx.ShaderProgram;
import zan.lib.gfx.obj.SpriteObject;
import zan.lib.gfx.sprite.BaseSprite;
import zan.lib.gfx.texture.TextureInfo;

public class CardSprite extends BaseSprite {
	
	protected SpriteObject cardFace;
	protected SpriteObject cardBack;
	
	protected boolean cardHide;
	
	public CardSprite(TextureInfo face, TextureInfo back) {
		super();
		cardFace = new SpriteObject(face);
		cardBack = new SpriteObject(back);
		cardHide = false;
	}
	
	public void hide(boolean hide) {cardHide = hide;}
	
	@Override
	public void destroy() {
		cardFace.destroy();
		cardBack.destroy();
	}
	
	@Override
	public void draw(ShaderProgram sp) {
		if (cardHide) cardBack.render(sp);
		else cardFace.render(sp);
	}
	
}
