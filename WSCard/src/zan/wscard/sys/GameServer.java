package zan.wscard.sys;

import java.util.ArrayList;
import java.util.Arrays;

import zan.lib.util.Utility;

public abstract class GameServer extends GameSystem {

	private ArrayList<String> serverLog = new ArrayList<String>();
	private int[] logCount = new int[PL_NUM];
	private boolean[] waitForVerification = new boolean[PL_NUM];
	private boolean[] ready = new boolean[PL_NUM];

	private Player playerA = new Player();
	private Player playerB = new Player();

	private AttackInfo attackInfo = new AttackInfo();

	public GameServer() {
		logCount[PL_A] = 0;
		logCount[PL_B] = 0;
		waitForVerification[PL_A] = false;
		waitForVerification[PL_B] = false;
		ready[PL_A] = false;
		ready[PL_B] = false;
	}

	public void initServer(PlayerInfo infoA, PlayerInfo infoB) {
		playerA.setInfo(infoA);
		playerB.setInfo(infoB);
	}

	private void doChangeTurn() {
		if (isTurn(PL_A)) setTurn(PL_B);
		else if (isTurn(PL_B)) setTurn(PL_A);
		else setTurn(PL_A);	// TODO Randomize first turn
		sendToAllClients(MSG_TURN, playerTurn);
	}
	private void doDrawCards(int cid, int num) {
		Player player = getPlayer(cid);
		ArrayList<Integer> cards = new ArrayList<Integer>();
		for (int i=0;i<num;i++) {
			int drawn = player.drawCard();
			cards.add(drawn);
			if (drawn == CARD_NONE) {
				player.reshuffleDeck();
				cards.add(player.drawCard());
			}
		}
		sendArrayListToClient(cid, MSG_ANSWER, ANS_DRAW, cards);
	}
	private void doDealDamage(int cid, int num) {
		Player opponent = getOtherPlayer(cid);
		ArrayList<Integer> cards = new ArrayList<Integer>();
		for (int i=0;i<num;i++) {
			int drawn = opponent.drawCard();
			cards.add(drawn);
			if (drawn == CARD_NONE) {
				opponent.reshuffleDeck();
				drawn = opponent.drawCard();
				cards.add(drawn);
			}
			if (opponent.getCardData(drawn).type == CARD_CLIMAX) break;
		}
		sendArrayListToClient(cid, MSG_ANSWER, ANS_DEALDAMAGE, cards);
	}
	private void doLevelUp(int cid) {
		// TODO
		sendToAllClients(MSG_ANSWER, ANS_LEVELUP);
	}
	private void doInformAction(int cid, int info, int[] content) {	// TODO
		Player player = getPlayer(cid);
		Player opponent = getOtherPlayer(cid);

		if (info == ACT_NONE) {
			// NONE
		} else if (info == ACT_ENDTURN) {
			// After informing action
		} else if (info == ACT_STANDUP) {
			player.doStandUp();
		} else if (info == ACT_DRAWTOHAND) {
			for (int i=0;i<content.length;i++) {
				if (content[i] != CARD_NONE) {
					player.addToHand(content[i]);
					content[i] = 0;
				}
			}
		} else if (info == ACT_DISCARDFROMHAND) {
			player.removeFromHand(content[0]);
			player.addToWaitingRoom(content[0]);
		} else if (info == ACT_CLOCKFROMHAND) {
			player.removeFromHand(content[0]);
			player.addToClock(content[0]);
		} else if (info == ACT_PLACEFROMHAND) {
			player.removeFromHand(content[0]);
			player.placeOnStage(content[0], content[1]);
		} else if (info == ACT_SWAPONSTAGE) {
			player.swapOnStage(content[0], content[1]);
		} else if (info == ACT_MAIN_END) {
			// NONE
		} else if (info == ACT_ATTACK_DECLARATION) {
			attackInfo.clear();
			attackInfo.setAttack(player, opponent);
			attackInfo.setType(content[0]);
			attackInfo.setStage(content[1]);
			player.setCardState(content[1], CS_REST);
		} else if (info == ACT_ATTACK_TRIGGER) {
			attackInfo.setTrigger(content[0]);
			player.addToStock(content[0]);
		} else if (info == ACT_ATTACK_DAMAGE) {
			if (content.length > 0) {
				if (opponent.getCardData(content[content.length-1]).type == CARD_CLIMAX) {
					for (int i=0;i<content.length;i++) {
						if (content[i] != CARD_NONE) opponent.addToWaitingRoom(content[i]);
					}
				} else {
					for (int i=0;i<content.length;i++) {
						if (content[i] != CARD_NONE) opponent.addToClock(content[i]);
					}
				}
			} else {
				// TODO No damage
			}
		} else if (info == ACT_ATTACK_BATTLE) {
			if (content[0] == BTL_ATTACKER) {
				opponent.setCardState(attackInfo.getDefenderStage(), CS_REVERSE);
			} else if (content[0] == BTL_DEFENDER) {
				player.setCardState(attackInfo.getAttackerStage(), CS_REVERSE);
			} else if (content[0] == BTL_TIE) {
				opponent.setCardState(attackInfo.getDefenderStage(), CS_REVERSE);
				player.setCardState(attackInfo.getAttackerStage(), CS_REVERSE);
			}
			attackInfo.clear();
		} else if (info == ACT_CLEANUP) {
			opponent.doCleanUp();
			player.doCleanUp();
		} else if (info == ACT_LEVELUP) {
			player.doLevelUp(content[0]);
		}

		StringBuilder m = new StringBuilder().append(MSG_INFO).append(" ").append(cid).append(" ").append(info);
		for (int i=0;i<content.length;i++) m.append(" ").append(content[i]);
		sendToAllClients(m.toString());

		if (info == ACT_ENDTURN) {
			if (isState(GS_FIRSTDRAW)) {
				ready[cid] = true;
				if (ready[PL_A] && ready[PL_B]) {
					ready[PL_A] = false;
					ready[PL_B] = false;
					sendState(GS_GAME);
					doChangeTurn();
				}
			} else if (isState(GS_GAME)) {
				if (isTurn(cid)) doChangeTurn();
			}
		}
	}

	private void processMessage(int cid, int[] tkns) {
		if (tkns[0] == MSG_PING) sendToClient(cid, MSG_PONG);

		if (isState(GS_INIT)) {
			if (tkns[0] == MSG_READY) {
				ready[cid] = true;
				if (ready[PL_A] && ready[PL_B]) {
					ready[PL_A] = false;
					ready[PL_B] = false;
					sendState(GS_FIRSTDRAW);
				}
			}
		} else if (isState(GS_FIRSTDRAW)) {
			if (tkns[0] == MSG_REQUEST) {
				if (tkns[1] == REQ_DRAW) doDrawCards(cid, tkns[2]);
			} else if (tkns[0] == MSG_ACTION) {
				doInformAction(cid, tkns[1], Arrays.copyOfRange(tkns, 2, tkns.length));
			}
		} else if (isState(GS_GAME)) {
			if (tkns[0] == MSG_PHASE) {
				if (isTurn(cid)) sendPhase(tkns[1]);	// TODO (!)
			} else if (tkns[0] == MSG_REQUEST) {
				if (tkns[1] == REQ_DRAW) doDrawCards(cid, tkns[2]);
				else if (tkns[1] == REQ_DEALDAMAGE) doDealDamage(cid, tkns[2]);
				else if (tkns[1] == REQ_LEVELUP) doLevelUp(cid);
			} else if (tkns[0] == MSG_ACTION) {
				doInformAction(cid, tkns[1], Arrays.copyOfRange(tkns, 2, tkns.length));
			}
		} else if (isState(GS_END)) {
			// TODO
		}
	}

	public void update() {
		String msg;
		while ((msg = getInbox()) != null) {
			String[] data = msg.split(" ");
			if (data.length > 2) {
				int cid = Utility.parseInt(data[0]);
				if (data[1].contentEquals("VERIFY")) {
					for (int i=Utility.parseInt(data[2]);i<serverLog.size();i++) {
						String ver = serverLog.get(i);
						if (ver.startsWith("ALL")) {
							writeToClient(cid, ver.substring(4));
						} else if (ver.startsWith(data[0])) {
							writeToClient(cid, ver.substring(data[0].length()+1));
						}
					}
				} else {
					int cnt = Utility.parseInt(data[1]);
					int[] tkns = new int[data.length-2];
					for (int i=2;i<data.length;i++) tkns[i-2] = Utility.parseInt(data[i]);
					if (cnt == logCount[cid]) {
						processMessage(cid, tkns);
						logCount[cid]++;
						waitForVerification[cid] = false;
					} else if (!waitForVerification[cid]) {
						writeToClient(cid, "VERIFY " + logCount[cid]);
						waitForVerification[cid] = true;
					}
				}
			}
		}
	}

	public Player getPlayer(int cid) {
		if (cid == PL_A) return playerA;
		else if (cid == PL_B) return playerB;
		return null;
	}
	public Player getOtherPlayer(int cid) {
		if (cid == PL_A) return playerB;
		else if (cid == PL_B) return playerA;
		return null;
	}

	private void sendState(int state) {
		setState(state);
		sendToAllClients(MSG_STATE, gameState);
	}
	private void sendPhase(int phase) {
		setPhase(phase);
		sendToAllClients(MSG_PHASE, gamePhase);
	}

	private void sendArrayListToClient(int cid, int msg, int type, ArrayList<Integer> data) {
		StringBuilder m = new StringBuilder().append(msg).append(" ").append(type);
		for (int i=0;i<data.size();i++) m.append(" ").append(data.get(i));
		sendToClient(cid, m.toString());
	}

	private void sendToClient(int cid, int msg, int... data) {
		StringBuilder m = new StringBuilder().append(msg);
		for (int i=0;i<data.length;i++) m.append(" ").append(data[i]);
		sendToClient(cid, m.toString());
	}
	private void sendToAllClients(int msg, int... data) {
		StringBuilder m = new StringBuilder().append(msg);
		for (int i=0;i<data.length;i++) m.append(" ").append(data[i]);
		sendToAllClients(m.toString());
	}

	private void sendToClient(int cid, String msg) {
		int cnt = serverLog.size();
		serverLog.add(cid + " " + cnt + " " + msg);
		writeToClient(cid, cnt + " " + msg);
	}
	private void sendToAllClients(String msg) {
		int cnt = serverLog.size();
		serverLog.add("ALL " + cnt + " " + msg);
		writeToAllClients(cnt + " " + msg);
	}

	protected abstract void writeToClient(int cid, String msg);
	protected abstract void writeToAllClients(String msg);

}
