package zan.wscard.obj;

import static org.lwjgl.opengl.GL11.GL_LINE_LOOP;
import zan.lib.gfx.ShaderProgram;
import zan.lib.gfx.obj.VertexObject;

public class StageField extends CardField {
	
	protected CardObject cardObj;
	
	protected VertexObject vObj;
	
	protected double posX, posY;
	protected double size;
	
	protected boolean inBound; // TODO
	
	public StageField() {
		cardObj = null;
		
		final int[] ind = {0, 1, 2, 3};
		final float[] ver = {
			-0.5f*(500f/730f), -0.5f,
			0.5f*(500f/730f), -0.5f,
			0.5f*(500f/730f), 0.5f,
			-0.5f*(500f/730f), 0.5f,
		};
		vObj = new VertexObject(ver, ind);
		vObj.setNumCoords(2);
		vObj.setDrawMode(GL_LINE_LOOP);
		
		posX = 0.0;
		posY = 0.0;
		size = 1.0;
		
		inBound = false;
	}
	
	public void destroy() {
		vObj.destroy();
	}
	
	public void setCard(CardObject cardObj) {this.cardObj = cardObj;}
	public CardObject getCard() {return cardObj;}
	
	public void setPos(double posX, double posY) {
		this.posX = posX;
		this.posY = posY;
	}
	public void setSize(double size) {this.size = size;}
	
	public double getPosX() {return posX;}
	public double getPosY() {return posY;}
	
	// TODO
	public boolean isInBound(double sx, double sy) {
		if (sx > posX-0.5*size*(500.0/730.0) && sx < posX+0.5*size*(500.0/730.0) && sy > posY-0.5*size && sy < posY+0.5*size) {
			inBound = true;
			return true;
		}
		inBound = false;
		return false;
	}
	
	public void render(ShaderProgram sp) {
		sp.enableTexture(false);
		
		sp.pushMatrix();
		sp.translate(posX, posY, 0.0);
		sp.scale(size, size, 1.0);
		sp.applyModelView();
		sp.popMatrix();
		
		if (inBound) sp.setColor(1.0, 1.0, 0.0, 1.0);
		else sp.setColor(1.0, 1.0, 1.0, 1.0);
		
		vObj.render(sp);
		
		sp.enableTexture(true);
	}
	
}
