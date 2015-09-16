package zan.wscard.gui;

import java.util.ArrayList;

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
import zan.wscard.sys.PlayerMove;
import static zan.wscard.sys.GameSystem.*;
import static zan.wscard.sys.PlayerMove.*;
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

	private ArrayList<PlayerMove> playerMoves = new ArrayList<PlayerMove>();

	private int actionDelay = 0;

	private double mouseX = 0.0;
	private double mouseY = 0.0;

	public GameGUI(GameClient client) {
		gameClient = client;

		playerDeck = new DeckField(250.0, -100.0);
		opponentDeck = new DeckField(-250.0, 100.0);

		playerWaitingRoom = new WaitingRoomField(320.0, -100.0);
		opponentWaitingRoom = new WaitingRoomField(-320.0, 100.0);

		playerClock = new ClockField(-250.0, -100.0);
		opponentClock = new ClockField(250.0, 100.0);

		playerLevel = new LevelField(-250.0, -150.0);
		opponentLevel = new LevelField(250.0, 150.0);

		playerStock = new StockField(-250.0, -50.0);
		opponentStock = new StockField(250.0, 50.0);

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
	}

	public void destroy() {
		// TODO
	}

	// User Interface

	private void onInit() {
		if (isKeyPressed(IM_KEY_SPACE)) gameClient.sendReady();
	}

	private void onFirstDraw() {
		if (heldCard == null) {
			if (isKeyPressed(IM_KEY_SPACE)) {
				// Submits first draw
				playerMoves.add(new PlayerMove(MT_DRAW, playerMoves.size()));
				playerMoves.add(new PlayerMove(MT_ENDTURN));
				submitMoves(playerMoves);
				gameClient.endPhase();
				return;
			}

			if (isMousePressed(IM_MOUSE_BUTTON_1)) checkDragFromHand();
		} else {
			holdCard();
			if (isMouseReleased(IM_MOUSE_BUTTON_1)) {
				if (playerWaitingRoom.isInBound(mouseX, mouseY)) {
					// Discards a card
					playerHand.removeCard(heldCard);
					playerWaitingRoom.addCard(heldCard);
					activeCards.remove(heldCard);
					playerMoves.add(new PlayerMove(MT_DISCARD, heldCard.getCardID()));
				}
				dropCard();
			}
		}
	}

	private void onEnd() {

	}

	private void onClockPhase() {
		if (heldCard == null) {
			if (isKeyPressed(IM_KEY_SPACE)) {
				// Skips clocking
				gameClient.nextPhase();
				return;
			}

			if (isMousePressed(IM_MOUSE_BUTTON_1)) checkDragFromHand();
		} else {
			holdCard();
			if (isMouseReleased(IM_MOUSE_BUTTON_1)) {
				if (playerClock.isInBound(mouseX, mouseY)) {
					// Clocks a card
					playerHand.removeCard(heldCard);
					playerClock.addCard(heldCard);
					activeCards.remove(heldCard);
					playerMoves.add(new PlayerMove(MT_CLOCK, heldCard.getCardID()));
					playerMoves.add(new PlayerMove(MT_DRAW, 2));
					submitMoves(playerMoves);
					gameClient.nextPhase();
				}
				dropCard();
			}
		}
	}

	private void onMainPhase() {
		if (heldCard == null) {
			if (isKeyPressed(IM_KEY_SPACE)) {
				// Ends main phase
				for (int i=0;i<playerStages.length;i++) if (playerStages[i].getCard() != null) playerStages[i].getCard().setCardState(1);
				submitMoves(playerMoves);
				gameClient.nextPhase();
				return;
			}

			if (isMousePressed(IM_MOUSE_BUTTON_1)) {
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
							playerMoves.add(new PlayerMove(MT_PLACE, heldCard.getCardID(), sf.getStageID()));
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
							playerMoves.add(new PlayerMove(PlayerMove.MT_SWAP, pf.getStageID(), sf.getStageID()));
							any = true;
							break;
						}
					}
					// Returns a card from stage to hand / Cancels a card placement
					if (!any && heldCard.getCardState() == CS_NONE) {
						StageField pf = (StageField)previousField;
						pf.setCard(null);
						playerHand.addCard(heldCard);

						for (int i=0;i<playerMoves.size();i++) {
							if (playerMoves.get(i).getType() == PlayerMove.MT_PLACE && playerMoves.get(i).getArg(0) == heldCard.getCardID()) {
								playerMoves.remove(i);
								break;
							}
						}
					}
				}
				dropCard();
			}
		}
	}

	private void onAttackPhase() {
		if (isKeyPressed(IM_KEY_SPACE)) {
			// Ends attack phase
			submitMoves(playerMoves);
			gameClient.nextPhase();
			return;
		}

		if (selectedCard == null) {
			if (isMouseReleased(IM_MOUSE_BUTTON_1)) {
				// Selects a card from the center stage
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
		} else {
			StageField sf = (StageField)selectedCard.getCardField();
			int sid = sf.getStageID();
			if (opponentStages[2-sid].hasCard()) {
				if (isKeyReleased(IM_KEY_1)) {
					// Submits frontal attack
					selectedCard.setCardState(2);
					playerMoves.add(new PlayerMove(PlayerMove.MT_ATTACK, 1, sid));
					submitMoves(playerMoves);
					selectedCard = null;
				} else if (isKeyReleased(IM_KEY_2)) {
					// Submits side attack
					selectedCard.setCardState(2);
					playerMoves.add(new PlayerMove(PlayerMove.MT_ATTACK, 2, sid));
					submitMoves(playerMoves);
					selectedCard = null;
				}
			} else {
				if (isKeyReleased(IM_KEY_0)) {
					// Submits direct attack
					selectedCard.setCardState(2);
					playerMoves.add(new PlayerMove(PlayerMove.MT_ATTACK, 0, sid));
					submitMoves(playerMoves);
					selectedCard = null;
				}
			}
			if (isMouseReleased(IM_MOUSE_BUTTON_1)) selectedCard = null;
		}
	}

	public void doUserInterface() {
		if (gameClient.isState(GS_INIT)) {
			onInit();
		} else if (gameClient.isState(GS_FIRSTDRAW)) {
			onFirstDraw();
		} else if (gameClient.isState(GS_GAME)) {
			if (gameClient.isInTurn() && gameClient.isInPhase()) {
				if (gameClient.isPhase(GP_WAIT)) {

				} else if (gameClient.isPhase(GP_STANDUP)) {

				} else if (gameClient.isPhase(GP_DRAW)) {

				} else if (gameClient.isPhase(GP_CLOCK)) {
					onClockPhase();
				} else if (gameClient.isPhase(GP_MAIN)) {
					onMainPhase();
				} else if (gameClient.isPhase(GP_ATTACK)) {
					onAttackPhase();
				} else if (gameClient.isPhase(GP_END)) {

				}
			}
		} else if (gameClient.isState(GS_END)) {
			onEnd();
		}
	}

	// Frequently used methods

	private void submitMoves(ArrayList<PlayerMove> moves) {
		gameClient.submitMoves(moves);
		moves.clear();
	}

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
			if (playerStages[i].isInBound(mouseX, mouseY)) {
				dragCard(playerStages[i].getCard());
				return true;
			}
		}
		return false;
	}

	// Action Events

	private void doActionEvent(String[] tkns) {
		if (tkns[0].contentEquals("NEXTPHASE")) {
			gameClient.nextPhase();
			actionDelay = 50;
		} else if (tkns[0].contentEquals("STANDUP")) {
			for (int i=0;i<playerStages.length;i++) {
				if (playerStages[i].hasCard()) {
					playerStages[i].getCard().setCardState(CS_STAND);
				}
			}
			actionDelay = 0;
		} else if (tkns[0].contentEquals("DRAW")) {
			int id = Utility.parseInt(tkns[1]);
			CardObject card = new CardObject(id, gameClient.getPlayer().getCardData(id));
			card.setPos(playerDeck.getAnchorX(), playerDeck.getAnchorY());
			playerHand.addCard(card);
			activeCards.add(card);
			actionDelay = 10;
		} else if (tkns[0].contentEquals("DAMAGE")) {
			int id = Utility.parseInt(tkns[1]);
			CardObject card = new CardObject(id, gameClient.getPlayer().getCardData(id));
			card.setPos(playerDeck.getAnchorX(), playerDeck.getAnchorY());
			playerClock.addCard(card);
			actionDelay = 20;
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
			actionDelay = 50;
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
		} else if (tkns[0].contentEquals("OPDAMAGE")) {
			int id = Utility.parseInt(tkns[1]);
			CardObject card = new CardObject(id, gameClient.getOpponent().getCardData(id));
			card.setPos(opponentDeck.getAnchorX(), opponentDeck.getAnchorY());
			opponentClock.addCard(card);
			actionDelay = 20;
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
			actionDelay = 50;
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
			if (gameClient.isInPhase()) TextManager.renderText(sp, "First Draw. Drag cards into the waiting room to discard them and redraw.", "defont");
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
		}
	}

}
