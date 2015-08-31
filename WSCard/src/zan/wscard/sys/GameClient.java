package zan.wscard.sys;

import java.util.ArrayList;

public abstract class GameClient extends GameSystem {
	
	protected int clientID;
	
	protected PlayerClient player, opponent;
	
	protected boolean inPhase;
	
	protected ArrayList<String> actionStack;
	
	public GameClient() {
		super();
		clientID = PL_NONE;
		player = new PlayerClient();
		opponent = new PlayerClient();
		inPhase = false;
		actionStack = new ArrayList<String>();
	}
	
	public void initClient(PlayerInfo infoPlayer, PlayerInfo infoOpponent) {
		player.setInfo(infoPlayer);
		opponent.setInfo(infoOpponent);
	}
	
	public void nextPhase() {setPhase(++gamePhase);}
	public void submitMoves(ArrayList<PlayerMove> moves) {
		for (int i=0;i<moves.size();i++) {
			String msg = "MOVE " + moves.get(i).getType();
			for (int j=0;j<moves.get(i).getNumArgs();j++) msg += " " + moves.get(i).getArg(j);
			writeToServer(msg);
		}
	}
	public void sendReady() {writeToServer("READY");}
	public void endTurn() {writeToServer("ENDTURN");}
	public void endPhase() {inPhase = false;}
	
	public void update() {
		String msg = getInbox();
		while (msg != null && !msg.isEmpty()) {
			String[] tkns = msg.split(" ");
			
			if (tkns[0].contentEquals("STATE")) {
				setState(Integer.parseInt(tkns[1]));
			} else if (tkns[0].contentEquals("CID")) {
				clientID = Integer.parseInt(tkns[1]);
			}
			
			if (isState(GS_INIT)) {
				
			} else if (isState(GS_FIRSTDRAW)) {
				if (tkns[0].contentEquals("DRAW")) {
					for (int i=1;i<tkns.length;i++) actionStack.add("DRAW " + Integer.parseInt(tkns[i]));
				} else if (tkns[0].contentEquals("OPDRAW")) {
					for (int i=0;i<Integer.parseInt(tkns[1]);i++) actionStack.add("OPDRAW");
				}
			} else if (isState(GS_GAME)) {
				
				if (tkns[0].contentEquals("TURN")) {
					int turn = Integer.parseInt(tkns[1]);
					if (turn == clientID) {
						if (!isInTurn()) setPhase(GP_STANDUP);
					}
					playerTurn = turn;
				}
				
				if (isInTurn()) {
					if (tkns[0].contentEquals("DRAW")) {
						for (int i=1;i<tkns.length;i++) actionStack.add("DRAW " + Integer.parseInt(tkns[i]));
						actionStack.add("NEXTPHASE");
					}
				} else {
					if (tkns[0].contentEquals("PHASE")) {
						int phase = Integer.parseInt(tkns[1]);
						if (phase == GP_STANDUP+1) actionStack.add("OPSTANDUP");
						else if (phase == GP_CLOCK+1) actionStack.add("OPCLOCK");
						else if (phase == GP_MAIN+1) actionStack.add("OPMAIN");
						else if (phase == GP_ATTACK+1) actionStack.add("OPATTACK");
					} else if (tkns[0].contentEquals("OPDRAW")) {
						for (int i=0;i<Integer.parseInt(tkns[1]);i++) actionStack.add("OPDRAW");
					} else if (tkns[0].contentEquals("OPPLACE")) {
						actionStack.add(msg);
					} else if (tkns[0].contentEquals("OPMOVE")) {
						actionStack.add(msg);
					}
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
				writeToServer("REQDRAW 1");
				endPhase();
			} else if (isPhase(GP_CLOCK)) {
				actionStack.add("NEXTPHASE");
				endPhase();
			} else if (isPhase(GP_MAIN)) {
				
			} else if (isPhase(GP_ATTACK)) {
				actionStack.add("NEXTPHASE");
				endPhase();
			} else if (isPhase(GP_END)) {
				setPhase(GP_WAIT);
				writeToServer("ENDTURN");
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
