package zan.wscard.panel;

import java.util.ArrayList;
import java.util.HashMap;

import zan.lib.gfx.ShaderProgram;
import zan.lib.gfx.TextureManager;
import zan.lib.gfx.obj.SpriteObject;
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
	
	private ArrayList<CardObject> playerCards;
	private ArrayList<CardObject> opponentCards;
	private HandField playerHand;
	private HandField opponentHand;
	private ArrayList<StageField> playerFields;
	private ArrayList<StageField> opponentFields;
	
	private CardObject focusedCard;
	private CardObject heldCard;
	private Vec2D heldOffset;
	
	// TODO
	private static final double cardSize = 80.0;
	private int actionDelayA, actionDelayB;
	private String actionMessage;
	
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
		cardSprites.put("CARDBACK", TextureManager.loadTexture("CARDBACK", "res/img/card/cardback.jpg"));
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
		
		playerCards = new ArrayList<CardObject>();
		opponentCards = new ArrayList<CardObject>();
		playerHand = new HandField();
		playerHand.setPosY(-240.0);
		opponentHand = new HandField();
		opponentHand.setPosY(240.0);
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
		
		focusedCard = null;
		heldCard = null;
		heldOffset = new Vec2D();
		
		actionDelayA = 0;
		actionDelayB = 0;
		actionMessage = "";
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
					if (isKeyPressed(IM_KEY_SPACE)) clientA.nextPhase();
				}
			}
		}
		
		if (clientB.isState(GameSystem.GS_INIT) || clientB.isState(GameSystem.GS_FIRSTDRAW)) {
			if (isKeyPressed(IM_KEY_ENTER)) clientB.sendReady();
		} else if (clientB.isState(GameSystem.GS_GAME)) {
			if (clientB.isInTurn()) {
				if (clientB.isPhase(GameSystem.GP_MAIN)) {
					if (isKeyPressed(IM_KEY_ENTER)) clientB.nextPhase();
				}
			}
		}
		
		
		gameServer.update();
		clientA.update();
		clientB.update();
		
		
		if (actionDelayA == 0) {
			actionMessage = "";
			String action = clientA.getAction();
			if (action != null) {
				String[] tkns = action.split(" ");
				actionMessage = action;
				if (tkns[0].contentEquals("NEXTPHASE")) {
					clientA.nextPhase();
					actionDelayA = 50;
				} else if (tkns[0].contentEquals("DRAW")) {
					int drawn = Integer.parseInt(tkns[1]);
					CardData c = clientA.getPlayer().getCardData(drawn);
					CardObject co = new CardObject(drawn, c, new SpriteObject(cardSprites.get(c.image), 500f, 730f));
					co.setAnchor(300.0, -60.0);
					co.setPos(300.0, -60.0);
					co.setSize(cardSize);
					co.setField(playerHand);
					playerHand.addCard(co);
					playerCards.add(co);
					actionDelayA = 10;
				} else if (tkns[0].contentEquals("OPDRAW")) {
					CardObject co = new CardObject(-1, null, new SpriteObject(cardSprites.get("CARDBACK"), 500f, 730f));
					co.setAnchor(-300.0, 60.0);
					co.setPos(-300.0, 60.0);
					co.setSize(cardSize);
					co.setField(opponentHand);
					opponentHand.addCard(co);
					opponentCards.add(co);
					actionDelayA = 10;
				} else if (tkns[0].startsWith("OP")) {
					actionDelayA = 50;
				}
			}
		} else {
			actionDelayA--;
		}
		
		
		if (actionDelayB == 0) {
			String action = clientB.getAction();
			if (action != null) {
				String[] tkns = action.split(" ");
				if (tkns[0].contentEquals("NEXTPHASE")) {
					clientB.nextPhase();
					actionDelayB = 50;
				}
			}
		} else {
			actionDelayB--;
		}
		
		
		double mouseX = viewPort.getScreenToVirtualX(InputManager.getMouseX());
		double mouseY = viewPort.getScreenToVirtualY(InputManager.getMouseY());
		
		focusedCard = null;
		for (int i=0;i<playerCards.size();i++) {
			CardObject hc = playerCards.get(i);
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
		opponentHand.anchorCards();
		for (int i=0;i<playerCards.size();i++) playerCards.get(i).update();
		for (int i=0;i<opponentCards.size();i++) {
			opponentCards.get(i).toggleAnchor(true);
			opponentCards.get(i).update();
		}
	}
	
	@Override
	public void render(double ip) {
		shaderProgram.bind();
		shaderProgram.pushMatrix();
		viewPort.adjustView(shaderProgram);
		
		for (int i=0;i<playerFields.size();i++) playerFields.get(i).render(shaderProgram);
		for (int i=0;i<opponentFields.size();i++) opponentFields.get(i).render(shaderProgram);
		for (int i=0;i<playerCards.size();i++) if (heldCard != playerCards.get(i)) playerCards.get(i).render(shaderProgram, ip);
		for (int i=0;i<opponentCards.size();i++) opponentCards.get(i).render(shaderProgram, ip);
		if (heldCard != null) heldCard.render(shaderProgram, ip);
		
		if (focusedCard != null) {
			shaderProgram.pushMatrix();
			shaderProgram.translate(-400.0, 290.0, 0.0);
			shaderProgram.scale(10.0, 10.0, 1.0);
			TextManager.renderText(shaderProgram, focusedCard.getCardData().name, "defont");
			shaderProgram.popMatrix();
		}
		
		if (!actionMessage.isEmpty()) {
			shaderProgram.pushMatrix();
			shaderProgram.translate(200.0, 290.0, 0.0);
			shaderProgram.scale(10.0, 10.0, 1.0);
			TextManager.renderText(shaderProgram, actionMessage, "defont");
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
