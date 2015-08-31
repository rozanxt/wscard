package zan.wscard.panel;

import java.util.ArrayList;

import zan.lib.gfx.ShaderProgram;
import zan.lib.gfx.texture.TextureManager;
import zan.lib.gfx.text.TextManager;
import zan.lib.gfx.view.ViewPort2D;
import zan.lib.util.math.Vec2D;
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
import zan.wscard.sys.PlayerMove;
import zan.wscard.obj.CardField;
import zan.wscard.obj.CardObject;
import zan.wscard.obj.HandField;
import zan.wscard.obj.StageField;

public class GamePanel extends BasePanel {
	
	private ShaderProgram shaderProgram;
	private ViewPort2D viewPort;
	
	private LocalGameServer gameServer;
	private LocalGameClient clientA, clientB;
	
	private ArrayList<CardObject> playerCards;
	private ArrayList<CardObject> opponentCards;
	private HandField playerHand;
	private HandField opponentHand;
	private StageField[] playerStages;
	private StageField[] opponentStages;
	
	private ArrayList<PlayerMove> playerMoves;
	
	private CardObject focusedCard;
	private CardObject heldCard;
	private Vec2D heldOffset;
	
	// TODO
	private static final double cardSize = 80.0;
	private int actionDelayA, actionDelayB;
	private String actionMessage;
	private int AIState;
	
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
		
		TextureManager.loadTexture("CARDBACK", "res/img/card/cardback.jpg");
		for (int i=0;i<testCards.size();i++) {
			CardData c = testCards.get(i);
			TextureManager.loadTexture(c.id, c.image);
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
		playerStages = new StageField[5];
		for (int i=0;i<5;i++) {
			playerStages[i] = new StageField(i);
			playerStages[i].setSize(cardSize);
			if (i < 3) playerStages[i].setPos(-60.0+60.0*i, -50.0);
			else playerStages[i].setPos(-30.0+60.0*(i-3), -135.0);
		}
		opponentStages = new StageField[5];
		for (int i=0;i<5;i++) {
			opponentStages[i] = new StageField(i);
			opponentStages[i].setSize(cardSize);
			if (i < 3) opponentStages[i].setPos(60.0-60.0*i, 50.0);
			else opponentStages[i].setPos(30.0-60.0*(i-3), 135.0);
		}
		
		playerMoves = new ArrayList<PlayerMove>();
		
		focusedCard = null;
		heldCard = null;
		heldOffset = new Vec2D();
		
		actionDelayA = 0;
		actionDelayB = 0;
		actionMessage = "";
		
		AIState = 0;
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
					if (isKeyPressed(IM_KEY_SPACE)) {
						for (int i=0;i<playerStages.length;i++) if (playerStages[i].getCard() != null) playerStages[i].getCard().setCardState(1);
						clientA.submitMoves(playerMoves);
						playerMoves.clear();
						clientA.nextPhase();
					}
				}
			}
		}
		
		if (clientB.isState(GameSystem.GS_INIT) || clientB.isState(GameSystem.GS_FIRSTDRAW)) {
			if (isKeyPressed(IM_KEY_ENTER)) clientB.sendReady();
		} else if (clientB.isState(GameSystem.GS_GAME)) {
			if (clientB.isInTurn()) {
				if (clientB.isPhase(GameSystem.GP_MAIN)) {
					if (isKeyPressed(IM_KEY_ENTER)) {
						// TODO
						ArrayList<PlayerMove> opponentMoves = new ArrayList<PlayerMove>();
						if (AIState == 0) {
							opponentMoves.add(new PlayerMove(PlayerMove.MT_PLACE, 0, 2));
							opponentMoves.add(new PlayerMove(PlayerMove.MT_PLACE, 0, 3));
							opponentMoves.add(new PlayerMove(PlayerMove.MT_MOVE, 2, 3));
							AIState++;
						} else if (AIState == 1) {
							opponentMoves.add(new PlayerMove(PlayerMove.MT_PLACE, 0, 1));
							opponentMoves.add(new PlayerMove(PlayerMove.MT_MOVE, 3, 5));
							AIState++;
						} else if (AIState == 2) {
							opponentMoves.add(new PlayerMove(PlayerMove.MT_PLACE, 0, 4));
							opponentMoves.add(new PlayerMove(PlayerMove.MT_MOVE, 1, 3));
							AIState++;
						} else if (AIState == 3) {
							opponentMoves.add(new PlayerMove(PlayerMove.MT_MOVE, 2, 4));
							opponentMoves.add(new PlayerMove(PlayerMove.MT_MOVE, 1, 3));
							opponentMoves.add(new PlayerMove(PlayerMove.MT_MOVE, 5, 1));
						}
						clientB.submitMoves(opponentMoves);
						clientB.nextPhase();
					}
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
					CardObject co = new CardObject(drawn, c, TextureManager.getTexture(c.id));
					co.setAnchor(300.0, -60.0);
					co.setPos(300.0, -60.0);
					co.setSize(cardSize);
					co.setField(playerHand);
					playerHand.addCard(co);
					playerCards.add(co);
					actionDelayA = 10;
				} else if (tkns[0].contentEquals("OPDRAW")) {
					CardObject co = new CardObject(-1, null, TextureManager.getTexture("CARDBACK"));
					co.setAnchor(-300.0, 60.0);
					co.setPos(-300.0, 60.0);
					co.setSize(cardSize);
					co.setField(opponentHand);
					opponentHand.addCard(co);
					opponentCards.add(co);
					actionDelayA = 10;
				} else if (tkns[0].contentEquals("OPPLACE")) {	// TODO
					int stage = Integer.parseInt(tkns[1]);
					StageField sf = opponentStages[stage-1];
					CardObject co = opponentHand.getCard(0);
					opponentHand.removeCard(co);
					co.setField(sf);
					sf.setCard(co);
					actionDelayA = 30;
				} else if (tkns[0].contentEquals("OPMOVE")) {	// TODO
					int start = Integer.parseInt(tkns[1]);
					int end = Integer.parseInt(tkns[2]);
					StageField pf = opponentStages[start-1];
					StageField sf = opponentStages[end-1];
					CardObject cp = pf.getCard();
					CardObject cs = sf.getCard();
					if (cs != null) cs.setField(pf);
					pf.setCard(cs);
					if (cp != null) cp.setField(sf);
					sf.setCard(cp);
					actionDelayA = 30;
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
		
		
		double mouseX = viewPort.getScreenToVirtualX(getMouseX());
		double mouseY = viewPort.getScreenToVirtualY(getMouseY());
		
		focusedCard = null;
		for (int i=0;i<playerCards.size();i++) {
			CardObject hc = playerCards.get(i);
			hc.toggleAnchor(true);
			if (hc.isInBound(mouseX, mouseY)) {
				focusedCard = hc;
				if (isMousePressed(IM_MOUSE_BUTTON_1) && hc.isInAnchor() && clientA.isInTurn() && clientA.isInPhase() && clientA.isPhase(GameClient.GP_MAIN)) {
					heldCard = hc;
					heldOffset.setComponents(mouseX - hc.getAnchorX(), mouseY - hc.getAnchorY());
				}
			}
		}
		
		if (heldCard != null) {
			heldCard.toggleAnchor(false);
			heldCard.setPos(mouseX - heldOffset.getX(), mouseY - heldOffset.getY());
			
			if (isMouseReleased(IM_MOUSE_BUTTON_1)) {
				CardField previousField = heldCard.getField();
				if (previousField != null) {
					if (previousField instanceof HandField) {
						for (int i=0;i<playerStages.length;i++) {
							StageField sf = playerStages[i];
							if (sf.isInBound(mouseX, mouseY)) {
								if (sf.getCard() == null) {
									HandField hf = (HandField)previousField;
									hf.removeCard(heldCard);
									heldCard.setField(sf);
									sf.setCard(heldCard);
									
									playerMoves.add(new PlayerMove(PlayerMove.MT_PLACE, heldCard.getCardID(), sf.getStageID()));
								}
							}
						}
					} else if (previousField instanceof StageField) {
						boolean isMoved = false;
						for (int i=0;i<playerStages.length;i++) {
							StageField sf = playerStages[i];
							if (sf.isInBound(mouseX, mouseY)) {
								StageField pf = (StageField)previousField;
								if (sf.getCard() != null) sf.getCard().setField(pf);
								pf.setCard(sf.getCard());
								heldCard.setField(sf);
								sf.setCard(heldCard);
								
								playerMoves.add(new PlayerMove(PlayerMove.MT_MOVE, pf.getStageID(), sf.getStageID()));
								isMoved = true;
							}
						}
						if (heldCard.getCardState() == 0 && !isMoved) {
							StageField pf = (StageField)previousField;
							pf.setCard(null);
							heldCard.setField(playerHand);
							playerHand.addCard(heldCard);
							
							for (int i=0;i<playerMoves.size();i++) {
								if (playerMoves.get(i).getType() == PlayerMove.MT_PLACE && playerMoves.get(i).getArg(0) == heldCard.getCardID()) {
									playerMoves.remove(i);
									break;
								}
							}
						}
					}
				}
				heldCard = null;
			}
		}
		
		playerHand.anchorCards();
		opponentHand.anchorCards();
		for (int i=0;i<playerStages.length;i++) {playerStages[i].isInBound(mouseX, mouseY);}
		for (int i=0;i<opponentStages.length;i++) {opponentStages[i].isInBound(mouseX, mouseY);}
		
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
		
		for (int i=0;i<playerStages.length;i++) playerStages[i].render(shaderProgram);
		for (int i=0;i<opponentStages.length;i++) opponentStages[i].render(shaderProgram);
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
