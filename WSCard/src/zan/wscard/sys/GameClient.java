package zan.wscard.sys;

import java.util.ArrayList;

import zan.lib.util.Utility;
import static zan.wscard.sys.Player.NO_CARD;
import static zan.wscard.sys.PlayerMove.*;

public abstract class GameClient extends GameSystem {

	protected int clientID = PL_NONE;

	protected Player player = new Player();
	protected Player opponent = new Player();

	protected ArrayList<String> actionStack = new ArrayList<String>();
	protected ArrayList<String> clientLog = new ArrayList<String>();
	protected int logCount = 0;
	protected boolean waitForVerification = false;

	protected boolean inPhase = false;

	protected int storedPhase = GP_WAIT;

	public void initClient(PlayerInfo infoPlayer, PlayerInfo infoOpponent) {
		player.setInfo(infoPlayer);
		opponent.setInfo(infoOpponent);
	}

	public void nextPhase() {setPhase(++gamePhase);}
	public void submitMoves(ArrayList<PlayerMove> moves) {
		for (int i=0;i<moves.size();i++) {
			String m = "DO " + moves.get(i).getType();
			for (int j=0;j<moves.get(i).getNumArgs();j++) m += " " + moves.get(i).getArg(j);
			sendToServer(m);
		}
	}
	public void sendReady() {sendToServer("READY");}
	private void endTurn() {sendToServer("DO " + MT_ENDTURN);}
	public void endPhase() {inPhase = false;}

	public void processMessage(String[] tkns) {
		if (tkns[0].contentEquals("STATE")) {
			setState(Utility.parseInt(tkns[1]));
			inPhase = true;
		}

		if (isState(GS_INIT)) {

		} else if (isState(GS_FIRSTDRAW)) {
			if (tkns[0].contentEquals("MOVE")) {
				int mid = Utility.parseInt(tkns[1]);
				int type = Utility.parseInt(tkns[2]);
				if (mid == clientID) {	// TODO playerID
					// TODO sync player data
				} else {
					if (type == MT_DRAW) {
						for (int i=3;i<tkns.length;i++) {
							int drawn = Utility.parseInt(tkns[i]);
							if (drawn == NO_CARD) actionStack.add("OPRESHUFFLE");
							else actionStack.add("OPDRAW");
						}
					} else if (type == MT_DISCARD) {
						for (int i=3;i<tkns.length;i++) actionStack.add("OPDISCARD " + Utility.parseInt(tkns[i]));
					}
				}
			} else if (tkns[0].contentEquals("DRAW")) {
				for (int i=1;i<tkns.length;i++) {
					int drawn = Utility.parseInt(tkns[i]);
					if (drawn == NO_CARD) actionStack.add("RESHUFFLE");
					else actionStack.add("DRAW " + drawn);
				}
			}
		} else if (isState(GS_GAME)) {
			if (tkns[0].contentEquals("TURN")) {
				int turn = Utility.parseInt(tkns[1]);
				if (turn == clientID) {	// TODO playerID
					if (!isInTurn()) setPhase(GP_STANDUP);
				}
				playerTurn = turn;
			} else if (tkns[0].contentEquals("PHASE")) {
				int phase = Utility.parseInt(tkns[1]);
				if (phase == GP_WAIT) {
					actionStack.add("CLEANUP");
					actionStack.add("OPCLEANUP");
					if (isInTurn()) actionStack.add("OPSTANDUP");	// TODO
				}
			} else if (tkns[0].contentEquals("MOVE")) {
				int mid = Utility.parseInt(tkns[1]);
				int type = Utility.parseInt(tkns[2]);
				if (mid == clientID) {	// TODO playerID
					// TODO sync player data

					if (type == MT_TRIGGER) {
						actionStack.add("TRIGGER " + Utility.parseInt(tkns[3]));
					} else if (type == MT_DAMAGE) {
						for (int i=3;i<tkns.length;i++) {
							int drawn = Utility.parseInt(tkns[i]);
							if (drawn == NO_CARD) actionStack.add("RESHUFFLE");
							else actionStack.add("DAMAGE " + drawn);
						}
					} else if (type == MT_CANCELDAMAGE) {
						for (int i=3;i<tkns.length;i++) {
							int drawn = Utility.parseInt(tkns[i]);
							if (drawn == NO_CARD) actionStack.add("RESHUFFLE");
							else actionStack.add("CANCELDAMAGE " + drawn);
						}
					} else if (type == MT_REVERSE) {
						actionStack.add("REVERSE " + Utility.parseInt(tkns[3]));
					} else if (type == MT_RESHUFFLE) {
						actionStack.add("RESHUFFLECOST " + Utility.parseInt(tkns[3]));
					}
				} else {
					if (type == MT_DRAW) {
						for (int i=3;i<tkns.length;i++) {
							int drawn = Utility.parseInt(tkns[i]);
							if (drawn == NO_CARD) actionStack.add("OPRESHUFFLE");
							else actionStack.add("OPDRAW");
						}
					} else if (type == MT_DISCARD) {
						for (int i=3;i<tkns.length;i++) actionStack.add("OPDISCARD " + Utility.parseInt(tkns[i]));
					} else if (type == MT_PLACE) {
						actionStack.add("OPPLACE " + Utility.parseInt(tkns[3]) + " " + Utility.parseInt(tkns[4]));
					} else if (type == MT_SWAP) {
						actionStack.add("OPMOVE " + Utility.parseInt(tkns[3]) + " " + Utility.parseInt(tkns[4]));
					} else if (type == MT_CLOCK) {
						actionStack.add("OPCLOCK " + Utility.parseInt(tkns[3]));
					} else if (type == MT_ATTACK) {
						actionStack.add("OPATTACK " + Utility.parseInt(tkns[3]) + " " + Utility.parseInt(tkns[4]));
					} else if (type == MT_TRIGGER) {
						actionStack.add("OPTRIGGER " + Utility.parseInt(tkns[3]));
					} else if (type == MT_DAMAGE) {
						for (int i=3;i<tkns.length;i++) {
							int drawn = Utility.parseInt(tkns[i]);
							if (drawn == NO_CARD) actionStack.add("OPRESHUFFLE");
							else actionStack.add("OPDAMAGE " + drawn);
						}
					} else if (type == MT_CANCELDAMAGE) {
						for (int i=3;i<tkns.length;i++) {
							int drawn = Utility.parseInt(tkns[i]);
							if (drawn == NO_CARD) actionStack.add("OPRESHUFFLE");
							else actionStack.add("OPCANCELDAMAGE " + drawn);
						}
					} else if (type == MT_REVERSE) {
						actionStack.add("OPREVERSE " + Utility.parseInt(tkns[3]));
					} else if (type == MT_RESHUFFLE) {
						actionStack.add("OPRESHUFFLECOST " + Utility.parseInt(tkns[3]));
					} else if (type == MT_LEVELUP) {
						actionStack.add("OPLEVELUP " + Utility.parseInt(tkns[3]));
					}
				}

				if (type == MT_LEVELUP) {
					super.setPhase(storedPhase);
					storedPhase = GP_WAIT;
					inPhase = true;
				}
			} else if (tkns[0].contentEquals("DRAW")) {
				for (int i=1;i<tkns.length;i++) {
					int drawn = Utility.parseInt(tkns[i]);
					if (drawn == NO_CARD) actionStack.add("RESHUFFLE");
					else actionStack.add("DRAW " + drawn);
				}
				if (isPhase(GP_DRAW)) actionStack.add("NEXTPHASE");
			} else if (tkns[0].contentEquals("NOTIFYLEVELUP")) {
				storedPhase = gamePhase;
				int levelup = Utility.parseInt(tkns[1]);
				if (levelup == clientID) {
					super.setPhase(GP_LEVELUP);
				} else {
					super.setPhase(GP_WAIT);
				}
			}
		} else if (isState(GS_END)) {
			if (tkns[0].contentEquals("WINNER")) {
				actionStack.add("WINNER " + Utility.parseInt(tkns[1]));
			}
		}
	}

	public void update() {
		String msg;
		while ((msg = getInbox()) != null) {
			String[] chunk = msg.split(" ");

			if (chunk[0].contentEquals("VERIFY")) {
				for (int i=Utility.parseInt(chunk[1]);i<clientLog.size();i++) writeToServer(clientLog.get(i));
			} else if (chunk[0].contentEquals("CID")) {
				clientID = Utility.parseInt(chunk[1]);
			} else if (chunk.length > 1) {
				int cnt = Utility.parseInt(chunk[0]);
				if (cnt == logCount) waitForVerification = false;
				if (cnt >= logCount && !waitForVerification) {
					String[] tkns = new String[chunk.length-1];
					for (int i=1;i<chunk.length;i++) tkns[i-1] = chunk[i];
					processMessage(tkns);
					logCount = cnt + 1;
				} else if (!waitForVerification) {
					writeToServer("VERIFY " + logCount);
					waitForVerification = true;
				}
			}
		}

		if (isInTurn() && isInPhase()) {
			if (isPhase(GP_STANDUP)) {
				actionStack.add("STANDUP");
				actionStack.add("NEXTPHASE");
				endPhase();
			} else if (isPhase(GP_DRAW)) {
				sendToServer("DO " + MT_DRAW + " 1");
				endPhase();
			} else if (isPhase(GP_CLOCK)) {

			} else if (isPhase(GP_MAIN)) {

			} else if (isPhase(GP_ATTACK)) {

			} else if (isPhase(GP_END)) {
				setPhase(GP_WAIT);
				endTurn();
			}
		}
	}

	public String getAction() {
		if (actionStack.isEmpty()) return null;
		return actionStack.remove(0);
	}

	public Player getPlayer() {return player;}
	public Player getOpponent() {return opponent;}

	public boolean isInTurn() {return (playerTurn == clientID);}
	public boolean isInPhase() {return inPhase;}

	public int getClientID() {return clientID;}

	@Override
	protected void setPhase(int phase) {
		super.setPhase(phase);
		inPhase = true;
		sendToServer("PHASE " + gamePhase);
	}

	protected void sendToServer(String msg) {
		clientLog.add(clientLog.size() + " " + msg);
		writeToServer(clientLog.get(clientLog.size()-1));
	}

	protected abstract void writeToServer(String msg);

	// TODO
	public void printLog() {
		System.out.println("CLIENT LOG");
		for (int i=0;i<clientLog.size();i++) System.out.println(clientLog.get(i));
	}

}
