package zan.wscard.sys;

import java.util.ArrayList;
import java.util.Arrays;

import zan.lib.util.Utility;
import static zan.wscard.sys.Player.NO_CARD;

public abstract class GameServer extends GameSystem {

	private Player playerA = new Player();
	private Player playerB = new Player();

	private ArrayList<String> serverLog = new ArrayList<String>();
	private int[] logCount = new int[PL_NUM];
	private boolean[] waitForVerification = new boolean[PL_NUM];
	private boolean[] ready = new boolean[PL_NUM];

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

	private void sendState(int state) {
		setState(state);
		sendToAllClients(MSG_STATE, gameState);
	}
	private void sendPhase(int phase) {
		setPhase(phase);
		sendToAllClients(MSG_PHASE, gamePhase);
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
			if (drawn == NO_CARD) {
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
			if (drawn == NO_CARD) {
				opponent.reshuffleDeck();
				drawn = opponent.drawCard();
				cards.add(drawn);
			}
			if (opponent.getCardData(drawn).type == CARD_CLIMAX) break;
		}
		sendArrayListToClient(cid, MSG_ANSWER, ANS_DEALDAMAGE, cards);
	}

	private void doInformAction(int cid, int type, int[] actions) {
		Player player = getPlayer(cid);
		Player opponent = getOtherPlayer(cid);

		if (type == ACT_STANDUP) {
			// TODO Player card state CS_STAND
		} else if (type == ACT_DRAWTOHAND) {
			for (int i=0;i<actions.length;i++) {
				if (actions[i] != CARD_NONE) {
					player.addToHand(actions[i]);
					actions[i] = 0;
				}
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
			// TODO Player card state CS_REST
		} else if (type == ACT_ATTACK_TRIGGER) {
			player.addToStock(actions[0]);
		} else if (type == ACT_ATTACK_DAMAGE) {
			for (int i=0;i<actions.length;i++) {
				if (actions[i] != CARD_NONE) opponent.addToClock(actions[i]);
			}
		} else if (type == ACT_CLEANUP) {
			// TODO Player card state CS_REVERSE to Waiting Room
		}

		StringBuilder m = new StringBuilder().append(MSG_INFO).append(" ").append(cid).append(" ").append(type);
		for (int i=0;i<actions.length;i++) m.append(" ").append(actions[i]);
		sendToAllClients(m.toString());

		if (type == ACT_ENDTURN) {
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
		return getPlayer((cid == PL_A)?PL_B:PL_A);
	}

	private void sendArrayListToClient(int cid, int msg, int type, ArrayList<Integer> data) {
		StringBuilder m = new StringBuilder().append(msg).append(" ").append(type);
		for (int i=0;i<data.size();i++) m.append(" ").append(data.get(i));
		sendToClient(cid, m.toString());
	}
	private void sendArrayListToAllClients(int msg, int type, ArrayList<Integer> data) {
		StringBuilder m = new StringBuilder().append(msg).append(" ").append(type);
		for (int i=0;i<data.size();i++) m.append(" ").append(data.get(i));
		sendToAllClients(m.toString());
	}

	private void sendArrayToClient(int cid, int msg, int type, int[] data) {
		StringBuilder m = new StringBuilder().append(msg).append(" ").append(type);
		for (int i=0;i<data.length;i++) m.append(" ").append(data[i]);
		sendToClient(cid, m.toString());
	}
	private void sendArrayToAllClients(int msg, int type, int[] data) {
		StringBuilder m = new StringBuilder().append(msg).append(" ").append(type);
		for (int i=0;i<data.length;i++) m.append(" ").append(data[i]);
		sendToAllClients(m.toString());
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
