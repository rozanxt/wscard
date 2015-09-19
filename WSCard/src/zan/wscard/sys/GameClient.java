package zan.wscard.sys;

import static zan.wscard.sys.GameSystem.ACT_ATTACK_DECLARATION;
import static zan.wscard.sys.GameSystem.ACT_ENDTURN;
import static zan.wscard.sys.GameSystem.ACT_STANDUP;
import static zan.wscard.sys.GameSystem.GP_DRAW;
import static zan.wscard.sys.GameSystem.GP_WAIT;
import static zan.wscard.sys.GameSystem.MSG_ACTION;
import static zan.wscard.sys.GameSystem.MSG_REQUEST;
import static zan.wscard.sys.GameSystem.REQ_DRAW;
import static zan.wscard.sys.GameSystem.SP_ATTACK_TRIGGER;
import static zan.wscard.sys.GameSystem.SP_END;

import java.util.ArrayList;
import java.util.Arrays;

import zan.lib.util.Utility;

public abstract class GameClient extends GameSystem {

	private Player player = new Player();
	private Player opponent = new Player();

	private ArrayList<String> clientLog = new ArrayList<String>();
	private int logCount = 0;
	private boolean waitForVerification = false;

	private ArrayList<String> actionStack = new ArrayList<String>();

	private AttackInfo attackInfo = new AttackInfo();

	private int clientID = PL_NONE;

	private int subPhase = SP_END;

	public void initClient(PlayerInfo infoPlayer, PlayerInfo infoOpponent) {
		player.setInfo(infoPlayer);
		opponent.setInfo(infoOpponent);
	}

	public void setSubPhase(int phase) {subPhase = phase;}

	public void syncAction(int type, int[] actions) {
		if (type == ACT_STANDUP) {

		} else if (type == ACT_DRAWTOHAND) {
			for (int i=1;i<actions.length;i++) {
				player.addToHand(actions[i]);
				if (actions[i] != CARD_NONE) actions[i] = 0;
			}
		} else if (type == ACT_DISCARDFROMHAND) {
			player.removeFromHand(actions[0]);
			player.addToWaitingRoom(actions[0]);
		} else if (type == ACT_CLOCKFROMHAND) {
			player.removeFromHand(actions[0]);
			player.addToClock(actions[0]);
		} else if (type == ACT_PLACEFROMHAND) {
			player.removeFromHand(actions[0]);
			player.placeOnStage(actions[0], actions[1]);
		} else if (type == ACT_SWAPONSTAGE) {
			player.swapOnStage(actions[0], actions[1]);
		} else if (type == ACT_ATTACK_DECLARATION) {

		} else if (type == ACT_ATTACK_TRIGGER) {

		} else if (type == ACT_ATTACK_DAMAGE) {

		}
	}

	public void actStandUp() {
		actionStack.add("STANDUP");
		sendToServer(MSG_ACTION, ACT_STANDUP);
		setPhase(GP_DRAW);
	}

	public void actDraw() {
		sendToServer(MSG_REQUEST, REQ_DRAW, 1);
		setSubPhase(SP_END);
	}

	public void actCleanUp() {
		actionStack.add("OPCLEANUP");
		actionStack.add("CLEANUP");
		sendToServer(MSG_ACTION, ACT_CLEANUP);
		sendToServer(MSG_ACTION, ACT_ENDTURN);
		setPhase(GP_WAIT);
	}

	public void submitActions(ArrayList<PlayerAction> actions) {
		for (int i=0;i<actions.size();i++) {
			syncAction(actions.get(i).type, actions.get(i).actions);
			sendArrayToServer(MSG_ACTION, actions.get(i).type, actions.get(i).actions);
		}
		actions.clear();
	}

	public void submitAttack(int type, int stage) {
		attackInfo.clear();
		attackInfo.setAttack(player, opponent);
		attackInfo.setType(type);
		attackInfo.setStage(stage);
		sendToServer(MSG_ACTION, ACT_ATTACK_DECLARATION, type, stage);
		sendToServer(MSG_REQUEST, REQ_DRAW, 1);
		setSubPhase(SP_ATTACK_TRIGGER);
	}

	private void setState(int state) {
		gameState = state;

		if (isState(GS_FIRSTDRAW)) {
			setSubPhase(SP_START);
		}
	}
	public void setPhase(int phase) {
		gamePhase = phase;

		if (isInTurn()) {
			sendToServer(MSG_PHASE, gamePhase);
			setSubPhase(SP_START);
		}
	}
	private void setTurn(int turn) {
		playerTurn = turn;

		if (isInTurn()) {
			setPhase(GP_STANDUP);
			setSubPhase(SP_START);
		}
	}

	private void doDrawToHand(int[] cards) {
		for (int i=0;i<cards.length;i++) {
			if (cards[i] == CARD_NONE) actionStack.add("RESHUFFLE");
			else actionStack.add("DRAW " + cards[i]);
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
		attackInfo.setTrigger(trigger);
		actionStack.add("TRIGGER " + trigger);
		sendToServer(MSG_ACTION, ACT_ATTACK_TRIGGER, trigger);
		sendToServer(MSG_REQUEST, REQ_DEALDAMAGE, attackInfo.getDamage());
		setSubPhase(SP_ATTACK_DEALDAMAGE);
	}

	private void doDealDamage(int[] damage) {
		int damageCnt = 0;
		for (int i=0;i<damage.length;i++) {
			if (damage[i] == CARD_NONE) {
				actionStack.add("OPRESHUFFLE");
			} else {
				damageCnt++;
				actionStack.add("OPDAMAGE " + damage[i]);
			}
			if (opponent.getCardData(damage[i]).type == CARD_CLIMAX) {
				actionStack.add("OPCANCEL " + damageCnt);
				damageCnt = 0;
			}
		}
		sendArrayToServer(MSG_ACTION, ACT_ATTACK_DAMAGE, damage);
	}

	private void doAttackBattle() {
		int result = attackInfo.getBattleResult();
		if (result == BTL_ATTACKER) {
			actionStack.add("OPREVERSE " + attackInfo.getDefenderStage());
		} else if (result == BTL_DEFENDER) {
			actionStack.add("REVERSE " + attackInfo.getAttackerStage());
		} else if (result == BTL_TIE) {
			actionStack.add("OPREVERSE " + attackInfo.getDefenderStage());
			actionStack.add("REVERSE " + attackInfo.getAttackerStage());
		}
		sendToServer(MSG_ACTION, ACT_ATTACK_BATTLE, result);

		attackInfo.clear();
		setSubPhase(SP_START);
	}

	private void infoStandUp() {
		actionStack.add("OPSTANDUP");
	}

	private void infoDrawToHand(int[] cards) {
		for (int i=0;i<cards.length;i++) {
			if (cards[i] == CARD_NONE) {
				actionStack.add("OPRESHUFFLE");
			} else {
				opponent.addToHand(cards[i]);
				actionStack.add("OPDRAW");
			}
		}
	}

	private void infoDiscardFromHand(int card) {
		opponent.removeFromHand(card);
		opponent.addToWaitingRoom(card);
		actionStack.add("OPDISCARD " + card);
	}

	private void infoClockFromHand(int card) {
		opponent.removeFromHand(card);
		opponent.addToClock(card);
		actionStack.add("OPCLOCK " + card);
	}

	private void infoPlaceFromHand(int card, int stage) {
		opponent.removeFromHand(card);
		opponent.placeOnStage(card, stage);
		actionStack.add("OPPLACE " + card + " " + stage);
	}

	private void infoSwapOnStage(int stage1, int stage2) {
		opponent.swapOnStage(stage1, stage2);
		actionStack.add("OPMOVE " + stage1 + " " + stage2);
	}

	private void infoAttackDeclaration(int type, int stage) {
		attackInfo.clear();
		attackInfo.setAttack(opponent, player);
		attackInfo.setType(type);
		attackInfo.setStage(stage);
		actionStack.add("OPATTACK " + type + " " + stage);
	}

	private void infoAttackTrigger(int trigger) {
		attackInfo.setTrigger(trigger);
		actionStack.add("OPTRIGGER " + trigger);
	}

	private void infoAttackDamage(int[] damage) {
		int damageCnt = 0;
		for (int i=0;i<damage.length;i++) {
			if (damage[i] == CARD_NONE) {
				actionStack.add("RESHUFFLE");
			} else {
				damageCnt++;
				actionStack.add("DAMAGE " + damage[i]);
			}
			if (player.getCardData(damage[i]).type == CARD_CLIMAX) {
				actionStack.add("CANCEL " + damageCnt);
				damageCnt = 0;
			}
		}
	}

	private void infoAttackBattle(int result) {
		if (result == BTL_ATTACKER) {
			actionStack.add("REVERSE " + attackInfo.getDefenderStage());
		} else if (result == BTL_DEFENDER) {
			actionStack.add("OPREVERSE " + attackInfo.getAttackerStage());
		} else if (result == BTL_TIE) {
			actionStack.add("REVERSE " + attackInfo.getDefenderStage());
			actionStack.add("OPREVERSE " + attackInfo.getAttackerStage());
		}
	}

	private void infoCleanUp() {
		actionStack.add("CLEANUP");
		actionStack.add("OPCLEANUP");
	}

	private void processMessage(int[] tkns) {
		if (tkns[0] == MSG_PING) {
			sendToServer(MSG_PONG);
		} else if (tkns[0] == MSG_STATE) {
			setState(tkns[1]);
		} else if (tkns[0] == MSG_PHASE) {
			setPhase(tkns[1]);
		} else if (tkns[0] == MSG_TURN) {
			setTurn(tkns[1]);
		} else if (tkns[0] == MSG_ANSWER) {
			if (tkns[1] == ANS_DRAW) {
				if (isState(GS_FIRSTDRAW)) {
					doDrawToHand(Arrays.copyOfRange(tkns, 2, tkns.length));
					if (isSubPhase(SP_END)) sendToServer(MSG_ACTION, ACT_ENDTURN);
				} else if (isState(GS_GAME)) {
					if (isPhase(GP_DRAW)) {
						doDrawToHand(Arrays.copyOfRange(tkns, 2, tkns.length));
						setPhase(GP_CLOCK);
					} else if (isPhase(GP_CLOCK)) {
						doDrawToHand(Arrays.copyOfRange(tkns, 2, tkns.length));
						setPhase(GP_MAIN);
					} else if (isPhase(GP_ATTACK)) {
						if (isSubPhase(SP_ATTACK_TRIGGER)) {
							doDrawTrigger(Arrays.copyOfRange(tkns, 2, tkns.length));
						}
					}
				}
			} else if (tkns[1] == ANS_DEALDAMAGE) {
				if (isState(GS_GAME) && isPhase(GP_ATTACK) && isSubPhase(SP_ATTACK_DEALDAMAGE)) {
					doDealDamage(Arrays.copyOfRange(tkns, 2, tkns.length));
					setSubPhase(SP_ATTACK_BATTLE);
					doAttackBattle();
				}
			}
		} else if (tkns[0] == MSG_INFO) {
			if (tkns[1] == clientID) {
				// TODO INFO SYNC
			} else {
				if (tkns[2] == ACT_STANDUP) {
					infoStandUp();
				} else if (tkns[2] == ACT_DRAWTOHAND) {
					infoDrawToHand(Arrays.copyOfRange(tkns, 3, tkns.length));
				} else if (tkns[2] == ACT_DISCARDFROMHAND) {
					infoDiscardFromHand(tkns[3]);
				} else if (tkns[2] == ACT_CLOCKFROMHAND) {
					infoClockFromHand(tkns[3]);
				} else if (tkns[2] == ACT_PLACEFROMHAND) {
					infoPlaceFromHand(tkns[3], tkns[4]);
				} else if (tkns[2] == ACT_SWAPONSTAGE) {
					infoSwapOnStage(tkns[3], tkns[4]);
				} else if (tkns[2] == ACT_ATTACK_DECLARATION) {
					infoAttackDeclaration(tkns[3], tkns[4]);
				} else if (tkns[2] == ACT_ATTACK_TRIGGER) {
					infoAttackTrigger(tkns[3]);
				} else if (tkns[2] == ACT_ATTACK_DAMAGE) {
					infoAttackDamage(Arrays.copyOfRange(tkns, 3, tkns.length));
				} else if (tkns[2] == ACT_ATTACK_BATTLE) {
					infoAttackBattle(tkns[3]);
				} else if (tkns[2] == ACT_CLEANUP) {
					infoCleanUp();
				}
			}
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

	public boolean isSubPhase(int phase) {return (subPhase == phase);}
	public boolean isInTurn() {return isTurn(clientID);}

	public Player getPlayer() {return player;}
	public Player getOpponent() {return opponent;}

	public String getAction() {
		if (actionStack.isEmpty()) return null;
		return actionStack.remove(0);
	}

	public void sendArrayListToServer(int msg, int type, ArrayList<Integer> data) {
		StringBuilder act = new StringBuilder().append(msg).append(" ").append(type);
		for (int i=0;i<data.size();i++) act.append(" ").append(data.get(i));
		sendToServer(act.toString());
	}
	public void sendArrayToServer(int msg, int type, int[] data) {
		StringBuilder act = new StringBuilder().append(msg).append(" ").append(type);
		for (int i=0;i<data.length;i++) act.append(" ").append(data[i]);
		sendToServer(act.toString());
	}
	public void sendToServer(int msg, int... data) {
		StringBuilder act = new StringBuilder().append(msg);
		for (int i=0;i<data.length;i++) act.append(" ").append(data[i]);
		sendToServer(act.toString());
	}
	public void sendToServer(String msg) {
		int cnt = clientLog.size();
		clientLog.add(cnt + " " + msg);
		writeToServer(clientLog.get(cnt));
	}

	protected abstract void writeToServer(String msg);

}
