package zan.wscard.core;

import static zan.lib.input.InputManager.IM_KEY_ESCAPE;
import static zan.lib.input.InputManager.IM_KEY_F11;
import static zan.lib.input.InputManager.IM_RELEASE;
import zan.lib.core.CoreEngine;
import zan.wscard.panel.GamePanel;

public class GameCore extends CoreEngine {
	
	@Override
	protected void onKey(int key, int state, int mods, int scancode) {
		if (key == IM_KEY_ESCAPE && state == IM_RELEASE) close();
		else if (key == IM_KEY_F11 && state == IM_RELEASE) toggleFullScreen();
		super.onKey(key, state, mods, scancode);
	}
	
	public static void main(String[] args) {
		GameCore core = new GameCore();
		core.setTitle("Weiss-Schwarz Simulator");
		core.setScreenSize(800, 600);
		core.setPanel(new GamePanel(core));
		core.run();
	}
	
}
