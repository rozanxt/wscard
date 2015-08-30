package zan.wscard.panel;

import java.util.ArrayList;
import java.util.HashMap;

import zan.lib.gfx.ShaderProgram;
import zan.lib.gfx.TextureManager;
import zan.lib.gfx.obj.SpriteObject;
import zan.lib.gfx.sprite.Sprite;
import zan.lib.gfx.text.TextManager;
import zan.lib.gfx.view.ViewPort2D;
import zan.lib.input.InputManager;
import zan.lib.math.vector.Vec2D;
import zan.lib.panel.BasePanel;
import zan.lib.res.ResourceReader;
import static zan.lib.input.InputManager.*;
import zan.wscard.card.CardData;
import zan.wscard.card.CardReader;
import zan.wscard.core.GameCore;
import zan.wscard.sys.GameClient;
import zan.wscard.sys.GameSystem;
import zan.wscard.sys.LocalGameClient;
import zan.wscard.sys.LocalGameServer;
import zan.wscard.sys.PlayerInfo;
import zan.wscard.obj.CardField;
import zan.wscard.obj.CardObject;
import zan.wscard.obj.HandField;
import zan.wscard.obj.StageField;

public class GamePanel extends BasePanel {
	
	private ShaderProgram shaderProgram;
	private ViewPort2D viewPort;
	
	private LocalGameServer gameServer;
	private LocalGameClient clientA, clientB;
	
	private HashMap<String, Integer> cardSprites;
	
	private ArrayList<CardObject> gameCards;
	private ArrayList<Sprite> opponentCards;
	
	private CardObject focusedCard;
	private CardObject heldCard;
	private Vec2D heldOffset;
	
	private HandField playerHand;
	private ArrayList<StageField> playerFields;
	private ArrayList<StageField> opponentFields;
	
	// TODO
	private static final double cardSize = 80.0;
	private int delay;
	
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
		ArrayList<CardData> testCards = cr.loadCardData("res/card/LH.wsci");
		ArrayList<CardData> deckCards = new ArrayList<CardData>();
		final int[] deckSetup = {4, 1, 2, 4, 2, 2, 2, 2, 2, 2, 4, 2, 4, 2, 1, 4, 2, 2, 4, 2};
		for (int i=0;i<deckSetup.length;i++) {
			for (int j=0;j<deckSetup[i];j++) {
				deckCards.add(testCards.get(i));
			}
		}
		
		cardSprites = new HashMap<String, Integer>();
		for (int i=0;i<testCards.size();i++) {
			CardData c = testCards.get(i);
			cardSprites.put(c.image, TextureManager.loadTexture(c.id, c.image));
		}
		
		PlayerInfo infoA = new PlayerInfo("Player A", deckCards);
		PlayerInfo infoB = new PlayerInfo("Player B", deckCards);
		
		gameServer = new LocalGameServer();
		clientA = new LocalGameClient(gameServer);
		clientB = new LocalGameClient(gameServer);
		gameServer.addClient(clientA);
		gameServer.addClient(clientB);
		gameServer.initServer(infoA, infoB);
		clientA.initClient(infoA, infoB);
		clientB.initClient(infoB, infoA);
		
		gameCards = new ArrayList<CardObject>();
		opponentCards = new ArrayList<Sprite>();
		
		focusedCard = null;
		heldCard = null;
		heldOffset = new Vec2D();
		
		playerHand = new HandField();
		playerFields = new ArrayList<StageField>();
		for (int i=0;i<3;i++) {
			StageField cf = new StageField();
			cf.setPos(-60.0+60.0*i, -50.0);
			cf.setSize(cardSize);
			playerFields.add(cf);
		}
		for (int i=0;i<2;i++) {
			StageField cf = new StageField();
			cf.setPos(-30.0+60.0*i, -135.0);
			cf.setSize(cardSize);
			playerFields.add(cf);
		}
		opponentFields = new ArrayList<StageField>();
		for (int i=0;i<3;i++) {
			StageField cf = new StageField();
			cf.setPos(-60.0+60.0*i, 50.0);
			cf.setSize(cardSize);
			opponentFields.add(cf);
		}
		for (int i=0;i<2;i++) {
			StageField cf = new StageField();
			cf.setPos(-30.0+60.0*i, 135.0);
			cf.setSize(cardSize);
			opponentFields.add(cf);
		}
		
		delay = 0;
	}
	
	@Override
	public void destroy() {
		// TODO
	}
	
	@Override
	public void update(double time) {
		if (clientA.isState(GameSystem.GS_INIT) || clientA.isState(GameSystem.GS_FIRSTDRAW)) {
			if (isKeyPressed(IM_KEY_SPACE)) clientA.sendReady();
		} else if (clientA.isState(GameSystem.GS_GAME)) {
			if (clientA.isInTurn()) {
				if (clientA.isPhase(GameSystem.GP_MAIN)) {
					if (isKeyPressed(IM_KEY_SPACE)) clientA.endTurn();
				}
			}
		}
		
		if (clientB.isState(GameSystem.GS_INIT) || clientB.isState(GameSystem.GS_FIRSTDRAW)) {
			if (isKeyPressed(IM_KEY_ENTER)) clientB.sendReady();
		} else if (clientB.isState(GameSystem.GS_GAME)) {
			if (clientB.isInTurn()) {
				if (clientB.isPhase(GameSystem.GP_MAIN)) {
					if (isKeyPressed(IM_KEY_ENTER)) clientB.endTurn();
				}
			}
		}
		
		
		gameServer.update();
		clientA.update();
		clientB.update();
		
		
		if (delay == 0) {
			int drawn = clientA.getPlayer().getDrawnCard();
			if (drawn != -1) {
				CardData c = clientA.getPlayer().getCardData(drawn);
				CardObject co = new CardObject(drawn, c, new SpriteObject(cardSprites.get(c.image), 500f, 730f));
				co.setAnchor(300.0, -60.0);
				co.setPos(300.0, -60.0);
				co.setSize(cardSize);
				co.setField(playerHand);
				playerHand.addCard(co);
				gameCards.add(co);
				
				delay = 10;
			}
		} else {
			delay--;
		}
		
		
		double mouseX = viewPort.getScreenToVirtualX(InputManager.getMouseX());
		double mouseY = viewPort.getScreenToVirtualY(InputManager.getMouseY());
		
		focusedCard = null;
		for (int i=0;i<gameCards.size();i++) {
			CardObject hc = gameCards.get(i);
			hc.toggleAnchor(true);
			if (hc.isInBound(viewPort.getScreenToVirtualX(InputManager.getMouseX()), viewPort.getScreenToVirtualY(InputManager.getMouseY()))) {
				focusedCard = hc;
				if (InputManager.isMousePressed(InputManager.IM_MOUSE_BUTTON_1) && clientA.isInTurn() && clientA.isInPhase() && clientA.isPhase(GameClient.GP_MAIN)) {
					heldCard = hc;
					heldOffset.setComponents(mouseX - hc.getAnchorX(), mouseY - hc.getAnchorY());
				}
			}
		}
		
		if (heldCard != null && InputManager.isMouseReleased(InputManager.IM_MOUSE_BUTTON_1)) {
			boolean inField = false; // TODO
			for (int i=0;i<playerFields.size();i++) {
				StageField cf = playerFields.get(i);
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
			if (!inField && heldCard.getCardState() == 0) {
				CardField previousField = heldCard.getField();
				if (previousField != null) {
					if (previousField instanceof StageField) {
						StageField sf = (StageField)previousField;
						sf.setCard(null);
						heldCard.setField(playerHand);
						playerHand.addCard(heldCard);
					}
				}
			}
			heldCard = null;
		}
		if (heldCard != null) {
			heldCard.toggleAnchor(false);
			heldCard.setPos(mouseX - heldOffset.getX(), mouseY - heldOffset.getY());
		}
		
		for (int i=0;i<playerFields.size();i++) {playerFields.get(i).isInBound(mouseX, mouseY);}
		for (int i=0;i<opponentFields.size();i++) {opponentFields.get(i).isInBound(mouseX, mouseY);}
		
		playerHand.anchorCards();
		for (int i=0;i<opponentCards.size();i++) opponentCards.get(i).update();
		for (int i=0;i<gameCards.size();i++) gameCards.get(i).update();
	}
	
	@Override
	public void render(double ip) {
		shaderProgram.bind();
		shaderProgram.pushMatrix();
		viewPort.adjustView(shaderProgram);
		
		for (int i=0;i<playerFields.size();i++) playerFields.get(i).render(shaderProgram);
		for (int i=0;i<opponentFields.size();i++) opponentFields.get(i).render(shaderProgram);
		for (int i=0;i<opponentCards.size();i++) opponentCards.get(i).render(shaderProgram, ip);
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
