package zan.wscard.sys;

import java.util.ArrayList;

import zan.lib.util.Utility;
import zan.wscard.card.CardData;
import static zan.wscard.sys.PlayerMove.*;

public abstract class GameServer extends GameSystem {

	protected PlayerServer playerA = new PlayerServer();
	protected PlayerServer playerB = new PlayerServer();

	protected boolean readyA = false;
	protected boolean readyB = false;

	protected ArrayList<String> serverLog = new ArrayList<String>();
	protected int logCountA = 0;
	protected int logCountB = 0;
	protected boolean waitForVerificationA = false;
	protected boolean waitForVerificationB = false;

	public void initServer(PlayerInfo infoA, PlayerInfo infoB) {
		playerA.setInfo(infoA);
		playerB.setInfo(infoB);
	}

	protected void doChangeTurn() {
		if (playerTurn == PL_A) playerTurn = PL_B;
		else if (playerTurn == PL_B) playerTurn = PL_A;
		else playerTurn = PL_A;	// TODO
		sendToAllClients("TURN " + playerTurn);
	}

	protected void doDrawCards(int cid, int num) {
		ArrayList<Integer> drawn = getPlayer(cid).drawCards(num);
		String m = "DRAW";
		for (int i=0;i<drawn.size();i++) m += " " + drawn.get(i);
		sendToClient(cid, m);
	}

	protected void doAttack(int cid, int type, int stage) {
		PlayerServer player = getPlayer(cid);
		CardData attacker = player.getCardData(player.getStageCard(stage));
		int trigger = player.triggerCard();
		int triggersoul = player.getCardData(trigger).soul;	// TODO trigger parsing
		int damage = 0;
		if (type == 0) {
			damage = attacker.soul + triggersoul + 1;
		} else if (type == 1) {
			damage = attacker.soul + triggersoul;
		} else if (type == 2) {
			CardData defender = getPlayer(((cid == PL_A)?PL_B:PL_A)).getCardData(getPlayer(((cid == PL_A)?PL_B:PL_A)).getStageCard(2-stage));
			damage = attacker.soul + triggersoul - defender.level;
		}
		ArrayList<Integer> damaged = getPlayer(((cid == PL_A)?PL_B:PL_A)).damageCards(damage);
		sendToAllClients("MOVE " + cid + " " + MT_TRIGGER + " " + trigger);
		String m = "MOVE " + ((cid == PL_A)?PL_B:PL_A) + " " + MT_DAMAGE;
		for (int i=0;i<damaged.size();i++) m += " " + damaged.get(i);
		sendToAllClients(m);

		if (type != 0) {
			CardData defender = getPlayer(((cid == PL_A)?PL_B:PL_A)).getCardData(getPlayer(((cid == PL_A)?PL_B:PL_A)).getStageCard(2-stage));
			if (attacker.power > defender.power) {
				sendToAllClients("MOVE " + ((cid == PL_A)?PL_B:PL_A) + " " + MT_REVERSE + " " + (2-stage));
			} else if (attacker.power < defender.power) {
				sendToAllClients("MOVE " + cid + " " + MT_REVERSE + " " + stage);
			} else {
				sendToAllClients("MOVE " + cid + " " + MT_REVERSE + " " + stage);
				sendToAllClients("MOVE " + ((cid == PL_A)?PL_B:PL_A) + " " + MT_REVERSE + " " + (2-stage));
			}
		}
	}

	public void processMessage(int cid, String[] tkns) {
		if (isState(GS_INIT)) {
			if (tkns[0].contentEquals("READY")) {
				if (cid == PL_A) readyA = true;
				else if (cid == PL_B) readyB = true;
				if (readyA && readyB) {
					readyA = false;
					readyB = false;
					setState(GS_FIRSTDRAW);
					doDrawCards(PL_A, 5);
					doDrawCards(PL_B, 5);
					sendToAllClients("MOVE 0 2 5");
					sendToAllClients("MOVE 1 2 5");
				}
			}
		} else if (isState(GS_FIRSTDRAW)) {
			if (tkns[0].contentEquals("DO")) {
				int type = Utility.parseInt(tkns[1]);

				// TODO
				String m = "MOVE " + cid + " " + type;
				for (int i=2;i<tkns.length;i++) m += " " + Utility.parseInt(tkns[i]);
				sendToAllClients(m);

				if (type == MT_ENDTURN) {
					if (cid == PL_A) readyA = true;
					else if (cid == PL_B) readyB = true;
					if (readyA && readyB) {
						readyA = false;
						readyB = false;
						setState(GS_GAME);
						doChangeTurn();
					}
				} else if (type == MT_DRAW) {
					doDrawCards(cid, Utility.parseInt(tkns[2]));
				} else if (type == MT_DISCARD) {
					getPlayer(cid).discardCard(Utility.parseInt(tkns[2]));
				}
			}
		} else if (isState(GS_GAME)) {
			if (tkns[0].contentEquals("PHASE")) {
				if (cid == playerTurn) setPhase(Utility.parseInt(tkns[1]));
			} else if (tkns[0].contentEquals("DO")) {
				int type = Utility.parseInt(tkns[1]);

				if (cid == playerTurn) {
					// TODO
					String m = "MOVE " + cid + " " + type;
					for (int i=2;i<tkns.length;i++) m += " " + Utility.parseInt(tkns[i]);
					sendToAllClients(m);

					if (type == MT_ENDTURN) {
						doChangeTurn();
					} else if (type == MT_DRAW) {
						doDrawCards(cid, Utility.parseInt(tkns[2]));
					} else if (type == MT_DISCARD) {
						getPlayer(cid).discardCard(Utility.parseInt(tkns[2]));
					} else if (type == MT_PLACE) {
						getPlayer(cid).placeCard(Utility.parseInt(tkns[2]), Utility.parseInt(tkns[3]));
					} else if (type == MT_SWAP) {
						getPlayer(cid).moveCard(Utility.parseInt(tkns[2]), Utility.parseInt(tkns[3]));
					} else if (type == MT_CLOCK) {
						getPlayer(cid).clockCard(Utility.parseInt(tkns[2]));
					} else if (type == MT_ATTACK) {
						doAttack(cid, Utility.parseInt(tkns[2]), Utility.parseInt(tkns[3]));
					}
				}
			}
		} else if (isState(GS_END)) {

		}
	}

	public void update() {
		String msg;
		while ((msg = getInbox()) != null) {
			String[] chunk = msg.split(" ");
			if (chunk.length > 2) {
				int cid = Utility.parseInt(chunk[0]);
				if (chunk[1].contentEquals("VERIFY")) {
					for (int i=Utility.parseInt(chunk[2]);i<serverLog.size();i++) {
						String ver = serverLog.get(i);
						if (ver.startsWith("ALL")) {
							writeToClient(cid, ver.substring(4));
						} else if (ver.startsWith(chunk[0])) {
							writeToClient(cid, ver.substring(chunk[0].length()+1));
						}
					}
				} else {
					int cnt = Utility.parseInt(chunk[1]);
					String[] tkns = new String[chunk.length-2];
					for (int i=2;i<chunk.length;i++) tkns[i-2] = chunk[i];
					if (cid == PL_A) {
						if (cnt == logCountA) {
							processMessage(cid, tkns);
							logCountA++;
							waitForVerificationA = false;
						} else if (!waitForVerificationA) {
							writeToClient(cid, "VERIFY " + logCountA);
							waitForVerificationA = true;
						}
					} else if (cid == PL_B) {
						if (cnt == logCountB) {
							processMessage(cid, tkns);
							logCountB++;
							waitForVerificationB = false;
						} else if (!waitForVerificationB) {
							writeToClient(cid, "VERIFY " + logCountB);
							waitForVerificationB = true;
						}
					}
				}
			}
		}
	}

	protected PlayerServer getPlayer(int cid) {
		if (cid == PL_A) return playerA;
		else if (cid == PL_B) return playerB;
		return null;
	}
	protected PlayerServer getPlayerInTurn() {return getPlayer(playerTurn);}
	protected PlayerServer getPlayerInWait() {return getPlayer((playerTurn == PL_A)?PL_B:PL_A);}

	@Override
	protected void setState(int state) {
		super.setState(state);
		sendToAllClients("STATE " + gameState);
	}
	@Override
	protected void setPhase(int phase) {
		super.setPhase(phase);
		sendToAllClients("PHASE " + gamePhase);
	}

	protected void sendToClient(int cid, String msg) {
		serverLog.add(cid + " " + serverLog.size() + " " + msg);
		writeToClient(cid, (serverLog.size()-1) + " " + msg);
	}

	protected void sendToAllClients(String msg) {
		serverLog.add("ALL " + serverLog.size() + " " + msg);
		writeToAllClients((serverLog.size()-1) + " " + msg);
	}

	protected abstract void writeToClient(int cid, String msg);
	protected abstract void writeToAllClients(String msg);

}
