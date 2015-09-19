package zan.wscard.gui;

import java.util.ArrayList;

import zan.lib.gfx.obj.VertexObject;
import zan.lib.gfx.shader.DefaultShader;
import zan.lib.gfx.text.TextManager;
import zan.lib.util.Utility;
import zan.lib.util.math.Vec2D;
import zan.wscard.obj.CardField;
import zan.wscard.obj.CardObject;
import zan.wscard.obj.ClockField;
import zan.wscard.obj.DeckField;
import zan.wscard.obj.HandField;
import zan.wscard.obj.LevelField;
import zan.wscard.obj.StageField;
import zan.wscard.obj.StockField;
import zan.wscard.obj.WaitingRoomField;
import zan.wscard.sys.GameClient;
import zan.wscard.sys.PlayerAction;
import static org.lwjgl.opengl.GL11.GL_TRIANGLE_FAN;
import static zan.wscard.sys.GameSystem.*;
import static zan.wscard.obj.CardObject.*;
import static zan.lib.input.InputManager.*;

public class GameGUI {

	private GameClient gameClient;

	private DeckField playerDeck, opponentDeck;
	private WaitingRoomField playerWaitingRoom, opponentWaitingRoom;
	private ClockField playerClock, opponentClock;
	private LevelField playerLevel, opponentLevel;
	private StockField playerStock, opponentStock;
	private HandField playerHand, opponentHand;
	private StageField[] playerStages, opponentStages;

	private ArrayList<CardObject> activeCards = new ArrayList<CardObject>();
	private CardObject hoveredCard = null;
	private CardObject selectedCard = null;
	private CardObject focusedCard = null;
	private CardObject heldCard = null;
	private Vec2D heldOffset = new Vec2D(0.0, 0.0);

	private ArrayList<PlayerAction> playerActions = new ArrayList<PlayerAction>();

	private VertexObject selected;

	private int actionDelay = 0;

	private double mouseX = 0.0;
	private double mouseY = 0.0;

	private int winner = PL_NONE;

	public GameGUI(GameClient client) {
		gameClient = client;

		playerDeck = new DeckField(250.0, -100.0);
		opponentDeck = new DeckField(-250.0, 100.0);

		playerWaitingRoom = new WaitingRoomField(320.0, -100.0);
		opponentWaitingRoom = new WaitingRoomField(-320.0, 100.0);

		playerClock = new ClockField(-250.0, -135.0);
		opponentClock = new ClockField(250.0, 135.0);

		playerLevel = new LevelField(-250.0, -40.0);
		opponentLevel = new LevelField(250.0, 40.0);

		playerStock = new StockField(180.0, -100.0);
		opponentStock = new StockField(-180.0, 100.0);

		playerHand = new HandField(0.0, -240.0);
		opponentHand = new HandField(0.0, 240.0);

		playerStages = new StageField[5];
		for (int i=0;i<5;i++) {
			if (i < 3) playerStages[i] = new StageField(i, -60.0+60.0*i, -50.0);
			else playerStages[i] = new StageField(i, -30.0+60.0*(i-3), -135.0);
		}
		opponentStages = new StageField[5];
		for (int i=0;i<5;i++) {
			if (i < 3) opponentStages[i] = new StageField(i, 60.0-60.0*i, 50.0);
			else opponentStages[i] = new StageField(i, 30.0-60.0*(i-3), 135.0);
		}

		final int[] ind = {0, 1, 2, 3};
		final float[] ver = {
			-0.5f*(float)CardObject.cardRatio, -0.5f,
			0.5f*(float)CardObject.cardRatio, -0.5f,
			0.5f*(float)CardObject.cardRatio, 0.5f,
			-0.5f*(float)CardObject.cardRatio, 0.5f,
		};
		selected = new VertexObject(ver, ind, 2, 0, 0, 0, GL_TRIANGLE_FAN);
	}

	public void destroy() {
		// TODO
	}

	// User Interface

	private void onInit() {
		if (isKeyPressed(IM_KEY_SPACE)) {
			gameClient.sendToServer(MSG_READY);
		}
	}

	private void onFirstDraw() {
		if (gameClient.isSubPhase(SP_START)) {
			gameClient.sendToServer(MSG_REQUEST, REQ_DRAW, 5);
			gameClient.setSubPhase(SP_FIRSTDRAW_DISCARD);
		} else if (gameClient.isSubPhase(SP_FIRSTDRAW_DISCARD)) {
			if (heldCard == null) {
				if (isKeyPressed(IM_KEY_SPACE)) {
					// Submits discarded cards in first draw
					int redraw = playerActions.size();
					gameClient.submitActions(playerActions);
					if (redraw > 0) gameClient.sendToServer(MSG_REQUEST, REQ_DRAW, redraw);
					else gameClient.sendToServer(MSG_ACTION, ACT_ENDTURN);
					gameClient.setSubPhase(SP_END);
				} else if (isMousePressed(IM_MOUSE_BUTTON_1)) {
					checkDragFromHand();
				}
			} else {
				holdCard();
				if (isMouseReleased(IM_MOUSE_BUTTON_1)) {
					if (playerWaitingRoom.isInBound(mouseX, mouseY)) {
						// Discards a card
						playerHand.removeCard(heldCard);
						playerWaitingRoom.addCard(heldCard);
						activeCards.remove(heldCard);
						playerActions.add(new PlayerAction(ACT_DISCARDFROMHAND, heldCard.getCardID()));
					}
					dropCard();
				}
			}
		}
	}

	private void onEnd() {

	}

	private void onStandUpPhase() {
		if (gameClient.isSubPhase(SP_START)) {
			gameClient.actStandUp();
		}
	}

	private void onDrawPhase() {
		if (gameClient.isSubPhase(SP_START)) {
			gameClient.actDraw();
		}
	}

	private void onClockPhase() {
		if (gameClient.isSubPhase(SP_START)) {
			if (heldCard == null) {
				if (isKeyPressed(IM_KEY_SPACE)) {
					// Skips clocking
					gameClient.sendPhase(GP_MAIN);
				} else if (isMousePressed(IM_MOUSE_BUTTON_1)) {
					checkDragFromHand();
				}
			} else {
				holdCard();
				if (isMouseReleased(IM_MOUSE_BUTTON_1)) {
					if (playerClock.isInBound(mouseX, mouseY)) {
						// Clocks a card
						playerHand.removeCard(heldCard);
						playerClock.addCard(heldCard);
						activeCards.remove(heldCard);
						gameClient.sendToServer(MSG_ACTION, ACT_CLOCKFROMHAND, heldCard.getCardID());
						gameClient.sendToServer(MSG_REQUEST, REQ_DRAW, 2);
						gameClient.setSubPhase(SP_END);
					}
					dropCard();
				}
			}
		}
	}

	private void onMainPhase() {
		if (gameClient.isSubPhase(SP_START)) {
			if (heldCard == null) {
				if (isKeyPressed(IM_KEY_SPACE)) {
					// Ends main phase
					for (int i=0;i<playerStages.length;i++) if (playerStages[i].getCard() != null) playerStages[i].getCard().setCardState(1);
					gameClient.submitActions(playerActions);
					gameClient.sendPhase(GP_ATTACK);
				} else if (isMousePressed(IM_MOUSE_BUTTON_1)) {
					checkDragFromHand();
					checkDragFromStage();
				}
			} else {
				holdCard();
				if (isMouseReleased(IM_MOUSE_BUTTON_1)) {
					CardField previousField = heldCard.getCardField();
					if (previousField instanceof HandField) {
						// Places a card from hand on stage
						for (int i=0;i<playerStages.length;i++) {
							StageField sf = playerStages[i];
							if (sf.isInBound(mouseX, mouseY) && sf.getCard() == null) {
								playerHand.removeCard(heldCard);
								sf.setCard(heldCard);
								playerActions.add(new PlayerAction(ACT_PLACEFROMHAND, heldCard.getCardID(), sf.getStageID()));
								break;
							}
						}
					} else if (previousField instanceof StageField) {
						// Swaps two cards on stage / Moves a card from a stage to another stage
						boolean any = false;
						for (int i=0;i<playerStages.length;i++) {
							StageField sf = playerStages[i];
							if (sf.isInBound(mouseX, mouseY)) {
								StageField pf = (StageField)previousField;
								pf.setCard(sf.getCard());
								sf.setCard(heldCard);
								playerActions.add(new PlayerAction(ACT_SWAPONSTAGE, pf.getStageID(), sf.getStageID()));
								any = true;
								break;
							}
						}
						// Returns a card from stage to hand / Cancels a card placement
						if (!any && heldCard.getCardState() == CS_NONE) {
							StageField pf = (StageField)previousField;
							pf.setCard(null);
							playerHand.addCard(heldCard);

							for (int i=0;i<playerActions.size();i++) {
								if (playerActions.get(i).type == ACT_PLACEFROMHAND && playerActions.get(i).actions[0] == heldCard.getCardID()) {
									playerActions.remove(i);
									break;
								}
							}
						}
					}
					dropCard();
				}
			}
		}
	}

	private void onAttackPhase() {
		if (gameClient.isSubPhase(SP_START)) {
			if (isMousePressed(IM_MOUSE_BUTTON_1)) {
				// Selects a card from the center stage
				selectedCard = null;
				for (int i=0;i<3;i++) {
					CardObject sc = playerStages[i].getCard();
					if (sc != null) {
						if (sc.getCardState() == CS_STAND && sc.isInBound(mouseX, mouseY)) {
							selectedCard = sc;
							break;
						}
					}
				}
			}

			if (selectedCard != null) {
				StageField sf = (StageField)selectedCard.getCardField();
				int sid = sf.getStageID();
				if (opponentStages[2-sid].hasCard()) {
					if (isKeyPressed(IM_KEY_1)) {
						// Submits frontal attack
						selectedCard.setCardState(2);
						selectedCard = null;
						gameClient.submitAttack(ATK_FRONTAL, sid);
					} else if (isKeyPressed(IM_KEY_2)) {
						// Submits side attack
						selectedCard.setCardState(2);
						selectedCard = null;
						gameClient.submitAttack(ATK_SIDE, sid);
					}
				} else {
					if (isKeyPressed(IM_KEY_0)) {
						// Submits direct attack
						selectedCard.setCardState(2);
						selectedCard = null;
						gameClient.submitAttack(ATK_DIRECT, sid);
					}
				}
			}

			if (isKeyPressed(IM_KEY_SPACE)) {
				// Ends attack phase
				selectedCard = null;
				gameClient.sendPhase(GP_END);
			}
		}
	}

	private void onEndPhase() {
		if (gameClient.isSubPhase(SP_START)) {
			gameClient.actCleanUp();
		}
	}

	public void doUserInterface() {
		if (isKeyPressed(IM_KEY_P)) gameClient.sendToServer(MSG_PING);

		if (gameClient.isState(GS_INIT)) {
			onInit();
		} else if (gameClient.isState(GS_FIRSTDRAW)) {
			onFirstDraw();
		} else if (gameClient.isState(GS_GAME)) {
			if (gameClient.isInTurn()) {
				if (gameClient.isPhase(GP_WAIT)) {

				} else if (gameClient.isPhase(GP_STANDUP)) {
					onStandUpPhase();
				} else if (gameClient.isPhase(GP_DRAW)) {
					onDrawPhase();
				} else if (gameClient.isPhase(GP_CLOCK)) {
					onClockPhase();
				} else if (gameClient.isPhase(GP_MAIN)) {
					onMainPhase();
				} else if (gameClient.isPhase(GP_ATTACK)) {
					onAttackPhase();
				} else if (gameClient.isPhase(GP_END)) {
					onEndPhase();
				}
			}
		} else if (gameClient.isState(GS_END)) {
			onEnd();
		}
	}

	// Frequently used methods

	private void dragCard(CardObject card) {
		heldCard = card;
		heldCard.toggleHeld(true);
		heldCard.toggleAnchor(false);
		heldOffset.setComponents(mouseX - heldCard.getAnchorX(), mouseY - heldCard.getAnchorY());
	}
	private void dropCard() {
		heldCard.toggleHeld(false);
		heldCard.toggleAnchor(true);
		heldCard = null;
	}
	private void holdCard() {
		heldCard.setPos(mouseX - heldOffset.getX(), mouseY - heldOffset.getY());
	}

	private boolean checkDragFromHand() {
		if (heldCard != null) return false;
		for (int i=0;i<playerHand.getNumCards();i++) {
			if (playerHand.getCard(i).isInBound(mouseX, mouseY)) {
				dragCard(playerHand.getCard(i));
				return true;
			}
		}
		return false;
	}
	private boolean checkDragFromStage() {
		if (heldCard != null) return false;
		for (int i=0;i<playerStages.length;i++) {
			if (playerStages[i].hasCard() && playerStages[i].isInBound(mouseX, mouseY)) {
				dragCard(playerStages[i].getCard());
				return true;
			}
		}
		return false;
	}
	private boolean checkDragFromClock() {
		if (heldCard != null) return false;
		for (int i=6;i>=0;i--) {
			if (playerClock.getCard(i).isInBound(mouseX, mouseY)) {
				dragCard(playerClock.getCard(i));
				return true;
			}
		}
		return false;
	}

	// Action Events

	private void doActionEvent(String[] tkns) {	// TODO TRIGGER, RESHUFFLE
		if (tkns[0].contentEquals("WAIT")) {
			int wait = Utility.parseInt(tkns[1]);
			actionDelay = wait;
		} else if (tkns[0].contentEquals("PHASE")) {
			int phase = Utility.parseInt(tkns[1]);
			gameClient.sendPhase(phase);
		} else if (tkns[0].contentEquals("ENDTURN")) {
			gameClient.sendToServer(MSG_ACTION, ACT_ENDTURN);
		} else if (tkns[0].contentEquals("STANDUP")) {
			for (int i=0;i<playerStages.length;i++) {
				if (playerStages[i].hasCard()) {
					playerStages[i].getCard().setCardState(CS_STAND);
				}
			}
		} else if (tkns[0].contentEquals("DRAW")) {
			int id = Utility.parseInt(tkns[1]);
			CardObject card = new CardObject(id, gameClient.getPlayer().getCardData(id));
			card.setPos(playerDeck.getAnchorX(), playerDeck.getAnchorY());
			playerHand.addCard(card);
			activeCards.add(card);
			actionDelay = 10;
		} else if (tkns[0].contentEquals("RESHUFFLE")) {
			playerWaitingRoom.clearWaitingRoom();
			actionDelay = 50;
		} else if (tkns[0].contentEquals("RESHUFFLECOST")) {
			int id = Utility.parseInt(tkns[1]);
			CardObject card = new CardObject(id, gameClient.getPlayer().getCardData(id));
			card.setPos(playerDeck.getAnchorX(), playerDeck.getAnchorY());
			playerClock.addCard(card);
			actionDelay = 30;
		} else if (tkns[0].contentEquals("TRIGGER")) {
			int id = Utility.parseInt(tkns[1]);
			CardObject card = new CardObject(id, gameClient.getPlayer().getCardData(id));
			card.setPos(playerDeck.getAnchorX(), playerDeck.getAnchorY());
			playerStock.addCard(card);
			actionDelay = 50;
		} else if (tkns[0].contentEquals("DAMAGE")) {
			int id = Utility.parseInt(tkns[1]);
			CardObject card = new CardObject(id, gameClient.getPlayer().getCardData(id));
			card.setPos(playerDeck.getAnchorX(), playerDeck.getAnchorY());
			playerClock.addCard(card);
			actionDelay = 20;
		} else if (tkns[0].contentEquals("CANCEL")) {	// TODO
			int cancel = Utility.parseInt(tkns[1]);
			ArrayList<CardObject> cancelled = playerClock.removeCards(cancel);
			for (int i=0;i<cancelled.size();i++) playerWaitingRoom.addCard(cancelled.get(i));
			actionDelay = 50;
		} else if (tkns[0].contentEquals("REVERSE")) {
			int stage = Utility.parseInt(tkns[1]);
			playerStages[stage].getCard().setCardState(CS_REVERSE);
			actionDelay = 30;
		} else if (tkns[0].contentEquals("CLEANUP")) {
			for (int i=0;i<playerStages.length;i++) {
				if (playerStages[i].hasCard()) {
					if (playerStages[i].getCard().getCardState() == CS_REVERSE) {
						playerWaitingRoom.addCard(playerStages[i].getCard());
						activeCards.remove(playerStages[i].getCard());
						playerStages[i].setCard(null);
					}
				}
			}
			actionDelay = 30;
		} else if (tkns[0].contentEquals("OPSTANDUP")) {
			for (int i=0;i<opponentStages.length;i++) {
				if (opponentStages[i].hasCard()) {
					opponentStages[i].getCard().setCardState(CS_STAND);
				}
			}
			actionDelay = 50;
		} else if (tkns[0].contentEquals("OPDRAW")) {
			CardObject card = new CardObject();
			card.setPos(opponentDeck.getAnchorX(), opponentDeck.getAnchorY());
			opponentHand.addCard(card);
			actionDelay = 10;
		} else if (tkns[0].contentEquals("OPRESHUFFLE")) {
			opponentWaitingRoom.clearWaitingRoom();
			actionDelay = 50;
		} else if (tkns[0].contentEquals("OPRESHUFFLECOST")) {
			int id = Utility.parseInt(tkns[1]);
			CardObject card = new CardObject(id, gameClient.getOpponent().getCardData(id));
			card.setPos(opponentDeck.getAnchorX(), opponentDeck.getAnchorY());
			opponentClock.addCard(card);
			actionDelay = 30;
		} else if (tkns[0].contentEquals("OPDISCARD")) {
			int id = Utility.parseInt(tkns[1]);
			CardObject hand = opponentHand.getCard(0);
			opponentHand.removeCard(hand);
			CardObject card = new CardObject(id, gameClient.getOpponent().getCardData(id));
			card.setPos(hand.getAnchorX(), hand.getAnchorY());
			opponentWaitingRoom.addCard(card);
			actionDelay = 20;
		} else if (tkns[0].contentEquals("OPPLACE")) {
			int id = Utility.parseInt(tkns[1]);
			int stage = Utility.parseInt(tkns[2]);
			CardObject hand = opponentHand.getCard(0);
			opponentHand.removeCard(hand);
			CardObject card = new CardObject(id, gameClient.getOpponent().getCardData(id));
			card.setPos(hand.getAnchorX(), hand.getAnchorY());
			opponentStages[stage].setCard(card);
			activeCards.add(card);
			actionDelay = 30;
		} else if (tkns[0].contentEquals("OPMOVE")) {
			int start = Utility.parseInt(tkns[1]);
			int end = Utility.parseInt(tkns[2]);
			CardObject temp = opponentStages[start].getCard();
			opponentStages[start].setCard(opponentStages[end].getCard());
			opponentStages[end].setCard(temp);
			actionDelay = 30;
		} else if (tkns[0].contentEquals("OPCLOCK")) {
			int id = Utility.parseInt(tkns[1]);
			CardObject hand = opponentHand.getCard(0);
			opponentHand.removeCard(hand);
			CardObject card = new CardObject(id, gameClient.getOpponent().getCardData(id));
			card.setPos(hand.getAnchorX(), hand.getAnchorY());
			opponentClock.addCard(card);
			actionDelay = 20;
		} else if (tkns[0].contentEquals("OPATTACK")) {
			// TODO int type = Utility.parseInt(tkns[1]);
			int stage = Utility.parseInt(tkns[2]);
			opponentStages[stage].getCard().setCardState(CS_REST);
			actionDelay = 40;
		} else if (tkns[0].contentEquals("OPTRIGGER")) {
			int id = Utility.parseInt(tkns[1]);
			CardObject card = new CardObject(id, gameClient.getOpponent().getCardData(id));
			card.setPos(opponentDeck.getAnchorX(), opponentDeck.getAnchorY());
			opponentStock.addCard(card);
			actionDelay = 50;
		} else if (tkns[0].contentEquals("OPDAMAGE")) {
			int id = Utility.parseInt(tkns[1]);
			CardObject card = new CardObject(id, gameClient.getOpponent().getCardData(id));
			card.setPos(opponentDeck.getAnchorX(), opponentDeck.getAnchorY());
			opponentClock.addCard(card);
			actionDelay = 20;
		} else if (tkns[0].contentEquals("OPCANCEL")) {
			int cancel = Utility.parseInt(tkns[1]);
			ArrayList<CardObject> cancelled = opponentClock.removeCards(cancel);
			for (int i=0;i<cancelled.size();i++) opponentWaitingRoom.addCard(cancelled.get(i));
			actionDelay = 50;
		} else if (tkns[0].contentEquals("OPREVERSE")) {
			int stage = Utility.parseInt(tkns[1]);
			opponentStages[stage].getCard().setCardState(CS_REVERSE);
			actionDelay = 30;
		} else if (tkns[0].contentEquals("OPCLEANUP")) {
			for (int i=0;i<opponentStages.length;i++) {
				if (opponentStages[i].hasCard()) {
					if (opponentStages[i].getCard().getCardState() == CS_REVERSE) {
						opponentWaitingRoom.addCard(opponentStages[i].getCard());
						activeCards.remove(opponentStages[i].getCard());
						opponentStages[i].setCard(null);
					}
				}
			}
			actionDelay = 30;
		} else if (tkns[0].contentEquals("OPLEVELUP")) {
			int id = Utility.parseInt(tkns[1]);
			for (int i=0;i<7;i++) {
				if (opponentClock.getCard(i).getCardID() == id) {
					opponentLevel.addCard(opponentClock.getCard(i));
				} else {
					opponentWaitingRoom.addCard(opponentClock.getCard(i));
				}
			}
			for (int i=0;i<7;i++) {
				opponentClock.removeCard(opponentClock.getCard(0));
			}
		} else if (tkns[0].contentEquals("WINNER")) {
			winner = Utility.parseInt(tkns[1]);
		} else if (tkns[0].contentEquals("")) {
			// TODO
		}
	}

	public void doActionEvents() {
		if (actionDelay <= 0) {
			String action = gameClient.getAction();
			if (action != null) doActionEvent(action.split(" "));
		} else {
			actionDelay--;
		}
	}

	// Update Events

	public void updateMousePos(double x, double y) {
		mouseX = x;
		mouseY = y;
	}

	public void update() {
		hoveredCard = null;
		for (int i=0;i<activeCards.size();i++) if (activeCards.get(i).isInBound(mouseX, mouseY)) hoveredCard = activeCards.get(i);

		playerDeck.update();
		opponentDeck.update();
		playerWaitingRoom.update();
		opponentWaitingRoom.update();
		playerClock.update();
		opponentClock.update();
		playerLevel.update();
		opponentLevel.update();
		playerStock.update();
		opponentStock.update();
		playerHand.update();
		opponentHand.update();
		for (int i=0;i<playerStages.length;i++) playerStages[i].update();
		for (int i=0;i<opponentStages.length;i++) opponentStages[i].update();

		playerDeck.highlight(mouseX, mouseY);
		opponentDeck.highlight(mouseX, mouseY);
		playerWaitingRoom.highlight(mouseX, mouseY);
		opponentWaitingRoom.highlight(mouseX, mouseY);
		playerClock.highlight(mouseX, mouseY);
		opponentClock.highlight(mouseX, mouseY);
		playerLevel.highlight(mouseX, mouseY);
		opponentLevel.highlight(mouseX, mouseY);
		playerStock.highlight(mouseX, mouseY);
		opponentStock.highlight(mouseX, mouseY);
		playerHand.highlight(mouseX, mouseY);
		opponentHand.highlight(mouseX, mouseY);
		for (int i=0;i<playerStages.length;i++) playerStages[i].highlight(mouseX, mouseY);
		for (int i=0;i<opponentStages.length;i++) opponentStages[i].highlight(mouseX, mouseY);
	}

	// Render

	public void render(DefaultShader sp, double ip) {
		playerStock.renderField(sp, ip);
		playerWaitingRoom.renderField(sp, ip);
		playerDeck.renderField(sp, ip);
		playerLevel.renderField(sp, ip);
		playerClock.renderField(sp, ip);
		for (int i=0;i<playerStages.length;i++) playerStages[i].renderField(sp, ip);
		playerHand.renderField(sp, ip);

		opponentStock.renderField(sp, ip);
		opponentWaitingRoom.renderField(sp, ip);
		opponentDeck.renderField(sp, ip);
		opponentLevel.renderField(sp, ip);
		opponentClock.renderField(sp, ip);
		for (int i=0;i<opponentStages.length;i++) opponentStages[i].renderField(sp, ip);
		opponentHand.renderField(sp, ip);

		playerStock.renderCards(sp, ip);
		playerWaitingRoom.renderCards(sp, ip);
		playerDeck.renderCards(sp, ip);
		playerLevel.renderCards(sp, ip);
		playerClock.renderCards(sp, ip);
		for (int i=0;i<playerStages.length;i++) playerStages[i].renderCards(sp, ip);
		playerHand.renderCards(sp, ip);

		opponentStock.renderCards(sp, ip);
		opponentWaitingRoom.renderCards(sp, ip);
		opponentDeck.renderCards(sp, ip);
		opponentLevel.renderCards(sp, ip);
		opponentClock.renderCards(sp, ip);
		for (int i=0;i<opponentStages.length;i++) opponentStages[i].renderCards(sp, ip);
		opponentHand.renderCards(sp, ip);

		if (heldCard != null) heldCard.render(sp, ip);

		if (selectedCard != null) {
			sp.setColor(0.0, 0.8, 0.5, 0.25);
			sp.pushMatrix();
			sp.translate(selectedCard.getAnchorX(), selectedCard.getAnchorY(), 0.0);
			sp.scale(CardObject.cardSize, CardObject.cardSize, 1.0);
			sp.applyModelMatrix();
			selected.render(sp);
			sp.popMatrix();
		}

		sp.setColor(1.0, 1.0, 1.0, 1.0);
		if (hoveredCard != null) {
			sp.pushMatrix();
			sp.translate(-400.0, -300.0, 0.0);
			sp.scale(12.0, 12.0, 1.0);
			TextManager.renderText(sp, hoveredCard.getCardData().name + " " + hoveredCard.getCardData().power, "defont");
			sp.popMatrix();
		}

		if (gameClient.isState(GS_INIT)) {
			sp.pushMatrix();
			sp.translate(-400.0, 288.0, 0.0);
			sp.scale(12.0, 12.0, 1.0);
			TextManager.renderText(sp, "Game Initialization. Press space bar to start game.", "defont");
			sp.popMatrix();
		} else if (gameClient.isState(GS_FIRSTDRAW)) {
			sp.pushMatrix();
			sp.translate(-400.0, 288.0, 0.0);
			sp.scale(12.0, 12.0, 1.0);
			if (gameClient.isSubPhase(SP_FIRSTDRAW_DISCARD)) TextManager.renderText(sp, "First Draw. Drag cards into the waiting room to discard them and redraw.", "defont");
			else TextManager.renderText(sp, "Waiting for opponent...", "defont");
			sp.popMatrix();
		} else if (gameClient.isState(GS_GAME)) {
			sp.pushMatrix();
			sp.translate(-400.0, 288.0, 0.0);
			sp.scale(12.0, 12.0, 1.0);
			if (gameClient.isInTurn()) TextManager.renderText(sp, "PLAYER'S TURN", "defont");
			else TextManager.renderText(sp, "OPPONENT'S TURN", "defont");
			sp.popMatrix();

			sp.pushMatrix();
			sp.translate(-40.0, 288.0, 0.0);
			sp.scale(12.0, 12.0, 1.0);
			if (gameClient.isPhase(GP_WAIT)) TextManager.renderText(sp, "Waiting...", "defont");
			else if (gameClient.isPhase(GP_STANDUP)) TextManager.renderText(sp, "Stand Up Phase", "defont");
			else if (gameClient.isPhase(GP_DRAW)) TextManager.renderText(sp, "Draw Phase", "defont");
			else if (gameClient.isPhase(GP_CLOCK)) TextManager.renderText(sp, "Clock Phase", "defont");
			else if (gameClient.isPhase(GP_MAIN)) TextManager.renderText(sp, "Main Phase", "defont");
			else if (gameClient.isPhase(GP_ATTACK)) TextManager.renderText(sp, "Attack Phase", "defont");
			else if (gameClient.isPhase(GP_END)) TextManager.renderText(sp, "End Phase", "defont");
			sp.popMatrix();
		} else if (gameClient.isState(GS_END) && winner != PL_NONE) {
			sp.pushMatrix();
			sp.translate(-400.0, 288.0, 0.0);
			sp.scale(12.0, 12.0, 1.0);
			if (winner == gameClient.getClientID()) TextManager.renderText(sp, "YOU HAVE WON!", "defont");
			else TextManager.renderText(sp, "YOU HAVE LOST!", "defont");
			sp.popMatrix();
		}
	}

}
