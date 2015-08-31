package zan.wscard.sys;

import java.util.ArrayList;

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
	
	protected void doDrawCard(int cid, int num) {
		ArrayList<Integer> drawn = getPlayer(cid).drawCards(num);
		String msg = "DRAW";
		for (int i=0;i<drawn.size();i++) msg += " " + drawn.get(i);
		writeToClient(cid, msg);
		writeToClient((cid == PL_A)?PL_B:PL_A, "OPDRAW " + num);
	}
	
	protected void doChangeTurn() {
		if (playerTurn == PL_A) playerTurn = PL_B;
		else if (playerTurn == PL_B) playerTurn = PL_A;
		else playerTurn = PL_A;	// TODO
		writeToAllClients("TURN " + playerTurn);
	}
	
	public void update() {
		String msg = getInbox();
		while (msg != null && !msg.isEmpty()) {
			String[] tkns = msg.split(" ");
			int cid = Integer.parseInt(tkns[0]);
			
			if (isState(GS_INIT)) {
				if (tkns[1].contentEquals("READY")) {
					if (cid == 0) readyA = true;
					else if (cid == 1) readyB = true;
					if (readyA && readyB) {
						readyA = false;
						readyB = false;
						setState(GS_FIRSTDRAW);
						doDrawCard(0, 5);
						doDrawCard(1, 5);
					}
				}
			} else if (isState(GS_FIRSTDRAW)) {
				if (tkns[1].contentEquals("READY")) {
					if (cid == 0) readyA = true;
					else if (cid == 1) readyB = true;
					if (readyA && readyB) {
						readyA = false;
						readyB = false;
						setState(GS_GAME);
						doChangeTurn();
					}
				}
			} else if (isState(GS_GAME)) {
				if (cid == playerTurn) {
					if (tkns[1].contentEquals("PHASE")) {
						setPhase(Integer.parseInt(tkns[2]));
					} else if (tkns[1].contentEquals("REQDRAW")) {
						doDrawCard(cid, Integer.parseInt(tkns[2]));
					} else if (tkns[1].contentEquals("MOVE")) {
						int type = Integer.parseInt(tkns[2]);
						if (type == PlayerMove.MT_PLACE) {
							writeToClient((cid == PL_A)?PL_B:PL_A, "OPPLACE " + Integer.parseInt(tkns[4]));
						} else if (type == PlayerMove.MT_MOVE) {
							writeToClient((cid == PL_A)?PL_B:PL_A, "OPMOVE " + Integer.parseInt(tkns[3]) + " " + Integer.parseInt(tkns[4]));
						}
					} else if (tkns[1].contentEquals("ENDTURN")) {
						doChangeTurn();
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
