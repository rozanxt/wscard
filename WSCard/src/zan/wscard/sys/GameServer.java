package zan.wscard.sys;

import java.util.ArrayList;

import zan.lib.util.Utility;
import zan.wscard.card.CardData;
import static zan.wscard.sys.PlayerMove.*;

public abstract class GameServer extends GameSystem {

	protected PlayerServer playerA, playerB;

	protected boolean readyA, readyB;

	public GameServer() {
		super();
		playerA = new PlayerServer();
		playerB = new PlayerServer();
		readyA = false;
		readyB = false;
	}

	public void initServer(PlayerInfo infoA, PlayerInfo infoB) {
		playerA.setInfo(infoA);
		playerB.setInfo(infoB);
	}

	protected void doChangeTurn() {
		if (playerTurn == PL_A) playerTurn = PL_B;
		else if (playerTurn == PL_B) playerTurn = PL_A;
		else playerTurn = PL_A;	// TODO
		writeToAllClients("TURN " + playerTurn);
	}

	protected void doDrawCards(int cid, int num) {
		ArrayList<Integer> drawn = getPlayer(cid).drawCards(num);
		String m = "DRAW";
		for (int i=0;i<drawn.size();i++) m += " " + drawn.get(i);
		writeToClient(cid, m);
	}

	public void update() {
		String msg = getInbox();
		while (msg != null && !msg.isEmpty()) {
			String[] tkns = msg.split(" ");
			int cid = Utility.parseInt(tkns[0]);

			if (isState(GS_INIT)) {
				if (tkns[1].contentEquals("READY")) {
					if (cid == PL_A) readyA = true;
					else if (cid == PL_B) readyB = true;
					if (readyA && readyB) {
						readyA = false;
						readyB = false;
						setState(GS_FIRSTDRAW);
						doDrawCards(PL_A, 5);
						doDrawCards(PL_B, 5);
						writeToAllClients("MOVE 0 2 5");
						writeToAllClients("MOVE 1 2 5");
					}
				}
			} else if (isState(GS_FIRSTDRAW)) {
				if (tkns[1].contentEquals("DO")) {
					int type = Utility.parseInt(tkns[2]);

					// TODO
					String m = "MOVE " + cid + " " + type;
					for (int i=3;i<tkns.length;i++) m += " " + Utility.parseInt(tkns[i]);
					writeToAllClients(m);

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
						doDrawCards(cid, Utility.parseInt(tkns[3]));
					} else if (type == MT_DISCARD) {
						if (cid == PL_A) playerA.discardCard(Utility.parseInt(tkns[3]));
						else if (cid == PL_B) playerB.discardCard(Utility.parseInt(tkns[3]));
					}
				}
			} else if (isState(GS_GAME)) {
				if (tkns[1].contentEquals("PHASE")) {
					if (cid == playerTurn) setPhase(Utility.parseInt(tkns[2]));
				} else if (tkns[1].contentEquals("DO")) {
					int type = Utility.parseInt(tkns[2]);

					if (cid == playerTurn) {
						// TODO
						String m = "MOVE " + cid + " " + type;
						for (int i=3;i<tkns.length;i++) m += " " + Utility.parseInt(tkns[i]);
						writeToAllClients(m);

						if (type == MT_ENDTURN) {
							doChangeTurn();
						} else if (type == MT_DRAW) {
							doDrawCards(cid, Utility.parseInt(tkns[3]));
						} else if (type == MT_DISCARD) {
							if (cid == PL_A) playerA.discardCard(Utility.parseInt(tkns[3]));
							else if (cid == PL_B) playerB.discardCard(Utility.parseInt(tkns[3]));
						} else if (type == MT_PLACE) {
							if (cid == PL_A) playerA.placeCard(Utility.parseInt(tkns[3]), Utility.parseInt(tkns[4]));
							else if (cid == PL_B) playerB.placeCard(Utility.parseInt(tkns[3]), Utility.parseInt(tkns[4]));
						} else if (type == MT_MOVE) {
							if (cid == PL_A) playerA.moveCard(Utility.parseInt(tkns[3]), Utility.parseInt(tkns[4]));
							else if (cid == PL_B) playerB.moveCard(Utility.parseInt(tkns[3]), Utility.parseInt(tkns[4]));
						} else if (type == MT_CLOCK) {
							if (cid == PL_A) playerA.clockCard(Utility.parseInt(tkns[3]));
							else if (cid == PL_B) playerB.clockCard(Utility.parseInt(tkns[3]));
						} else if (type == MT_ATTACK) {
							int attacktype = Utility.parseInt(tkns[3]);
							int attackstage = Utility.parseInt(tkns[4]);
							PlayerServer player = getPlayer(cid);
							CardData attacker = player.getCardData(player.getStageCard(attackstage));
							int trigger = player.triggerCard();
							int triggersoul = player.getCardData(trigger).soul;	// TODO trigger parsing
							int damage = 0;
							if (attacktype == 0) {
								damage = attacker.soul + triggersoul + 1;
							} else if (attacktype == 1) {
								damage = attacker.soul + triggersoul;
							} else if (attacktype == 2) {
								CardData defender = getPlayer(((cid == PL_A)?PL_B:PL_A)).getCardData(getPlayer(((cid == PL_A)?PL_B:PL_A)).getStageCard(2-attackstage));
								damage = attacker.soul + triggersoul - defender.level;
							}
							ArrayList<Integer> damaged = getPlayer(((cid == PL_A)?PL_B:PL_A)).damageCards(damage);
							writeToAllClients("MOVE " + cid + " " + MT_TRIGGER + " " + trigger);
							m = "MOVE " + ((cid == PL_A)?PL_B:PL_A) + " " + MT_DAMAGE;
							for (int i=0;i<damaged.size();i++) m += " " + damaged.get(i);
							writeToAllClients(m);

							if (attacktype != 0) {
								CardData defender = getPlayer(((cid == PL_A)?PL_B:PL_A)).getCardData(getPlayer(((cid == PL_A)?PL_B:PL_A)).getStageCard(2-attackstage));
								if (attacker.power > defender.power) {
									writeToAllClients("MOVE " + ((cid == PL_A)?PL_B:PL_A) + " " + MT_REVERSE + " " + (2-attackstage));
								} else if (attacker.power < defender.power) {
									writeToAllClients("MOVE " + cid + " " + MT_REVERSE + " " + attackstage);
								} else {
									writeToAllClients("MOVE " + cid + " " + MT_REVERSE + " " + attackstage);
									writeToAllClients("MOVE " + ((cid == PL_A)?PL_B:PL_A) + " " + MT_REVERSE + " " + (2-attackstage));
								}
							}
						}
					}
				}
			} else if (isState(GS_END)) {

			}

			msg = getInbox();
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
		writeToAllClients("STATE " + gameState);
	}
	@Override
	protected void setPhase(int phase) {
		super.setPhase(phase);
		writeToAllClients("PHASE " + gamePhase);
	}

	protected abstract void writeToClient(int cid, String msg);
	protected abstract void writeToAllClients(String msg);

}
