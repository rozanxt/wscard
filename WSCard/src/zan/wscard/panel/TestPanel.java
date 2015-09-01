package zan.wscard.panel;

import zan.lib.input.InputManager;
import zan.lib.panel.BasePanel;
import zan.wscard.core.GameCore;

public class TestPanel extends BasePanel {
	
	private GameCore gameCore;
	private int mode;
	
	public TestPanel(GameCore core) {
		gameCore = core;
		mode = 0;
	}
	
	@Override
	public void init() {
		System.out.println("WSCard localhost Test Version");
		System.out.println("Press 1 to start game as server.");
		System.out.println("Press 2 to start game as client.");
	}
	
	@Override
	public void destroy() {
		
	}
	
	@Override
	public BasePanel changePanel() {
		if (mode != 0) return new GamePanel(gameCore, mode);
		return null;
	}
	
	@Override
	public void update(double time) {
		if (InputManager.isKeyPressed(InputManager.IM_KEY_1)) mode = 1;
		else if (InputManager.isKeyPressed(InputManager.IM_KEY_2)) mode = 2;
	}
	
	@Override
	public void render(double ip) {
		
	}
	
}
