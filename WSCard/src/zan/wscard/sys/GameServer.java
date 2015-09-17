package zan.wscard.sys;

import java.util.ArrayList;

import zan.lib.util.Utility;
import zan.wscard.card.CardData;
import static zan.wscard.sys.PlayerMove.*;
import static zan.wscard.sys.Player.NO_CARD;

public abstract class GameServer extends GameSystem {

	protected Player playerA = new Player();
	protected Player playerB = new Player();

	protected boolean readyA = false;
	protected boolean readyB = false;

	protected ArrayList<String> serverLog = new ArrayList<String>();
	protected int logCountA = 0;
	protected int logCountB = 0;
	protected boolean waitForVerificationA = false;
	protected boolean waitForVerificationB = false;

	protected int reshuffledA = 0;
	protected int reshuffledB = 0;
	protected boolean levelUpA = false;
	protected boolean levelUpB = false;
	protected int levelUp = PL_NONE;

	public void initServer(PlayerInfo infoA, PlayerInfo infoB) {
		playerA.setInfo(infoA);
		playerB.setInfo(infoB);
	}

	protected void sendMove(int cid, int type, int... args) {
		StringBuilder msg = new StringBuilder();
		msg.append("MOVE ").append(cid).append(" ").append(type);
		for (int i=0;i<args.length;i++) msg.append(" ").append(args[i]);
		sendToAllClients(msg.toString());
	}

	protected void doChangeTurn() {
		if (playerTurn == PL_A) playerTurn = PL_B;
		else if (playerTurn == PL_B) playerTurn = PL_A;
		else playerTurn = PL_A;	// TODO
		sendToAllClients("TURN " + playerTurn);
	}

	protected void doReshuffleDeck(int cid) {
		getPlayer(cid).reshuffleDeck();
		if (cid == PL_A) reshuffledA++;
		else if (cid == PL_B) reshuffledB++;
	}
	protected void doReshuffleCost(int cid) {
		if (cid == PL_A) {
			if (reshuffledA > 0) {
				for (int i=0;i<reshuffledA;i++) sendMove(cid, MT_RESHUFFLE, getPlayer(cid).reshuffleCost());
				reshuffledA = 0;
			}
		} else if (cid == PL_B) {
			if (reshuffledB > 0) {
				for (int i=0;i<reshuffledB;i++) sendMove(cid, MT_RESHUFFLE, getPlayer(cid).reshuffleCost());
				reshuffledB = 0;
			}
		}
	}

	protected void sendDrawCards(int cid, ArrayList<Integer> cards) {
		if (!cards.isEmpty()) {
			StringBuilder msg = new StringBuilder();
			msg.append("DRAW");
			int[] drawn = new int[cards.size()];
			for (int i=0;i<cards.size();i++) {
				msg.append(" ").append(cards.get(i));
				if (cards.get(i) == NO_CARD) drawn[i] = NO_CARD;
				else drawn[i] = 0;
			}
			sendToClient(cid, msg.toString());
			sendMove(cid, MT_DRAW, drawn);
			cards.clear();
		}
	}
	protected void doDrawCards(int cid, int num) {
		ArrayList<Integer> cards = new ArrayList<Integer>();
		for (int i=0;i<num;i++) {
			int drawn = getPlayer(cid).drawCard();
			cards.add(drawn);
			if (getPlayer(cid).isDeckEmpty()) {
				cards.add(NO_CARD);
				doReshuffleDeck(cid);
			}
		}
		sendDrawCards(cid, cards);
		doReshuffleCost(cid);
	}

	protected void doDiscardCard(int cid, int card) {
		getPlayer(cid).discardCard(card);
		sendMove(cid, MT_DISCARD, card);
	}

	protected void doPlaceCard(int cid, int card, int stage) {
		getPlayer(cid).placeCard(card, stage);
		sendMove(cid, MT_PLACE, card, stage);
	}

	protected void doSwapCard(int cid, int stage1, int stage2) {
		getPlayer(cid).swapCard(stage1, stage2);
		sendMove(cid, MT_SWAP, stage1, stage2);
	}

	protected void doClockCard(int cid, int card) {
		if (getPlayer(cid).clockCard(card) >= 7) {
			if (cid == PL_A) levelUpA = true;
			else if (cid == PL_B) levelUpB = true;
		}
		sendMove(cid, MT_CLOCK, card);
	}

	protected void sendDamageCards(int cid, ArrayList<Integer> cards) {
		if (!cards.isEmpty()) {
			int[] damage = new int[cards.size()];
			for (int i=0;i<cards.size();i++) {
				damage[i] = cards.get(i);
				if (getPlayer(cid).damageCard(damage[i]) >= 7) {
					if (cid == PL_A) levelUpA = true;
					else if (cid == PL_B) levelUpB = true;
				}
			}
			sendMove(cid, MT_DAMAGE, damage);
			cards.clear();
		}
	}
	protected void sendCancelDamageCards(int cid, ArrayList<Integer> cards) {
		if (!cards.isEmpty()) {
			int[] damage = new int[cards.size()];
			for (int i=0;i<cards.size();i++) damage[i] = cards.get(i);
			sendMove(cid, MT_CANCELDAMAGE, damage);
			cards.clear();
		}
	}
	protected void doAttackCard(int cid, int type, int stage) {
		Player player = getPlayer(cid);
		Player opponent = getPlayer((cid == PL_A)?PL_B:PL_A);
		CardData attacker = player.getStageCardData(stage);

		int trigger = player.triggerCard();
		if (player.isDeckEmpty()) {
			doReshuffleDeck(cid);
			// TODO send reshuffle
			sendMove(cid, MT_DRAW, NO_CARD);
			doReshuffleCost(cid);
		}
		int triggersoul = 0;	// TODO
		int damage = attacker.soul + triggersoul;
		sendMove(cid, MT_ATTACK, type, stage);
		sendMove(cid, MT_TRIGGER, trigger);

		if (type == 0) {
			damage += 1;
		} else if (type == 2) {
			CardData defender = opponent.getStageCardData(2-stage);
			damage -= defender.level;
		}

		boolean cancel = false;
		ArrayList<Integer> cards = new ArrayList<Integer>();
		for (int i=0;i<damage;i++) {
			int drawn = opponent.takeCard();
			cards.add(drawn);
			if (opponent.isDeckEmpty()) {
				cards.add(NO_CARD);
				doReshuffleDeck((cid == PL_A)?PL_B:PL_A);
			}
			if (opponent.getCardData(drawn).type == 1) {
				cancel = true;
				break;
			}
		}
		if (cancel) sendCancelDamageCards((cid == PL_A)?PL_B:PL_A, cards);
		else sendDamageCards((cid == PL_A)?PL_B:PL_A, cards);
		doReshuffleCost((cid == PL_A)?PL_B:PL_A);

		if (type == 1) {
			CardData defender = opponent.getStageCardData(2-stage);
			if (attacker.power > defender.power) {
				sendMove((cid == PL_A)?PL_B:PL_A, MT_REVERSE, (2-stage));
			} else if (attacker.power < defender.power) {
				sendMove(cid, MT_REVERSE, stage);
			} else {
				sendMove((cid == PL_A)?PL_B:PL_A, MT_REVERSE, (2-stage));
				sendMove(cid, MT_REVERSE, stage);
			}
		}
	}

	protected void doLevelUp(int cid, int card) {
		levelUp = PL_NONE;
		getPlayer(cid).levelUp(card);
		sendMove(cid, MT_LEVELUP, card);
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
				}
			}
		} else if (isState(GS_FIRSTDRAW)) {
			if (tkns[0].contentEquals("DO")) {
				int type = Utility.parseInt(tkns[1]);

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
					doDiscardCard(cid, Utility.parseInt(tkns[2]));
				}
			}
		} else if (isState(GS_GAME)) {
			if (tkns[0].contentEquals("PHASE")) {
				if (cid == playerTurn) setPhase(Utility.parseInt(tkns[1]));
			} else if (tkns[0].contentEquals("DO")) {
				int type = Utility.parseInt(tkns[1]);

				if (cid == playerTurn) {
					if (type == MT_ENDTURN) {
						doChangeTurn();
					} else if (type == MT_DRAW) {
						doDrawCards(cid, Utility.parseInt(tkns[2]));
					} else if (type == MT_DISCARD) {
						doDiscardCard(cid, Utility.parseInt(tkns[2]));
					} else if (type == MT_PLACE) {
						doPlaceCard(cid, Utility.parseInt(tkns[2]), Utility.parseInt(tkns[3]));
					} else if (type == MT_SWAP) {
						doSwapCard(cid, Utility.parseInt(tkns[2]), Utility.parseInt(tkns[3]));
					} else if (type == MT_CLOCK) {
						doClockCard(cid, Utility.parseInt(tkns[2]));
					} else if (type == MT_ATTACK) {
						doAttackCard(cid, Utility.parseInt(tkns[2]), Utility.parseInt(tkns[3]));
					}
				}

				if (levelUp == PL_NONE) {
					if (levelUpA) {
						if (getPlayer(PL_A).getLevel() < 3) {
							levelUpA = false;
							levelUp = PL_A;
							sendToAllClients("NOTIFYLEVELUP " + levelUp);
						} else {
							setState(GS_END);
							sendToAllClients("WINNER " + PL_B);
						}
					} else if (levelUpB) {
						if (getPlayer(PL_B).getLevel() < 3) {
							levelUpB = false;
							levelUp = PL_B;
							sendToAllClients("NOTIFYLEVELUP " + levelUp);
						} else {
							setState(GS_END);
							sendToAllClients("WINNER " + PL_A);
						}
					}
				} else {
					if (cid == levelUp && type == MT_LEVELUP) {
						doLevelUp(cid, Utility.parseInt(tkns[2]));
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

	protected Player getPlayer(int cid) {
		if (cid == PL_A) return playerA;
		else if (cid == PL_B) return playerB;
		return null;
	}
	protected Player getPlayerInTurn() {return getPlayer(playerTurn);}
	protected Player getPlayerInWait() {return getPlayer((playerTurn == PL_A)?PL_B:PL_A);}

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
