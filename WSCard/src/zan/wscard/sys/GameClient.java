package zan.wscard.sys;

import java.util.ArrayList;

import zan.lib.util.Utility;
import static zan.wscard.sys.PlayerMove.*;

public abstract class GameClient extends GameSystem {

	protected int clientID;

	protected PlayerClient player, opponent;

	protected ArrayList<String> actionStack;

	protected boolean inPhase;

	public GameClient() {
		super();
		clientID = PL_NONE;
		player = new PlayerClient();
		opponent = new PlayerClient();
		actionStack = new ArrayList<String>();
		inPhase = false;
	}

	public void initClient(PlayerInfo infoPlayer, PlayerInfo infoOpponent) {
		player.setInfo(infoPlayer);
		opponent.setInfo(infoOpponent);
	}

	public void nextPhase() {setPhase(++gamePhase);}
	public void submitRedraw(ArrayList<Integer> redraw) {
		String m = "DO " + MT_DISCARD;
		for (int i=0;i<redraw.size();i++) m += " " + redraw.get(i);
		writeToServer(m);
		writeToServer("DO " + MT_DRAW + " " + redraw.size());
		writeToServer("DO " + MT_ENDTURN);
	}
	public void submitClock(int card) {
		writeToServer("DO " + MT_CLOCK + " " + card);
		if (card != -1) writeToServer("DO " + MT_DRAW + " 2");
	}
	public void submitMoves(ArrayList<PlayerMove> moves) {
		for (int i=0;i<moves.size();i++) {
			String m = "DO " + moves.get(i).getType();
			for (int j=0;j<moves.get(i).getNumArgs();j++) m += " " + moves.get(i).getArg(j);
			writeToServer(m);
		}
	}
	public void sendReady() {writeToServer("READY");}
	public void endTurn() {writeToServer("DO " + MT_ENDTURN);}
	public void endPhase() {inPhase = false;}

	public void update() {
		String msg = getInbox();
		while (msg != null && !msg.isEmpty()) {
			String[] tkns = msg.split(" ");

			if (tkns[0].contentEquals("CID")) {
				clientID = Utility.parseInt(tkns[1]);
			} else if (tkns[0].contentEquals("STATE")) {
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
							for (int i=0;i<Utility.parseInt(tkns[3]);i++) actionStack.add("OPDRAW");
						} else if (type == MT_DISCARD) {
							for (int i=3;i<tkns.length;i++) actionStack.add("OPDISCARD " + Utility.parseInt(tkns[i]));
						}
					}
				} else if (tkns[0].contentEquals("DRAW")) {
					for (int i=1;i<tkns.length;i++) actionStack.add("DRAW " + Utility.parseInt(tkns[i]));
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
							for (int i=3;i<tkns.length;i++) actionStack.add("DAMAGE " + Utility.parseInt(tkns[i]));
						} else if (type == MT_REVERSE) {
							actionStack.add("REVERSE " + Utility.parseInt(tkns[3]));
						}
					} else {
						if (type == MT_DRAW) {
							for (int i=0;i<Utility.parseInt(tkns[3]);i++) actionStack.add("OPDRAW");
						} else if (type == MT_DISCARD) {
							for (int i=3;i<tkns.length;i++) actionStack.add("OPDISCARD " + Utility.parseInt(tkns[i]));
						} else if (type == MT_PLACE) {
							actionStack.add("OPPLACE " + Utility.parseInt(tkns[3]) + " " + Utility.parseInt(tkns[4]));
						} else if (type == MT_MOVE) {
							actionStack.add("OPMOVE " + Utility.parseInt(tkns[3]) + " " + Utility.parseInt(tkns[4]));
						} else if (type == MT_CLOCK) {
							actionStack.add("OPCLOCK " + Utility.parseInt(tkns[3]));
						} else if (type == MT_ATTACK) {
							actionStack.add("OPATTACK " + Utility.parseInt(tkns[3]) + " " + Utility.parseInt(tkns[4]));
						} else if (type == MT_TRIGGER) {
							actionStack.add("OPTRIGGER " + Utility.parseInt(tkns[3]));
						} else if (type == MT_DAMAGE) {
							for (int i=3;i<tkns.length;i++) actionStack.add("OPDAMAGE " + Utility.parseInt(tkns[i]));
						} else if (type == MT_REVERSE) {
							actionStack.add("OPREVERSE " + Utility.parseInt(tkns[3]));
						}
					}
				} else if (tkns[0].contentEquals("DRAW")) {
					for (int i=1;i<tkns.length;i++) actionStack.add("DRAW " + Utility.parseInt(tkns[i]));
					if (isPhase(GP_DRAW)) actionStack.add("NEXTPHASE");
				}
			} else if (isState(GS_END)) {

			}

			msg = getInbox();
		}

		if (isInTurn() && isInPhase()) {
			if (isPhase(GP_STANDUP)) {
				actionStack.add("STANDUP");
				actionStack.add("NEXTPHASE");
				endPhase();
			} else if (isPhase(GP_DRAW)) {
				writeToServer("DO " + MT_DRAW + " 1");
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

	public PlayerClient getPlayer() {return player;}
	public PlayerClient getOpponent() {return opponent;}

	public boolean isInTurn() {return (playerTurn == clientID);}
	public boolean isInPhase() {return inPhase;}

	@Override
	protected void setPhase(int phase) {
		super.setPhase(phase);
		inPhase = true;
		writeToServer("PHASE " + gamePhase);
	}

	protected abstract void writeToServer(String msg);

}
