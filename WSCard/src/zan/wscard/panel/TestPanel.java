package zan.wscard.panel;

import java.util.ArrayList;

import zan.lib.gfx.shader.DefaultShader;
import zan.lib.gfx.text.TextManager;
import zan.lib.gfx.view.ViewPort2D;
import zan.lib.input.InputManager;
import zan.lib.core.BasePanel;
import zan.wscard.core.GameCore;

public class TestPanel extends BasePanel {

	private DefaultShader shaderProgram;
	private ViewPort2D viewPort;

	private GameCore gameCore;
	private int mode;
	private String address;

	public TestPanel(GameCore core) {
		shaderProgram = new DefaultShader();
		viewPort = new ViewPort2D(0, 0, core.getScreenWidth(), core.getScreenHeight());
		gameCore = core;
		mode = 0;
		address = "localhost";
	}

	@Override
	public void init() {
		shaderProgram.loadProgram();
		shaderProgram.enableBlend(true);

		viewPort.setHeightInterval(600.0);
		viewPort.showView();
		viewPort.projectView(shaderProgram);
	}

	@Override
	public void destroy() {

	}

	@Override
	public BasePanel changePanel() {
		if (mode == 1) return new GamePanel(gameCore, mode, "localhost");
		else if (mode == 2) return new GamePanel(gameCore, mode, address);
		return null;
	}

	@Override
	public void update(double time) {
		if (InputManager.isKeyReleased(InputManager.IM_KEY_ESCAPE)) gameCore.close();
		else if (InputManager.isKeyReleased(InputManager.IM_KEY_F11)) gameCore.toggleFullScreen();

		if (mode == 0) {
			if (InputManager.isKeyPressed(InputManager.IM_KEY_S)) mode = 1;
			else if (InputManager.isKeyPressed(InputManager.IM_KEY_C)) {
				mode = 3;
				address = "";
			}
		} else if (mode == 3) {
			if (InputManager.isKeyPressed(InputManager.IM_KEY_ENTER)) {
				if (address.isEmpty()) address = "localhost";
				mode = 2;
			} else if (InputManager.isKeyPressed(InputManager.IM_KEY_BACKSPACE)) {
				if (!address.isEmpty()) address = address.substring(0, address.length()-1);
			} else {
				ArrayList<Character> charEvents = InputManager.getCharEvents();
				for (int i=0;i<charEvents.size();i++) address += charEvents.get(i);
			}
		}
	}

	@Override
	public void render(double ip) {
		shaderProgram.bind();
		shaderProgram.pushMatrix();
		viewPort.adjustView(shaderProgram);

		shaderProgram.setColor(1.0, 1.0, 1.0, 1.0);
		if (mode == 0) {
			shaderProgram.pushMatrix();
			shaderProgram.translate(-130.0, 200.0, 0.0);
			shaderProgram.scale(20.0, 20.0, 1.0);
			TextManager.renderText(shaderProgram, "WSCard Test Version", "defont");
			shaderProgram.popMatrix();

			shaderProgram.pushMatrix();
			shaderProgram.translate(-200.0, 150.0, 0.0);
			shaderProgram.scale(20.0, 20.0, 1.0);
			TextManager.renderText(shaderProgram, "Press S to start game as server.", "defont");
			shaderProgram.popMatrix();

			shaderProgram.pushMatrix();
			shaderProgram.translate(-200.0, 100.0, 0.0);
			shaderProgram.scale(20.0, 20.0, 1.0);
			TextManager.renderText(shaderProgram, "Press C to start game as client.", "defont");
			shaderProgram.popMatrix();
		} else if (mode == 3) {
			shaderProgram.pushMatrix();
			shaderProgram.translate(-200.0, 50.0, 0.0);
			shaderProgram.scale(20.0, 20.0, 1.0);
			TextManager.renderText(shaderProgram, "Enter server: " + address, "defont");
			shaderProgram.popMatrix();
		} else {
			shaderProgram.pushMatrix();
			shaderProgram.translate(-50.0, 0.0, 0.0);
			shaderProgram.scale(20.0, 20.0, 1.0);
			TextManager.renderText(shaderProgram, "Loading...", "defont");
			shaderProgram.popMatrix();
		}

		shaderProgram.popMatrix();
		shaderProgram.unbind();
	}

	@Override
	public void onScreenResize(int width, int height) {
		shaderProgram.bindState();
		viewPort.setScreenSize(width, height);
		viewPort.setViewPort(0, 0, width, height);
		viewPort.showView();
		viewPort.projectView(shaderProgram);
	}

}
