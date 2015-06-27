package zan.game.wscard.core;

import zan.game.wscard.panel.GamePanel;
import zan.lib.core.CoreEngine;

public class GameCore extends CoreEngine {
	
	public static void main(String[] args) {
		GameCore core = new GameCore();
		core.setTitle("Weiss-Schwarz Simulator");
		core.setScreenSize(800, 600);
		core.setPanel(new GamePanel());
		core.run();
	}
	
}
