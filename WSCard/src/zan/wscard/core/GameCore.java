package zan.wscard.core;

import zan.lib.core.CoreEngine;
import zan.lib.gfx.text.TextManager;
import zan.lib.gfx.texture.TextureManager;
import zan.wscard.panel.TestPanel;

public class GameCore extends CoreEngine {

	@Override
	protected void onInit() {
		TextureManager.init();
		TextManager.init();
		TextManager.loadFontFile("res/fnt/fonts.res");
	}

	@Override
	protected void onDestroy() {
		TextureManager.destroy();
		TextManager.destroy();
	}

	public static void main(String[] args) {
		GameCore core = new GameCore();
		core.setTitle("Weiss-Schwarz Simulator");
		core.setIcon("res/ico/wscard_icon.png");
		core.setScreenSize(800, 600);
		core.setPanel(new TestPanel(core));
		core.run();
	}

}
