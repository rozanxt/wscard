package zan.wscard.panel;

import java.util.ArrayList;

import zan.lib.gfx.shader.DefaultShader;
import zan.lib.gfx.texture.TextureManager;
import zan.lib.gfx.text.TextManager;
import zan.lib.gfx.view.ViewPort2D;
import zan.lib.net.NetworkManager;
import zan.lib.util.Utility;
import zan.lib.util.math.Vec2D;
import zan.lib.core.BasePanel;
import static zan.lib.input.InputManager.*;
import zan.wscard.card.CardData;
import zan.wscard.card.CardReader;
import zan.wscard.core.GameCore;
import zan.wscard.sys.GameSystem;
import zan.wscard.sys.NetworkGameClient;
import zan.wscard.sys.NetworkGameServer;
import zan.wscard.sys.PlayerInfo;
import zan.wscard.sys.PlayerMove;
import zan.wscard.obj.CardField;
import zan.wscard.obj.CardObject;
import zan.wscard.obj.ClockField;
import zan.wscard.obj.DeckField;
import zan.wscard.obj.HandField;
import zan.wscard.obj.StageField;
import zan.wscard.obj.WaitingRoomField;
import static zan.wscard.sys.GameSystem.*;

public class GamePanel extends BasePanel {

	private DefaultShader shaderProgram;
	private ViewPort2D viewPort;

	private NetworkGameServer gameServer;
	private NetworkGameClient gameClient;

	private HandField playerHand;
	private HandField opponentHand;
	private StageField[] playerStages;
	private StageField[] opponentStages;
	private DeckField playerDeck;
	private DeckField opponentDeck;
	private WaitingRoomField playerWaitingRoom;
	private WaitingRoomField opponentWaitingRoom;
	private ClockField playerClock;
	private ClockField opponentClock;

	private ArrayList<PlayerMove> playerMoves;

	private CardObject focusedCard;
	private CardObject heldCard;
	private Vec2D heldOffset;

	// TODO
	private int mode;
	private String address;
	private int actionDelay;
	private String actionMessage;
	private int redraw;

	public GamePanel(GameCore core, int mode, String address) {
		shaderProgram = new DefaultShader();
		viewPort = new ViewPort2D(0, 0, core.getScreenWidth(), core.getScreenHeight());
		this.mode = mode;
		this.address = address;
	}

	@Override
	public void init() {
		// Initialization
		shaderProgram.loadProgram();
		shaderProgram.enableBlend(true);

		viewPort.setHeightInterval(600.0);
		viewPort.showView();
		viewPort.projectView(shaderProgram);

		TextureManager.init();
		TextManager.init();
		TextManager.loadFontFile("res/font/fonts.res");

		// Card data / image
		CardReader cr = new CardReader();
		ArrayList<CardData> LHCards = cr.loadCardData("res/card/LH.wsci");
		ArrayList<CardData> LHDeck = new ArrayList<CardData>();
		final int[] LHSetup = {4, 1, 2, 4, 2, 2, 2, 2, 2, 2, 4, 2, 4, 2, 1, 4, 2, 2, 4, 2};
		for (int i=0;i<LHSetup.length;i++) {
			for (int j=0;j<LHSetup[i];j++) {
				LHDeck.add(LHCards.get(i));
			}
		}
		ArrayList<CardData> PDCards = cr.loadCardData("res/card/PD.wsci");
		ArrayList<CardData> PDDeck = new ArrayList<CardData>();
		final int[] PDSetup = {4, 1, 2, 4, 2, 2, 2, 2, 2, 2, 4, 2, 4, 2, 1, 4, 2, 2, 4, 2};
		for (int i=0;i<PDSetup.length;i++) {
			for (int j=0;j<PDSetup[i];j++) {
				PDDeck.add(PDCards.get(i));
			}
		}

		TextureManager.loadTexture("CARDBACK", "res/img/card/cardback.jpg");
		for (int i=0;i<LHCards.size();i++) {
			CardData c = LHCards.get(i);
			TextureManager.loadTexture(c.id, c.image);
		}
		for (int i=0;i<PDCards.size();i++) {
			CardData c = PDCards.get(i);
			TextureManager.loadTexture(c.id, c.image);
		}

		// Server & Client
		PlayerInfo infoA = new PlayerInfo("Player A", LHDeck);
		PlayerInfo infoB = new PlayerInfo("Player B", PDDeck);

		int port = 3276;
		if (mode == 1) {
			if (NetworkManager.openServer(port, 2)) {
				if (NetworkManager.openClient(address, port)) {
					gameServer = new NetworkGameServer();
					gameServer.initServer(infoA, infoB);
					gameClient = new NetworkGameClient();
					gameClient.initClient(infoA, infoB);
				} else {
					NetworkManager.closeServer();
				}
			}
		} else if (mode == 2) {
			if (NetworkManager.openClient(address, port)) {
				gameServer = null;
				gameClient = new NetworkGameClient();
				gameClient.initClient(infoB, infoA);
			}
		}

		// Fields
		playerHand = new HandField();
		playerHand.setPos(0.0, -240.0);
		opponentHand = new HandField();
		opponentHand.setPos(0.0, 240.0);
		playerStages = new StageField[5];
		for (int i=0;i<5;i++) {
			playerStages[i] = new StageField(i);
			if (i < 3) playerStages[i].setPos(-60.0+60.0*i, -50.0);
			else playerStages[i].setPos(-30.0+60.0*(i-3), -135.0);
		}
		opponentStages = new StageField[5];
		for (int i=0;i<5;i++) {
			opponentStages[i] = new StageField(i);
			if (i < 3) opponentStages[i].setPos(60.0-60.0*i, 50.0);
			else opponentStages[i].setPos(30.0-60.0*(i-3), 135.0);
		}
		playerDeck = new DeckField();
		playerDeck.setPos(250.0, -100.0);
		opponentDeck = new DeckField();
		opponentDeck.setPos(-250.0, 100.0);
		playerWaitingRoom = new WaitingRoomField();
		playerWaitingRoom.setPos(320.0, -100.0);
		opponentWaitingRoom = new WaitingRoomField();
		opponentWaitingRoom.setPos(-320.0, 100.0);
		// TODO
		playerClock = new ClockField();
		playerClock.setPos(-250.0, -100.0);
		opponentClock = new ClockField();
		opponentClock.setPos(250.0, 100.0);

		// Miscellaneous
		playerMoves = new ArrayList<PlayerMove>();

		focusedCard = null;
		heldCard = null;
		heldOffset = new Vec2D();

		actionDelay = 0;
		actionMessage = "";
		redraw = 0;
	}

	@Override
	public void destroy() {
		// TODO
		NetworkManager.closeClient();
		if (mode == 1) NetworkManager.closeServer();
	}

	@Override
	public void update(double time) {
		// Server & Client events
		if (gameClient.isState(GameSystem.GS_INIT)) {
			if (isKeyPressed(IM_KEY_SPACE)) gameClient.sendReady();
		} else if (gameClient.isState(GameSystem.GS_FIRSTDRAW) && gameClient.isInPhase()) {
			if (isKeyPressed(IM_KEY_SPACE)) {
				playerMoves.add(new PlayerMove(PlayerMove.MT_DRAW, redraw));
				playerMoves.add(new PlayerMove(PlayerMove.MT_ENDTURN));
				redraw = 0;
				gameClient.submitMoves(playerMoves);
				playerMoves.clear();
				gameClient.endPhase();
			}
		} else if (gameClient.isState(GameSystem.GS_GAME)) {
			if (gameClient.isInTurn()) {
				if (gameClient.isPhase(GameSystem.GP_CLOCK)) {
					if (isKeyPressed(IM_KEY_SPACE)) {
						// TODO
						gameClient.submitClock(-1);
						gameClient.nextPhase();
					}
				} else if (gameClient.isPhase(GameSystem.GP_MAIN)) {
					if (isKeyPressed(IM_KEY_SPACE)) {
						for (int i=0;i<playerStages.length;i++) if (playerStages[i].getCard() != null) playerStages[i].getCard().setCardState(1);
						gameClient.submitMoves(playerMoves);
						playerMoves.clear();
						gameClient.nextPhase();
					}
				} else if (gameClient.isPhase(GameSystem.GP_ATTACK)) {
					if (isKeyPressed(IM_KEY_SPACE)) {
						gameClient.submitMoves(playerMoves);
						playerMoves.clear();
						gameClient.nextPhase();
					}
				}
			}
		}

		if (mode == 1) gameServer.update();
		gameClient.update();

		// Action events
		if (actionDelay == 0) {
			actionMessage = "";
			String action = gameClient.getAction();
			if (action != null) {
				String[] tkns = action.split(" ");
				actionMessage = action;
				if (tkns[0].contentEquals("NEXTPHASE")) {
					gameClient.nextPhase();
					actionDelay = 50;
				} else if (tkns[0].contentEquals("STANDUP")) {
					for (int i=0;i<5;i++) {
						if (playerStages[i].hasCard()) {
							playerStages[i].getCard().setCardState(1);
						}
					}
					actionDelay = 10;
				} else if (tkns[0].contentEquals("DRAW")) {
					int drawn = Integer.parseInt(tkns[1]);
					CardData c = gameClient.getPlayer().getCardData(drawn);
					CardObject co = new CardObject(drawn, c);
					co.setPos(playerDeck.getAnchorX(), playerDeck.getAnchorY());
					co.setCardField(playerHand);
					playerHand.addCard(co);
					actionDelay = 10;
				} else if (tkns[0].contentEquals("TRIGGER")) {
					// TODO
				} else if (tkns[0].contentEquals("DAMAGE")) {
					int id = Utility.parseInt(tkns[1]);
					CardObject co = new CardObject(id, gameClient.getPlayer().getCardData(id));
					co.setPos(playerDeck.getAnchorX(), playerDeck.getAnchorY());
					playerClock.addCard(co);
					actionDelay = 10;
				} else if (tkns[0].contentEquals("REVERSE")) {
					int reversestage = Utility.parseInt(tkns[1]);
					playerStages[reversestage].getCard().setCardState(3);
					actionDelay = 10;
				} else if (tkns[0].contentEquals("CLEANUP")) {
					for (int i=0;i<5;i++) {
						if (playerStages[i].hasCard()) {
							CardObject c = playerStages[i].getCard();
							if (c.getCardState() == 3) {
								playerStages[i].setCard(null);
								c.setCardField(playerWaitingRoom);
								playerWaitingRoom.addCard(c);
							}
						}
					}
					actionDelay = 10;
				} else if (tkns[0].contentEquals("OPSTANDUP")) {
					for (int i=0;i<5;i++) {
						if (opponentStages[i].hasCard()) {
							opponentStages[i].getCard().setCardState(1);
						}
					}
					actionDelay = 10;
				} else if (tkns[0].contentEquals("OPDRAW")) {
					CardObject co = new CardObject(-1, null);
					co.setPos(opponentDeck.getAnchorX(), opponentDeck.getAnchorY());
					opponentHand.addCard(co);
					actionDelay = 10;
				} else if (tkns[0].contentEquals("OPDISCARD")) {
					int id = Integer.parseInt(tkns[1]);
					CardObject ch = opponentHand.getCard(0);
					opponentHand.removeCard(ch);
					CardObject co = new CardObject(id, gameClient.getOpponent().getCardData(id));
					co.setPos(ch.getAnchorX(), ch.getAnchorY());
					co.setCardField(opponentWaitingRoom);
					opponentWaitingRoom.addCard(co);
					actionDelay = 10;
				} else if (tkns[0].contentEquals("OPPLACE")) {
					int id = Integer.parseInt(tkns[1]);
					int stage = Integer.parseInt(tkns[2]);
					CardObject ch = opponentHand.getCard(0);
					opponentHand.removeCard(ch);
					StageField sf = opponentStages[stage];
					CardObject co = new CardObject(id, gameClient.getOpponent().getCardData(id));
					co.setPos(ch.getAnchorX(), ch.getAnchorY());
					co.setCardField(sf);
					sf.setCard(co);
					actionDelay = 30;
				} else if (tkns[0].contentEquals("OPMOVE")) {
					int start = Integer.parseInt(tkns[1]);
					int end = Integer.parseInt(tkns[2]);
					StageField pf = opponentStages[start];
					StageField sf = opponentStages[end];
					CardObject cp = pf.getCard();
					CardObject cs = sf.getCard();
					if (cs != null) cs.setCardField(pf);
					pf.setCard(cs);
					if (cp != null) cp.setCardField(sf);
					sf.setCard(cp);
					actionDelay = 30;
				} else if (tkns[0].contentEquals("OPCLOCK")) {
					int id = Integer.parseInt(tkns[1]);
					if (id != -1) {
						CardObject ch = opponentHand.getCard(0);
						opponentHand.removeCard(ch);
						CardObject co = new CardObject(id, gameClient.getOpponent().getCardData(id));
						co.setPos(ch.getAnchorX(), ch.getAnchorY());
						co.setCardField(opponentClock);
						opponentClock.addCard(co);
						actionDelay = 30;
					}
				} else if (tkns[0].contentEquals("OPATTACK")) {
					// TODO int attacktype = Utility.parseInt(tkns[1]);
					int attackstage = Utility.parseInt(tkns[2]);
					opponentStages[attackstage].getCard().setCardState(2);
					actionDelay = 10;
				} else if (tkns[0].contentEquals("OPTRIGGER")) {
					// TODO
				} else if (tkns[0].contentEquals("OPDAMAGE")) {
					int id = Utility.parseInt(tkns[1]);
					CardObject co = new CardObject(id, gameClient.getOpponent().getCardData(id));
					co.setPos(opponentDeck.getAnchorX(), opponentDeck.getAnchorY());
					opponentClock.addCard(co);
					actionDelay = 10;
				} else if (tkns[0].contentEquals("OPREVERSE")) {
					int reversestage = Utility.parseInt(tkns[1]);
					opponentStages[reversestage].getCard().setCardState(3);
					actionDelay = 10;
				} else if (tkns[0].contentEquals("OPCLEANUP")) {
					for (int i=0;i<5;i++) {
						if (opponentStages[i].hasCard()) {
							CardObject c = opponentStages[i].getCard();
							if (c.getCardState() == 3) {
								opponentStages[i].setCard(null);
								c.setCardField(opponentWaitingRoom);
								opponentWaitingRoom.addCard(c);
							}
						}
					}
					actionDelay = 10;
				}
			}
		} else {
			actionDelay--;
		}

		// Drag & Drop events
		double mouseX = viewPort.getScreenToVirtualX(getMouseX());
		double mouseY = viewPort.getScreenToVirtualY(getMouseY());

		if (gameClient.isState(GS_FIRSTDRAW)) {
			ArrayList<CardObject> playerCards = new ArrayList<CardObject>();
			playerCards.addAll(playerHand.getCards());

			focusedCard = null;
			for (int i=0;i<playerCards.size();i++) {
				if (playerCards.get(i).isInBound(mouseX, mouseY)) focusedCard = playerCards.get(i);
			}

			if (isMousePressed(IM_MOUSE_BUTTON_1)) {
				if (gameClient.isInPhase()) {
					for (int i=0;i<playerCards.size();i++) {
						CardObject hc = playerCards.get(i);
						if (hc.isInBound(mouseX, mouseY)) {
							heldCard = hc;
							heldCard.toggleHeld(true);
							heldCard.toggleAnchor(false);
							heldOffset.setComponents(mouseX - hc.getAnchorX(), mouseY - hc.getAnchorY());
						}
					}
				}
			}

			if (heldCard != null) {
				heldCard.setPos(mouseX - heldOffset.getX(), mouseY - heldOffset.getY());
				if (isMouseReleased(IM_MOUSE_BUTTON_1)) {
					if (gameClient.isInPhase()) {
						CardField previousField = heldCard.getCardField();
						if (previousField != null) {
							if (previousField instanceof HandField) {
								if (playerWaitingRoom.isInBound(mouseX, mouseY)) {
									HandField hf = (HandField)previousField;
									hf.removeCard(heldCard);
									heldCard.setCardField(playerWaitingRoom);
									playerWaitingRoom.addCard(heldCard);
									playerMoves.add(new PlayerMove(PlayerMove.MT_DISCARD, heldCard.getCardID()));
									redraw++;
								}
							}
						}
					}
					heldCard.toggleHeld(false);
					heldCard.toggleAnchor(true);
					heldCard = null;
				}
			}
		} else if (gameClient.isState(GS_GAME)) {
			if (gameClient.isPhase(GP_CLOCK)) {
				ArrayList<CardObject> playerCards = new ArrayList<CardObject>();
				playerCards.addAll(playerHand.getCards());

				focusedCard = null;
				for (int i=0;i<playerCards.size();i++) {
					if (playerCards.get(i).isInBound(mouseX, mouseY)) focusedCard = playerCards.get(i);
				}

				if (isMousePressed(IM_MOUSE_BUTTON_1)) {
					if (gameClient.isInTurn() && gameClient.isInPhase()) {
						for (int i=0;i<playerCards.size();i++) {
							CardObject hc = playerCards.get(i);
							if (hc.isInBound(mouseX, mouseY)) {
								heldCard = hc;
								heldCard.toggleHeld(true);
								heldCard.toggleAnchor(false);
								heldOffset.setComponents(mouseX - hc.getAnchorX(), mouseY - hc.getAnchorY());
							}
						}
					}
				}

				if (heldCard != null) {
					heldCard.setPos(mouseX - heldOffset.getX(), mouseY - heldOffset.getY());
					if (isMouseReleased(IM_MOUSE_BUTTON_1)) {
						if (gameClient.isInTurn() && gameClient.isInPhase()) {
							CardField previousField = heldCard.getCardField();
							if (previousField != null) {
								if (previousField instanceof HandField) {
									if (playerClock.isInBound(mouseX, mouseY)) {
										HandField hf = (HandField)previousField;
										hf.removeCard(heldCard);
										heldCard.setCardField(playerClock);
										playerClock.addCard(heldCard);
										// TODO
										gameClient.submitClock(heldCard.getCardID());
										gameClient.nextPhase();
									}
								}
							}
						}
						heldCard.toggleHeld(false);
						heldCard.toggleAnchor(true);
						heldCard = null;
					}
				}
			} else if (gameClient.isPhase(GP_MAIN)) {
				ArrayList<CardObject> playerCards = new ArrayList<CardObject>();
				playerCards.addAll(playerHand.getCards());
				for (int i=0;i<playerStages.length;i++) if (playerStages[i].hasCard()) playerCards.add(playerStages[i].getCard());

				focusedCard = null;
				for (int i=0;i<playerCards.size();i++) {
					if (playerCards.get(i).isInBound(mouseX, mouseY)) focusedCard = playerCards.get(i);
				}

				if (isMousePressed(IM_MOUSE_BUTTON_1)) {
					if (gameClient.isInTurn() && gameClient.isInPhase()) {
						for (int i=0;i<playerCards.size();i++) {
							CardObject hc = playerCards.get(i);
							if (hc.isInBound(mouseX, mouseY)) {
								heldCard = hc;
								heldCard.toggleHeld(true);
								heldCard.toggleAnchor(false);
								heldOffset.setComponents(mouseX - hc.getAnchorX(), mouseY - hc.getAnchorY());
							}
						}
					}
				}

				if (heldCard != null) {
					heldCard.setPos(mouseX - heldOffset.getX(), mouseY - heldOffset.getY());
					if (isMouseReleased(IM_MOUSE_BUTTON_1)) {
						if (gameClient.isInTurn() && gameClient.isInPhase()) {
							CardField previousField = heldCard.getCardField();
							if (previousField != null) {
								if (previousField instanceof HandField) {
									for (int i=0;i<playerStages.length;i++) {
										StageField sf = playerStages[i];
										if (sf.isInBound(mouseX, mouseY)) {
											if (sf.getCard() == null) {
												HandField hf = (HandField)previousField;
												hf.removeCard(heldCard);
												heldCard.setCardField(sf);
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
											if (sf.getCard() != null) sf.getCard().setCardField(pf);
											pf.setCard(sf.getCard());
											heldCard.setCardField(sf);
											sf.setCard(heldCard);
											playerMoves.add(new PlayerMove(PlayerMove.MT_MOVE, pf.getStageID(), sf.getStageID()));
											isMoved = true;
										}
									}
									if (heldCard.getCardState() == 0 && !isMoved) {
										StageField pf = (StageField)previousField;
										pf.setCard(null);
										heldCard.setCardField(playerHand);
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
						}
						heldCard.toggleHeld(false);
						heldCard.toggleAnchor(true);
						heldCard = null;
					}
				}
			} else if (gameClient.isPhase(GP_ATTACK)) {
				ArrayList<CardObject> playerCards = new ArrayList<CardObject>();
				playerCards.addAll(playerHand.getCards());
				for (int i=0;i<playerStages.length;i++) if (playerStages[i].hasCard()) playerCards.add(playerStages[i].getCard());
				for (int i=0;i<opponentStages.length;i++) if (opponentStages[i].hasCard()) playerCards.add(opponentStages[i].getCard());

				focusedCard = null;
				for (int i=0;i<playerCards.size();i++) {
					if (playerCards.get(i).isInBound(mouseX, mouseY)) focusedCard = playerCards.get(i);
				}

				if (isMouseReleased(IM_MOUSE_BUTTON_1)) {
					if (gameClient.isInTurn() && gameClient.isInPhase()) {
						heldCard = null;
						for (int i=0;i<3;i++) {
							CardObject hc = playerStages[i].getCard();
							if (hc != null) {
								if (hc.getCardState() == 1 && hc.isInBound(mouseX, mouseY)) {
									heldCard = hc;
								}
							}
						}
					}
				}

				if (heldCard != null && heldCard.getCardField() instanceof StageField) {
					StageField sf = (StageField)heldCard.getCardField();
					int sid = sf.getStageID();
					if (opponentStages[2-sid].hasCard()) {
						if (isKeyReleased(IM_KEY_1)) {
							heldCard.setCardState(2);
							playerMoves.add(new PlayerMove(PlayerMove.MT_ATTACK, 1, sid));
							heldCard = null;
						} else if (isKeyReleased(IM_KEY_2)) {
							heldCard.setCardState(2);
							playerMoves.add(new PlayerMove(PlayerMove.MT_ATTACK, 2, sid));
							heldCard = null;
						}
					} else {
						if (isKeyReleased(IM_KEY_0)) {
							heldCard.setCardState(2);
							playerMoves.add(new PlayerMove(PlayerMove.MT_ATTACK, 0, sid));
							heldCard = null;
						}
					}
				}
			}
		}

		// Update events
		playerHand.update();
		opponentHand.update();
		for (int i=0;i<playerStages.length;i++) playerStages[i].update();
		for (int i=0;i<opponentStages.length;i++) opponentStages[i].update();
		playerDeck.update();
		opponentDeck.update();
		playerWaitingRoom.update();
		opponentWaitingRoom.update();
		playerClock.update();
		opponentClock.update();

		// Highlight events
		for (int i=0;i<playerStages.length;i++) playerStages[i].highlight(mouseX, mouseY);
		for (int i=0;i<opponentStages.length;i++) opponentStages[i].highlight(mouseX, mouseY);
		playerDeck.highlight(mouseX, mouseY);
		opponentDeck.highlight(mouseX, mouseY);
		playerWaitingRoom.highlight(mouseX, mouseY);
		opponentWaitingRoom.highlight(mouseX, mouseY);
		playerClock.highlight(mouseX, mouseY);
		opponentClock.highlight(mouseX, mouseY);
	}

	@Override
	public void render(double ip) {
		shaderProgram.bind();
		shaderProgram.pushMatrix();
		viewPort.adjustView(shaderProgram);

		// Render fields
		opponentClock.renderField(shaderProgram, ip);
		playerClock.renderField(shaderProgram, ip);
		opponentDeck.renderField(shaderProgram, ip);
		playerDeck.renderField(shaderProgram, ip);
		opponentWaitingRoom.renderField(shaderProgram, ip);
		playerWaitingRoom.renderField(shaderProgram, ip);
		for (int i=0;i<opponentStages.length;i++) opponentStages[i].renderField(shaderProgram, ip);
		for (int i=0;i<playerStages.length;i++) playerStages[i].renderField(shaderProgram, ip);
		opponentHand.renderField(shaderProgram, ip);
		playerHand.renderField(shaderProgram, ip);

		// Render cards
		opponentClock.renderCards(shaderProgram, ip);
		playerClock.renderCards(shaderProgram, ip);
		opponentDeck.renderCards(shaderProgram, ip);
		playerDeck.renderCards(shaderProgram, ip);
		opponentWaitingRoom.renderCards(shaderProgram, ip);
		playerWaitingRoom.renderCards(shaderProgram, ip);
		for (int i=0;i<opponentStages.length;i++) opponentStages[i].renderCards(shaderProgram, ip);
		for (int i=0;i<playerStages.length;i++) playerStages[i].renderCards(shaderProgram, ip);
		opponentHand.renderCards(shaderProgram, ip);
		playerHand.renderCards(shaderProgram, ip);
		if (heldCard != null) heldCard.render(shaderProgram, ip);

		// Render text
		shaderProgram.setColor(1.0, 1.0, 1.0, 1.0);
		if (focusedCard != null) {
			shaderProgram.pushMatrix();
			shaderProgram.translate(-400.0, 290.0, 0.0);
			shaderProgram.scale(10.0, 10.0, 1.0);
			TextManager.renderText(shaderProgram, focusedCard.getCardData().name + " " + focusedCard.getCardData().power, "defont");
			shaderProgram.popMatrix();
		}
		if (!actionMessage.isEmpty()) {
			shaderProgram.pushMatrix();
			shaderProgram.translate(200.0, 290.0, 0.0);
			shaderProgram.scale(10.0, 10.0, 1.0);
			TextManager.renderText(shaderProgram, actionMessage, "defont");
			shaderProgram.popMatrix();
		}
		if (mode == 1) {
			if (NetworkManager.isClientOnline(0)) {
				shaderProgram.pushMatrix();
				shaderProgram.translate(300.0, 290.0, 0.0);
				shaderProgram.scale(10.0, 10.0, 1.0);
				TextManager.renderText(shaderProgram, "ON1", "defont");
				shaderProgram.popMatrix();
			}
			if (NetworkManager.isClientOnline(1)) {
				shaderProgram.pushMatrix();
				shaderProgram.translate(350.0, 290.0, 0.0);
				shaderProgram.scale(10.0, 10.0, 1.0);
				TextManager.renderText(shaderProgram, "ON2", "defont");
				shaderProgram.popMatrix();
			}
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
