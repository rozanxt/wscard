package zan.wscard.panel;

import java.util.ArrayList;
import java.util.Random;

import zan.lib.gfx.ShaderProgram;
import zan.lib.gfx.TextureManager;
import zan.lib.gfx.obj.SpriteObject;
import zan.lib.gfx.text.TextManager;
import zan.lib.gfx.view.ViewPort2D;
import zan.lib.input.InputManager;
import zan.lib.math.vector.Vec2D;
import zan.lib.panel.BasePanel;
import zan.lib.res.ResourceReader;
import zan.wscard.card.CardData;
import zan.wscard.card.CardReader;
import zan.wscard.core.GameCore;
import zan.wscard.obj.CardField;
import zan.wscard.obj.CardObject;
import zan.wscard.obj.HandField;
import zan.wscard.obj.StageField;

public class GamePanel extends BasePanel {
	
	private ShaderProgram shaderProgram;
	private ViewPort2D viewPort;
	
	private HandField handField;
	private ArrayList<StageField> stageFields;
	
	private ArrayList<CardObject> gameCards;
	
	private CardObject focusedCard;
	private CardObject heldCard;
	private Vec2D heldOffset;
	
	public GamePanel(GameCore core) {
		viewPort = new ViewPort2D(0, 0, core.getScreenWidth(), core.getScreenHeight());
	}
	
	@Override
	public void init() {
		shaderProgram = new ShaderProgram();
		
		viewPort.setHeightInterval(600.0);
		viewPort.showView();
		viewPort.projectView(shaderProgram);
		
		TextManager.loadFontFile(new ResourceReader("res/font/fonts.res").getData().getNode("defont"));
		
		Random rnd = new Random();
		
		CardReader cr = new CardReader();
		ArrayList<CardData> cards = cr.loadCardData("res/card/LH.wsci");
		
		handField = new HandField();
		stageFields = new ArrayList<StageField>();
		for (int i=0;i<3;i++) {
			StageField cf = new StageField();
			cf.setPos(-80.0+80.0*i, 0.0);
			cf.setSize(100.0);
			stageFields.add(cf);
		}
		for (int i=0;i<2;i++) {
			StageField cf = new StageField();
			cf.setPos(-40.0+80.0*i, -110.0);
			cf.setSize(100.0);
			stageFields.add(cf);
		}
		
		gameCards = new ArrayList<CardObject>();
		for (int i=0;i<5;i++) {
			CardData c = cards.get(rnd.nextInt(20));
			CardObject co = new CardObject(c, new SpriteObject(TextureManager.loadTexture(c.id, c.image), 500f, 730f));
			co.setAnchor(-(40.0*(5-1))+80.0*i, -240.0);
			co.setPos(-(40.0*(5-1))+80.0*i, -240.0);
			co.setSize(100.0);
			co.setField(handField);
			handField.addCard(co);
			gameCards.add(co);
		}
		
		focusedCard = null;
		heldCard = null;
		heldOffset = new Vec2D();
	}
	
	@Override
	public void destroy() {
		shaderProgram.destroy();
		for (int i=0;i<stageFields.size();i++) stageFields.get(i).destroy();
		for (int i=0;i<gameCards.size();i++) gameCards.get(i).destroy();
	}
	
	@Override
	public void update(double time) {
		double mouseX = viewPort.getScreenToVirtualX(InputManager.getMouseX());
		double mouseY = viewPort.getScreenToVirtualY(InputManager.getMouseY());
		
		focusedCard = null;
		for (int i=0;i<gameCards.size();i++) {
			CardObject hc = gameCards.get(i);
			hc.toggleAnchor(true);
			if (hc.isInBound(viewPort.getScreenToVirtualX(InputManager.getMouseX()), viewPort.getScreenToVirtualY(InputManager.getMouseY()))) {
				focusedCard = hc;
				if (InputManager.isMousePressed(InputManager.IM_MOUSE_BUTTON_1)) {
					heldCard = hc;
					heldOffset.setComponents(mouseX - hc.getAnchorX(), mouseY - hc.getAnchorY());
				}
			}
		}
		
		if (heldCard != null && InputManager.isMouseReleased(InputManager.IM_MOUSE_BUTTON_1)) {
			boolean inField = false; // TODO
			for (int i=0;i<stageFields.size();i++) {
				StageField cf = stageFields.get(i);
				if (cf.isInBound(mouseX, mouseY)) {
					CardField previousField = heldCard.getField();
					if (previousField != null) {
						if (previousField instanceof HandField) {
							if (cf.getCard() == null) {
								HandField hf = (HandField)previousField;
								hf.removeCard(heldCard);
								heldCard.setField(cf);
								cf.setCard(heldCard);
								inField = true;
							}
						} else if (previousField instanceof StageField) {
							StageField sf = (StageField)previousField;
							sf.setCard(cf.getCard());
							if (cf.getCard() != null) cf.getCard().setField(previousField);
							heldCard.setField(cf);
							cf.setCard(heldCard);
							inField = true;
						}
					}
				}
			}
			if (!inField) {
				CardField previousField = heldCard.getField();
				if (previousField != null) {
					if (previousField instanceof StageField) {
						StageField sf = (StageField)previousField;
						sf.setCard(null);
						heldCard.setField(handField);
						handField.addCard(heldCard);
					}
				}
			}
			heldCard = null;
		}
		if (heldCard != null) {
			heldCard.toggleAnchor(false);
			heldCard.setPos(mouseX - heldOffset.getX(), mouseY - heldOffset.getY());
		}
		
		for (int i=0;i<stageFields.size();i++) {stageFields.get(i).isInBound(mouseX, mouseY);}
		
		handField.anchorCards();
		for (int i=0;i<gameCards.size();i++) gameCards.get(i).update();
	}
	
	@Override
	public void render(double ip) {
		shaderProgram.bind();
		shaderProgram.pushMatrix();
		viewPort.adjustView(shaderProgram);
		
		for (int i=0;i<stageFields.size();i++) stageFields.get(i).render(shaderProgram);
		for (int i=0;i<gameCards.size();i++) if (heldCard != gameCards.get(i)) gameCards.get(i).render(shaderProgram, ip);
		if (heldCard != null) heldCard.render(shaderProgram, ip);
		
		if (focusedCard != null) {
			shaderProgram.pushMatrix();
			shaderProgram.translate(-400.0, 290.0, 0.0);
			shaderProgram.scale(10.0, 10.0, 1.0);
			TextManager.renderText(shaderProgram, focusedCard.getCardData().name, "defont");
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

/*public class GamePanel extends BasePanel {
	
	private ShaderProgram shaderProgram;
	private ViewPort2D viewPort;
	
	private LocalGameServer gameServer;
	
	private ArrayList<CardObject> handCards;
	
	private int cardFocus;
	
	public GamePanel(GameCore core) {
		viewPort = new ViewPort2D(0, 0, core.getScreenWidth(), core.getScreenHeight());
	}
	
	@Override
	public void init() {
		shaderProgram = new ShaderProgram();
		
		viewPort.setHeightInterval(300f);
		viewPort.setOrigin(0f, 0f);
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
		//shaderProgram.multMatrix(MatUtil.translationMat44D(-0.5f*viewPort.getHeightInterval()*viewPort.getScreenRatio(), -0.5f*viewPort.getHeightInterval(), 0f));
		
		shaderProgram.pushMatrix();
		shaderProgram.multMatrix(MatUtil.translationMat44D(0.0, 0.0, 0.0));
		shaderProgram.multMatrix(MatUtil.rotationMat44D(0.0, 0.0, 0.0, 1.0));
		shaderProgram.multMatrix(MatUtil.scaleMat44D(10.0, 12.0, 1.0));
		TextManager.renderText(shaderProgram, "MX: " + viewPort.getScreenToVirtualX((float)InputManager.getMouseX()) + " " + "MY: " + viewPort.getScreenToVirtualY((float)InputManager.getMouseY()), "defont");
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
	
}*/
