package zan.wscard.core;

import zan.lib.core.CoreEngine;
import zan.wscard.panel.GamePanel;

public class GameCore extends CoreEngine {
	
	public static void main(String[] args) {
		GameCore core = new GameCore();
		core.setTitle("Weiss-Schwarz Simulator");
		core.setScreenSize(800, 600);
		core.setPanel(new GamePanel());
		core.run();
	}
	
}
