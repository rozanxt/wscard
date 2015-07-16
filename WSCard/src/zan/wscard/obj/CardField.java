package zan.wscard.obj;

import zan.lib.gfx.ShaderProgram;
import zan.lib.gfx.obj.VertexObject;
import zan.lib.gfx.sprite.BaseSprite;
import zan.lib.util.Utility;
import static org.lwjgl.opengl.GL11.GL_LINE_LOOP;

public class CardField extends BaseSprite {
	
	protected VertexObject vObj;
	
	private boolean inBound;
	
	public CardField() {
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
		
		inBound = false;
	}
	
	// TODO
	public boolean isInBound(double sx, double sy) {
		if (sx > posX-0.5*scaleX*(500.0/730.0) && sx < posX+0.5*scaleX*(500.0/730.0) && sy > posY-0.5*scaleY && sy < posY+0.5*scaleY) {
			inBound = true;
			return true;
		}
		inBound = false;
		return false;
	}
	
	@Override
	public void destroy() {
		vObj.destroy();
	}
	
	@Override
	public void render(ShaderProgram sp, double ip) {
		sp.enableTexture(false);
		double iPosX = Utility.interpolateLinear(oldPosX, posX, ip);
		double iPosY = Utility.interpolateLinear(oldPosY, posY, ip);
		double iScaleX = Utility.interpolateLinear(oldScaleX, scaleX, ip);
		double iScaleY = Utility.interpolateLinear(oldScaleY, scaleY, ip);
		double iAngle = Utility.interpolateLinear(oldAngle, angle, ip);
		double iOpacity = Utility.interpolateLinear(oldOpacity, opacity, ip);
		
		sp.pushMatrix();
		sp.translate(iPosX, iPosY, 0.0);
		sp.rotate(iAngle, 0.0, 0.0, 1.0);
		sp.scale(iScaleX, iScaleY, 1.0);
		sp.applyModelView();
		sp.popMatrix();
		
		if (inBound) sp.setColor(1.0, 1.0, 0.0, iOpacity);
		else sp.setColor(1.0, 1.0, 1.0, iOpacity);
		
		draw(sp);
		sp.enableTexture(true);
	}
	
	@Override
	public void draw(ShaderProgram sp) {
		vObj.render(sp);
	}
	
}
