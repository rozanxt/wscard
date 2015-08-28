package zan.wscard.panel;

import java.util.ArrayList;

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
import zan.wscard.mechanics.LocalGameClient;
import zan.wscard.mechanics.LocalGameServer;
import zan.wscard.obj.CardField;
import zan.wscard.obj.CardObject;
import zan.wscard.obj.HandField;
import zan.wscard.obj.StageField;

public class GamePanel extends BasePanel {
	
	private ShaderProgram shaderProgram;
	private ViewPort2D viewPort;
	
	private LocalGameServer gameServer;
	private LocalGameClient clientA;
	private LocalGameClient clientB;
	
	private HandField handField;
	private ArrayList<StageField> stageFields;
	private ArrayList<StageField> stageFields2;
	
	private ArrayList<CardObject> gameCards;
	
	private CardObject focusedCard;
	private CardObject heldCard;
	private Vec2D heldOffset;
	
	private static final double cardSize = 80.0;
	
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
		
		CardReader cr = new CardReader();
		ArrayList<CardData> LHCards = cr.loadCardData("res/card/LH.wsci");
		ArrayList<CardData> deckCards = new ArrayList<CardData>();
		
		final int[] deckSetup = {4, 1, 2, 4, 2, 2, 2, 2, 2, 2, 4, 2, 4, 2, 1, 4, 2, 2, 4, 2};
		for (int i=0;i<deckSetup.length;i++) {
			for (int j=0;j<deckSetup[i];j++) {
				deckCards.add(LHCards.get(i));
			}
		}
		
		gameServer = new LocalGameServer();
		clientA = new LocalGameClient(gameServer, 0);
		clientB = new LocalGameClient(gameServer, 1);
		gameServer.addClient(clientA);
		gameServer.addClient(clientB);
		
		gameServer.init(deckCards, deckCards);
		clientA.init(deckCards, deckCards);
		clientB.init(deckCards, deckCards);
		
		clientA.writeToServer("REQ_HAND");
		gameServer.update();
		clientA.update();
		
		handField = new HandField();
		stageFields = new ArrayList<StageField>();
		for (int i=0;i<3;i++) {
			StageField cf = new StageField();
			cf.setPos(-60.0+60.0*i, -50.0);
			cf.setSize(cardSize);
			stageFields.add(cf);
		}
		for (int i=0;i<2;i++) {
			StageField cf = new StageField();
			cf.setPos(-30.0+60.0*i, -135.0);
			cf.setSize(cardSize);
			stageFields.add(cf);
		}
		
		stageFields2 = new ArrayList<StageField>();
		for (int i=0;i<3;i++) {
			StageField cf = new StageField();
			cf.setPos(-60.0+60.0*i, 50.0);
			cf.setSize(cardSize);
			stageFields2.add(cf);
		}
		for (int i=0;i<2;i++) {
			StageField cf = new StageField();
			cf.setPos(-30.0+60.0*i, 135.0);
			cf.setSize(cardSize);
			stageFields2.add(cf);
		}
		
		gameCards = new ArrayList<CardObject>();
		for (int i=0;i<clientA.getClientPlayer().getPlayerHand().size();i++) {
			CardData c = clientA.getClientPlayer().getCardData(clientA.getClientPlayer().getPlayerHand().get(i));
			CardObject co = new CardObject(c, new SpriteObject(TextureManager.loadTexture(c.id, c.image), 500f, 730f));
			co.setAnchor(-(30.0*(5-1))+60.0*i, -240.0);
			co.setPos(-(30.0*(5-1))+60.0*i, -240.0);
			co.setSize(cardSize);
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
		for (int i=0;i<stageFields2.size();i++) stageFields2.get(i).destroy();
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
		for (int i=0;i<stageFields2.size();i++) {stageFields2.get(i).isInBound(mouseX, mouseY);}
		
		handField.anchorCards();
		for (int i=0;i<gameCards.size();i++) gameCards.get(i).update();
	}
	
	@Override
	public void render(double ip) {
		shaderProgram.bind();
		shaderProgram.pushMatrix();
		viewPort.adjustView(shaderProgram);
		
		for (int i=0;i<stageFields.size();i++) stageFields.get(i).render(shaderProgram);
		for (int i=0;i<stageFields2.size();i++) stageFields2.get(i).render(shaderProgram);
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
