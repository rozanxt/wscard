package zan.wscard.panel;

import java.util.ArrayList;

import zan.lib.gfx.ShaderProgram;
import zan.lib.gfx.text.TextManager;
import zan.lib.gfx.view.ViewPort2D;
import zan.lib.math.matrix.MatUtil;
import zan.lib.panel.BasePanel;
import zan.lib.res.ResourceReader;
import zan.wscard.card.CardData;
import zan.wscard.card.CardReader;
import zan.wscard.core.GameCore;
import zan.wscard.mechanics.LocalGameServer;

public class GamePanel extends BasePanel {
	
	private ShaderProgram shaderProgram;
	private ViewPort2D viewPort;
	
	private LocalGameServer gameServer;
	
	public GamePanel(GameCore core) {
		viewPort = new ViewPort2D(0, 0, core.getScreenWidth(), core.getScreenHeight());
	}
	
	@Override
	public void init() {
		shaderProgram = new ShaderProgram("res/shader/sample.glvs", "res/shader/sample.glfs");
		
		viewPort.setHeightInterval(600f);
		viewPort.showView();
		viewPort.projectView(shaderProgram);
		
		TextManager.loadFontFile(new ResourceReader("res/font/fonts.res").getData().getNode("defont"));
		
		CardReader cr = new CardReader();
		ArrayList<CardData> LHCards = cr.loadCardData("res/card/LH.wsci");
		ArrayList<CardData> LHDeck = new ArrayList<CardData>();
		final int[] deckSetup = {4, 1, 2, 4, 2, 2, 2, 2, 2, 2, 4, 2, 4, 2, 1, 4, 2, 2, 4, 2};
		
		for (int i=0;i<deckSetup.length;i++) {
			for (int j=0;j<deckSetup[i];j++) {
				LHDeck.add(LHCards.get(i));
			}
		}
		
		gameServer = new LocalGameServer();
		gameServer.initialPhase(LHDeck);
	}
	
	@Override
	public void destroy() {
		shaderProgram.destroy();
	}
	
	@Override
	public void update(double time) {
		
	}
	
	@Override
	public void render(double ip) {
		shaderProgram.bind();
		shaderProgram.pushMatrix();
		viewPort.adjustView(shaderProgram);
		
		// TODO FOR DEBUGGING TEXT RENDERING HAS MEMORY LEAKS
		for (int i=0;i<5;i++) {
			shaderProgram.pushMatrix();
			shaderProgram.multMatrix(MatUtil.translationMat44D(-350.0, 250.0-50.0*i, 0.0));
			shaderProgram.multMatrix(MatUtil.rotationMat44D(0.0, 0.0, 0.0, 1.0));
			shaderProgram.multMatrix(MatUtil.scaleMat44D(20.0, 20.0, 1.0));
			TextManager.renderText(shaderProgram, gameServer.getPlayer(0).getPlayerCard(i).name, "defont");
			shaderProgram.popMatrix();
		}
		
		for (int i=0;i<5;i++) {
			shaderProgram.pushMatrix();
			shaderProgram.multMatrix(MatUtil.translationMat44D(-350.0, -50.0-50.0*i, 0.0));
			shaderProgram.multMatrix(MatUtil.rotationMat44D(0.0, 0.0, 0.0, 1.0));
			shaderProgram.multMatrix(MatUtil.scaleMat44D(20.0, 20.0, 1.0));
			TextManager.renderText(shaderProgram, gameServer.getPlayer(1).getPlayerCard(i).name, "defont");
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
