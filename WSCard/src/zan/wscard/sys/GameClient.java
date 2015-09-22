package zan.wscard.sys;

import java.util.ArrayList;
import java.util.Arrays;

import zan.lib.util.Utility;

public abstract class GameClient extends GameSystem {

	private int clientID = PL_NONE;

	private ArrayList<String> clientLog = new ArrayList<String>();
	private int logCount = 0;
	private boolean waitForVerification = false;
	private boolean[] ready = new boolean[PL_NUM];

	private Player player = new Player();
	private Player opponent = new Player();

	private int subPhase = SP_END;
	private int storedSubPhase = SP_WAIT;

	private int gameWinner = PL_NONE;

	private AttackInfo attackInfo = new AttackInfo();
	private ArrayList<String> actionStack = new ArrayList<String>();

	public GameClient() {
		ready[PL_A] = false;
		ready[PL_B] = false;
	}

	public void initClient(PlayerInfo infoPlayer, PlayerInfo infoOpponent) {
		player.setInfo(infoPlayer);
		opponent.setInfo(infoOpponent);
	}

	public void actPing() {
		sendToServer(MSG_PING);
	}
	public void actReady() {
		sendToServer(MSG_READY);
	}
	public void actFirstDraw() {
		sendToServer(MSG_REQUEST, REQ_DRAW, 5);
		setSubPhase(SP_FIRSTDRAW_DRAW);
	}
	public void actRedraw(ArrayList<String> redraw) {	// TODO
		for (int i=0;i<redraw.size();i++) sendToServer(redraw.get(i));
		if (redraw.size() > 0) sendToServer(MSG_REQUEST, REQ_DRAW, redraw.size());
		else stackAction(ACS_ENDTURN);
		redraw.clear();
		endPhase();
	}
	public void actStandUp() {
		sendToServer(MSG_ACTION, ACT_STANDUP);
		endPhase();
	}
	public void actDraw() {
		sendToServer(MSG_REQUEST, REQ_DRAW, 1);
		endPhase();
	}
	public void actClock(int card) {
		if (card == CARD_NONE) {
			stackAction(ACS_PHASE, GP_MAIN);
			endPhase();
		} else {
			sendToServer(MSG_ACTION, ACT_CLOCKFROMHAND, card);
			sendToServer(MSG_REQUEST, REQ_DRAW, 2);
			endPhase();
		}
	}
	public void actMain(ArrayList<String> actions) {	// TODO
		for (int i=0;i<actions.size();i++) sendToServer(actions.get(i));
		actions.clear();
		endPhase();
	}
	public void actAttack(int type, int stage) {
		sendToServer(MSG_ACTION, ACT_ATTACK_DECLARATION, type, stage);
		sendToServer(MSG_REQUEST, REQ_DRAW, 1);
		setSubPhase(SP_ATTACK_TRIGGER);
	}
	public void endAttack() {
		stackAction(ACS_PHASE, GP_ENCORE);
		endPhase();
	}
	public void actEncore(ArrayList<String> encore) {	// TODO
		StringBuilder enc = new StringBuilder().append(MSG_ACTION).append(" ").append(ACT_ENCORE);
		for (int i=0;i<encore.size();i++) enc.append(" ").append(encore.get(i));
		sendToServer(enc.toString());
		encore.clear();
		setSubPhase(SP_ENCORE);
	}
	public void actCleanUp() {
		sendToServer(MSG_ACTION, ACT_CLEANUP);
		endPhase();
	}
	public void actDiscard(ArrayList<String> discard) {	// TODO
		for (int i=0;i<discard.size();i++) sendToServer(discard.get(i));
		discard.clear();
		actEndTurn();
	}
	public void actEndTurn() {
		stackAction(ACS_WAIT, 30);
		stackAction(ACS_ENDTURN);
		endPhase();
	}
	public void actLevelUp(int card) {
		sendToServer(MSG_ACTION, ACT_LEVELUP, card);
		setSubPhase(SP_WAIT);
	}

	public void sendPhase(int phase) {
		sendToServer(MSG_PHASE, phase);
	}
	public void storeSubPhase(int phase) {	// TODO
		if (subPhase != SP_WAIT && subPhase != SP_LEVELUP) storedSubPhase = subPhase;
		setSubPhase(phase);
	}
	public void restoreSubPhase() {
		setSubPhase(storedSubPhase);
		storedSubPhase = SP_WAIT;
	}

	private void syncState(int state) {
		if (!isState(state)) {
			setState(state);
			if (isState(GS_FIRSTDRAW)) startPhase();
		}
	}
	private void syncPhase(int phase) {
		if (!isPhase(phase)) {
			setPhase(phase);
			if (isPhase(GP_ENCORE)) {
				if (storedSubPhase != SP_WAIT) storedSubPhase = SP_START;
				else startPhase();
			} else if (isInTurn()) {
				if (storedSubPhase != SP_WAIT) storedSubPhase = SP_START;
				else startPhase();
			}
		}
	}
	private void syncTurn(int turn) {
		if (!isTurn(turn)) {
			setTurn(turn);
			if (isInTurn()) stackAction(ACS_PHASE, GP_STANDUP);
		}
	}

	private void doDrawToHand(int[] cards) {
		for (int i=0;i<cards.length;i++) {
			if (cards[i] == CARD_NONE) {
				player.doReshuffle();
				stackAction(ACS_PL_RESHUFFLE);
			} else {
				player.addToHand(cards[i]);
				stackAction(ACS_PL_DRAWTOHAND, cards[i]);
			}
		}
		sendArrayToServer(MSG_ACTION, ACT_DRAWTOHAND, cards);
	}
	private void doDrawTrigger(int[] cards) {
		int trigger = CARD_NONE;
		for (int i=0;i<cards.length;i++) {
			if (cards[i] != CARD_NONE) {
				trigger = cards[i];
				break;
			}
		}
		sendToServer(MSG_ACTION, ACT_ATTACK_TRIGGER, trigger);
	}
	private void doRequestDamage() {
		sendToServer(MSG_REQUEST, REQ_DEALDAMAGE, attackInfo.getDamage());
	}
	private void doDealDamage(int[] cards) {
		sendArrayToServer(MSG_ACTION, ACT_ATTACK_DAMAGE, cards);
	}
	private void doBattle() {
		int result = attackInfo.getBattleResult();
		sendToServer(MSG_ACTION, ACT_ATTACK_BATTLE, result);
	}

	private void processAnswer(int type, int[] content) {
		if (isState(GS_FIRSTDRAW)) {
			if (type == ANS_DRAW) {
				doDrawToHand(content);
				if (isSubPhase(SP_FIRSTDRAW_DRAW)) setSubPhase(SP_FIRSTDRAW_DISCARD);
				else if (isSubPhase(SP_END)) stackAction(ACS_ENDTURN);
			}
		} else if (isState(GS_GAME)) {
			if (isInTurn()) {
				if (type == ANS_DRAW) {
					if (isPhase(GP_DRAW)) {
						doDrawToHand(content);
						stackAction(ACS_PHASE, GP_CLOCK);
					} else if (isPhase(GP_CLOCK)) {
						doDrawToHand(content);
						stackAction(ACS_PHASE, GP_MAIN);
					} else if (isPhase(GP_ATTACK) && isSubPhase(SP_ATTACK_TRIGGER)) {
						doDrawTrigger(content);
						setSubPhase(SP_ATTACK_DAMAGE);
						doRequestDamage();
					}
				} else if (type == ANS_DEALDAMAGE) {
					if (isPhase(GP_ATTACK) && isSubPhase(SP_ATTACK_DAMAGE)) {
						doDealDamage(content);
						setSubPhase(SP_ATTACK_BATTLE);
						doBattle();
					}
				}
			}
		}
	}
	private void processPlayerInfo(int info, int[] content) {
		if (info == ACT_NONE) {
			// NONE
		} else if (info == ACT_ENDTURN) {
			// NONE
		} else if (info == ACT_STANDUP) {
			player.doStandUp();
			stackAction(ACS_WAIT, 30);
			stackAction(ACS_PL_STANDUP);
			stackAction(ACS_PHASE, GP_DRAW);
		} else if (info == ACT_DRAWTOHAND) {
			// in 'drawToHand' method
		} else if (info == ACT_DISCARDFROMHAND) {
			player.removeFromHand(content[0]);
			player.addToWaitingRoom(content[0]);
			stackAction(ACS_PL_DISCARDFROMHAND, content[0]);
		} else if (info == ACT_CLOCKFROMHAND) {
			player.removeFromHand(content[0]);
			player.addToClock(content[0]);
			stackAction(ACS_PL_CLOCKFROMHAND, content[0]);
		} else if (info == ACT_PLACEFROMHAND) {
			player.removeFromHand(content[0]);
			player.placeOnStage(content[0], content[1]);
			stackAction(ACS_PL_PLACEFROMHAND, content[0], content[1]);
		} else if (info == ACT_SWAPONSTAGE) {
			player.swapOnStage(content[0], content[1]);
			stackAction(ACS_PL_SWAPONSTAGE, content[0], content[1]);
		} else if (info == ACT_MAIN_END) {
			stackAction(ACS_PHASE, GP_ATTACK);
		} else if (info == ACT_ATTACK_DECLARATION) {
			attackInfo.clear();
			attackInfo.setAttack(player, opponent);
			attackInfo.setType(content[0]);
			attackInfo.setStage(content[1]);
			player.setCardState(content[1], CS_REST);
			stackAction(ACS_PL_ATTACK_DECLARATION, content[0], content[1]);
		} else if (info == ACT_ATTACK_TRIGGER) {
			attackInfo.setTrigger(content[0]);
			player.addToStock(content[0]);
			stackAction(ACS_PL_ATTACK_TRIGGER, content[0]);
		} else if (info == ACT_ATTACK_DAMAGE) {
			if (content.length > 0) {
				if (opponent.getCardData(content[content.length-1]).type == CARD_CLIMAX) {
					int cancelled = 0;
					for (int i=0;i<content.length;i++) {
						if (content[i] == CARD_NONE) {
							opponent.doReshuffle();
							stackAction(ACS_OP_RESHUFFLE);
						} else {
							cancelled++;
							opponent.addToWaitingRoom(content[i]);
							stackAction(ACS_OP_ATTACK_DAMAGE, content[i]);
						}
					}
					stackAction(ACS_OP_ATTACK_CANCEL, cancelled);
				} else {
					for (int i=0;i<content.length;i++) {
						if (content[i] == CARD_NONE) {
							stackAction(ACS_OP_RESHUFFLE);
						} else {
							opponent.addToClock(content[i]);
							stackAction(ACS_OP_ATTACK_DAMAGE, content[i]);
						}
					}
				}
			} else {
				// TODO No damage
			}
		} else if (info == ACT_ATTACK_BATTLE) {
			if (content[0] == BTL_ATTACKER) {
				opponent.setCardState(attackInfo.getDefenderStage(), CS_REVERSE);
				stackAction(ACS_OP_REVERSE, attackInfo.getDefenderStage());
			} else if (content[0] == BTL_DEFENDER) {
				player.setCardState(attackInfo.getAttackerStage(), CS_REVERSE);
				stackAction(ACS_PL_REVERSE, attackInfo.getAttackerStage());
			} else if (content[0] == BTL_TIE) {
				opponent.setCardState(attackInfo.getDefenderStage(), CS_REVERSE);
				player.setCardState(attackInfo.getAttackerStage(), CS_REVERSE);
				stackAction(ACS_OP_REVERSE, attackInfo.getDefenderStage());
				stackAction(ACS_PL_REVERSE, attackInfo.getAttackerStage());
			}
			attackInfo.clear();
			stackAction(ACS_SUBPHASE, SP_START);
		} else if (info == ACT_ENCORE) {	// TODO Separate messages pay stock and encore
			for (int i=0;i<content.length;i++) {
				player.payStock(3);
				player.setCardState(content[i], CS_REST);
				stackAction(ACS_PL_PAYSTOCK, 3);
				stackAction(ACS_PL_ENCORE, content[i]);
			}
			stackAction(ACS_SUBPHASE, SP_CLEANUP);
		} else if (info == ACT_CLEANUP) {
			player.doCleanUp();
			stackAction(ACS_PL_CLEANUP);
			if (isInTurn()) {
				ready[PL_A] = true;
				if (ready[PL_A] && ready[PL_B]) {
					ready[PL_A] = false;
					ready[PL_B] = false;
					stackAction(ACS_PHASE, GP_END);
				}
			}
		} else if (info == ACT_LEVELUP) {
			player.doLevelUp(content[0]);
			stackAction(ACS_PL_LEVELUP, content[0]);
			if (!player.readyForLevelUp() && !opponent.readyForLevelUp()) {
				stackAction(ACS_RESTORESUBPHASE);
			}
			if (player.isDefeated()) {
				sendToServer(MSG_DEFEAT);
			}
		}

		if (!isSubPhase(SP_LEVELUP)) {
			if (player.readyForLevelUp()) {
				stackAction(ACS_STORESUBPHASE, SP_LEVELUP);
			} else if (opponent.readyForLevelUp()) {
				stackAction(ACS_STORESUBPHASE, SP_WAIT);
			}
		}
	}
	private void processOpponentInfo(int info, int[] content) {
		if (info == ACT_NONE) {
			// NONE
		} else if (info == ACT_ENDTURN) {
			// NONE
		} else if (info == ACT_STANDUP) {
			opponent.doStandUp();
			stackAction(ACS_WAIT, 30);
			stackAction(ACS_OP_STANDUP);
			stackAction(ACS_PHASE, GP_DRAW);
		} else if (info == ACT_DRAWTOHAND) {
			for (int i=0;i<content.length;i++) {
				if (content[i] == CARD_NONE) {
					player.doReshuffle();
					stackAction(ACS_OP_RESHUFFLE);
				} else {
					player.addToHand(content[i]);
					stackAction(ACS_OP_DRAWTOHAND, content[i]);
				}
			}
		} else if (info == ACT_DISCARDFROMHAND) {
			opponent.removeFromHand(content[0]);
			opponent.addToWaitingRoom(content[0]);
			stackAction(ACS_OP_DISCARDFROMHAND, content[0]);
		} else if (info == ACT_CLOCKFROMHAND) {
			opponent.removeFromHand(content[0]);
			opponent.addToClock(content[0]);
			stackAction(ACS_OP_CLOCKFROMHAND, content[0]);
		} else if (info == ACT_PLACEFROMHAND) {
			opponent.removeFromHand(content[0]);
			opponent.placeOnStage(content[0], content[1]);
			stackAction(ACS_OP_PLACEFROMHAND, content[0], content[1]);
		} else if (info == ACT_SWAPONSTAGE) {
			opponent.swapOnStage(content[0], content[1]);
			stackAction(ACS_OP_SWAPONSTAGE, content[0], content[1]);
		} else if (info == ACT_MAIN_END) {
			// NONE
		} else if (info == ACT_ATTACK_DECLARATION) {
			attackInfo.clear();
			attackInfo.setAttack(opponent, player);
			attackInfo.setType(content[0]);
			attackInfo.setStage(content[1]);
			opponent.setCardState(content[1], CS_REST);
			stackAction(ACS_OP_ATTACK_DECLARATION, content[0], content[1]);
		} else if (info == ACT_ATTACK_TRIGGER) {
			attackInfo.setTrigger(content[0]);
			opponent.addToStock(content[0]);
			stackAction(ACS_OP_ATTACK_TRIGGER, content[0]);
		} else if (info == ACT_ATTACK_DAMAGE) {
			if (content.length > 0) {
				if (player.getCardData(content[content.length-1]).type == CARD_CLIMAX) {
					int cancelled = 0;
					for (int i=0;i<content.length;i++) {
						if (content[i] == CARD_NONE) {
							player.doReshuffle();
							stackAction(ACS_PL_RESHUFFLE);
						} else {
							cancelled++;
							player.addToWaitingRoom(content[i]);
							stackAction(ACS_PL_ATTACK_DAMAGE, content[i]);
						}
					}
					stackAction(ACS_PL_ATTACK_CANCEL, cancelled);
				} else {
					for (int i=0;i<content.length;i++) {
						if (content[i] == CARD_NONE) {
							stackAction(ACS_PL_RESHUFFLE);
						} else {
							player.addToClock(content[i]);
							stackAction(ACS_PL_ATTACK_DAMAGE, content[i]);
						}
					}
				}
			} else {
				// TODO No damage
			}
		} else if (info == ACT_ATTACK_BATTLE) {
			if (content[0] == BTL_ATTACKER) {
				player.setCardState(attackInfo.getDefenderStage(), CS_REVERSE);
				stackAction(ACS_PL_REVERSE, attackInfo.getDefenderStage());
			} else if (content[0] == BTL_DEFENDER) {
				opponent.setCardState(attackInfo.getAttackerStage(), CS_REVERSE);
				stackAction(ACS_OP_REVERSE, attackInfo.getAttackerStage());
			} else if (content[0] == BTL_TIE) {
				player.setCardState(attackInfo.getDefenderStage(), CS_REVERSE);
				opponent.setCardState(attackInfo.getAttackerStage(), CS_REVERSE);
				stackAction(ACS_PL_REVERSE, attackInfo.getDefenderStage());
				stackAction(ACS_OP_REVERSE, attackInfo.getAttackerStage());
			}
			attackInfo.clear();
		} else if (info == ACT_ENCORE) {
			for (int i=0;i<content.length;i++) {
				opponent.payStock(3);
				opponent.setCardState(content[i], CS_REST);
				stackAction(ACS_OP_PAYSTOCK, 3);
				stackAction(ACS_OP_ENCORE, content[i]);
			}
		} else if (info == ACT_CLEANUP) {
			opponent.doCleanUp();
			stackAction(ACS_OP_CLEANUP);
			if (isInTurn()) {
				ready[PL_B] = true;
				if (ready[PL_A] && ready[PL_B]) {
					ready[PL_A] = false;
					ready[PL_B] = false;
					stackAction(ACS_PHASE, GP_END);
				}
			}
		} else if (info == ACT_LEVELUP) {
			opponent.doLevelUp(content[0]);
			stackAction(ACS_OP_LEVELUP, content[0]);
			if (!player.readyForLevelUp() && !opponent.readyForLevelUp()) {
				stackAction(ACS_RESTORESUBPHASE);
			}
		}

		if (!isSubPhase(SP_LEVELUP)) {
			if (player.readyForLevelUp()) {
				stackAction(ACS_STORESUBPHASE, SP_LEVELUP);
			} else if (opponent.readyForLevelUp()) {
				stackAction(ACS_STORESUBPHASE, SP_WAIT);
			}
		}
	}
	private void processMessage(int[] tkns) {
		if (tkns[0] == MSG_PING) {
			sendToServer(MSG_PONG);
		} else if (tkns[0] == MSG_STATE) {
			syncState(tkns[1]);
		} else if (tkns[0] == MSG_PHASE) {
			syncPhase(tkns[1]);
		} else if (tkns[0] == MSG_TURN) {
			syncTurn(tkns[1]);
		} else if (tkns[0] == MSG_ANSWER) {
			processAnswer(tkns[1], Arrays.copyOfRange(tkns, 2, tkns.length));
		} else if (tkns[0] == MSG_INFO) {
			if (tkns[1] == clientID) {
				processPlayerInfo(tkns[2], Arrays.copyOfRange(tkns, 3, tkns.length));
			} else {
				processOpponentInfo(tkns[2], Arrays.copyOfRange(tkns, 3, tkns.length));
			}
		} else if (tkns[0] == MSG_WINNER) {
			gameWinner = tkns[1];
		}
	}

	public void update() {
		String msg;
		while ((msg = getInbox()) != null) {
			String[] data = msg.split(" ");
			if (data.length > 1) {
				if (data[0].contentEquals("VERIFY")) {
					for (int i=Utility.parseInt(data[1]);i<clientLog.size();i++) {
						writeToServer(clientLog.get(i));
					}
				} else if (data[0].contentEquals("CID")) {
					clientID = Utility.parseInt(data[1]);
				} else {
					int cnt = Utility.parseInt(data[0]);
					if (cnt == logCount) waitForVerification = false;
					if (!waitForVerification) {
						if (cnt >= logCount) {
							int[] tkns = new int[data.length-1];
							for (int i=1;i<data.length;i++) tkns[i-1] = Utility.parseInt(data[i]);
							processMessage(tkns);
							logCount = cnt+1;
						} else {
							writeToServer("VERIFY " + logCount);
							waitForVerification = true;
						}
					}
				}
			}
		}
	}

	public int getClientID() {return clientID;}

	public Player getPlayer() {return player;}
	public Player getOpponent() {return opponent;}

	public boolean isInTurn() {return isTurn(clientID);}

	public void endTurn() {sendToServer(MSG_ACTION + " " + ACT_ENDTURN);}
	public void setSubPhase(int phase) {subPhase = phase;}
	public void startPhase() {setSubPhase(SP_START);}
	public void endPhase() {setSubPhase(SP_END);}
	public boolean isSubPhase(int phase) {return (subPhase == phase);}

	private void stackAction(int type, int... data) {
		StringBuilder act = new StringBuilder().append(type);
		for (int i=0;i<data.length;i++) act.append(" ").append(data[i]);
		actionStack.add(act.toString());
	}
	public String getAction() {
		if (actionStack.isEmpty()) return null;
		return actionStack.remove(0);
	}

	public int getWinner() {
		return gameWinner;
	}

	private void sendArrayListToServer(int msg, int type, ArrayList<Integer> data) {
		StringBuilder act = new StringBuilder().append(msg).append(" ").append(type);
		for (int i=0;i<data.size();i++) act.append(" ").append(data.get(i));
		sendToServer(act.toString());
	}
	private void sendArrayToServer(int msg, int type, int[] data) {
		StringBuilder act = new StringBuilder().append(msg).append(" ").append(type);
		for (int i=0;i<data.length;i++) act.append(" ").append(data[i]);
		sendToServer(act.toString());
	}
	private void sendToServer(int msg, int... data) {
		StringBuilder act = new StringBuilder().append(msg);
		for (int i=0;i<data.length;i++) act.append(" ").append(data[i]);
		sendToServer(act.toString());
	}
	private void sendToServer(String msg) {
		int cnt = clientLog.size();
		clientLog.add(cnt + " " + msg);
		writeToServer(clientLog.get(cnt));
	}

	protected abstract void writeToServer(String msg);

}
