package zan.wscard.panel;

import java.util.ArrayList;

import zan.lib.gfx.ShaderProgram;
import zan.lib.gfx.TextureManager;
import zan.lib.gfx.obj.SpriteObject;
import zan.lib.gfx.text.TextManager;
import zan.lib.gfx.view.ViewPortScreen;
import zan.lib.input.InputManager;
import zan.lib.math.matrix.MatUtil;
import zan.lib.panel.BasePanel;
import zan.lib.res.ResourceReader;
import zan.wscard.card.CardData;
import zan.wscard.card.CardReader;
import zan.wscard.core.GameCore;
import zan.wscard.mechanics.LocalGameServer;
import zan.wscard.obj.CardObject;

public class GamePanel extends BasePanel {
	
	private ShaderProgram shaderProgram;
	private ViewPortScreen viewPort;
	
	private LocalGameServer gameServer;
	
	private ArrayList<CardObject> handCards;
	
	private int cardFocus;
	
	public GamePanel(GameCore core) {
		viewPort = new ViewPortScreen(core.getScreenWidth(), core.getScreenHeight());
	}
	
	@Override
	public void init() {
		shaderProgram = new ShaderProgram("res/shader/sample.glvs", "res/shader/sample.glfs");
		
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
		
		handCards = new ArrayList<CardObject>();
		for (int i=0;i<5;i++) {
			CardData card = gameServer.getPlayer(0).getPlayerCard(i);
			SpriteObject cardSpr = new SpriteObject(TextureManager.loadTexture(card.name, card.image), 500, 730);
			CardObject cardObj = new CardObject(card, cardSpr);
			cardObj.setPos(100f+i*150f, 150f);
			cardObj.setScale(200f);
			handCards.add(cardObj);
		}
		
		cardFocus = -1;
	}
	
	@Override
	public void destroy() {
		shaderProgram.destroy();
	}
	
	@Override
	public void update(double time) {
		for (int i=0;i<5;i++) {
			if (handCards.get(i).isInShape(InputManager.getMouseX(), InputManager.getMouseY())) {
				cardFocus = i;
			}
			
			handCards.get(i).update();
		}
	}
	
	@Override
	public void render(double ip) {
		shaderProgram.bind();
		shaderProgram.pushMatrix();
		viewPort.adjustView(shaderProgram);
		
		shaderProgram.pushMatrix();
		shaderProgram.multMatrix(MatUtil.translationMat44D(0.0, 0.0, 0.0));
		shaderProgram.multMatrix(MatUtil.rotationMat44D(0.0, 0.0, 0.0, 1.0));
		shaderProgram.multMatrix(MatUtil.scaleMat44D(10.0, 12.0, 1.0));
		TextManager.renderText(shaderProgram, "MX: " + InputManager.getMouseX() + " " + "MY: " + InputManager.getMouseY(), "defont");
		shaderProgram.popMatrix();
		
		// TODO FOR DEBUGGING
		for (int i=0;i<5;i++) {
			if (cardFocus != -1) {
				for (int j=0;j<14;j++) {
					String str = "";
					CardData data = gameServer.getPlayer(0).getPlayerCard(cardFocus);
					switch (j) {
						case 0:
							str = "ID: " + data.id;
							break;
						case 1:
							str = "Name: " + data.name;
							break;
						case 2:
							if (data.type == 0) str = "Character Card";
							else if (data.type == 1) str = "Climax Card";
							else if (data.type == 2) str = "Event Card";
							break;
						case 3:
							if (data.color == 0) str = "Color: Red";
							else if (data.color == 1) str = "Color: Blue";
							else if (data.color == 2) str = "Color: Green";
							else if (data.color == 3) str = "Color: Yellow";
							break;
						case 4:
							str = "Level: " + data.level;
							break;
						case 5:
							str = "Cost: " + data.cost;
							break;
						case 6:
							str = "Power: " + data.power;
							break;
						case 7:
							str = "Soul: " + data.soul;
							break;
						case 8:
							str = "Trigger: " + data.trigger;
							break;
						case 9:
							str = "Traits: " + data.trait;
							break;
						case 10:
							str = "Rarity: " + data.rarity;
							break;
						case 11:
							if (data.side == 0) str = "Side: Weiss";
							else if (data.side == 1) str = "Side: Schwarz";
							break;
						case 12:
							str = data.cardtext;
							break;
						case 13:
							str = data.flavortext;
							break;
					}
					
					shaderProgram.pushMatrix();
					shaderProgram.multMatrix(MatUtil.translationMat44D(50.0, 550.0-20.0*j, 0.0));
					shaderProgram.multMatrix(MatUtil.rotationMat44D(0.0, 0.0, 0.0, 1.0));
					shaderProgram.multMatrix(MatUtil.scaleMat44D(10.0, 12.0, 1.0));
					TextManager.renderText(shaderProgram, str, "defont");
					shaderProgram.popMatrix();
				}
			}
			
			handCards.get(i).render(shaderProgram, ip);
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
