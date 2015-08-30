package zan.wscard.sys;

import java.util.ArrayList;

public abstract class GameClient extends GameSystem {
	
	protected int clientID;
	
	protected PlayerClient player, opponent;
	
	protected boolean inPhase;
	
	public GameClient() {
		super();
		clientID = PL_NONE;
		player = new PlayerClient();
		opponent = new PlayerClient();
		inPhase = false;
	}
	
	public void initClient(PlayerInfo infoPlayer, PlayerInfo infoOpponent) {
		player.setInfo(infoPlayer);
		opponent.setInfo(infoOpponent);
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
					ArrayList<Integer> drawn = new ArrayList<Integer>();
					for (int i=1;i<tkns.length;i++) drawn.add(Integer.parseInt(tkns[i]));
					player.drawCards(drawn);
				}
			} else if (isState(GS_GAME)) {
				if (tkns[0].contentEquals("TURN")) {
					int turn = Integer.parseInt(tkns[1]);
					if (turn == clientID) {
						if (!isInTurn()) setPhase(GP_STANDUP);
					}
					playerTurn = turn;
				} else if (tkns[0].contentEquals("DRAW")) {
					ArrayList<Integer> drawn = new ArrayList<Integer>();
					for (int i=1;i<tkns.length;i++) drawn.add(Integer.parseInt(tkns[i]));
					player.drawCards(drawn);
					if (isPhase(GP_DRAW)) setPhase(GP_CLOCK);
				}
			} else if (isState(GS_END)) {
				
			}
			
			msg = getInbox();
		}
		
		if (isPhase(GP_STANDUP)) {
			setPhase(GP_DRAW);
		} else if (isPhase(GP_DRAW)) {
			writeToServer("REQDRAW 1");
		} else if (isPhase(GP_CLOCK)) {
			setPhase(GP_MAIN);
		} else if (isPhase(GP_MAIN)) {
			
		} else if (isPhase(GP_ATTACK)) {
			setPhase(GP_END);
		} else if (isPhase(GP_END)) {
			setPhase(GP_WAIT);
			writeToServer("ENDTURN");
		}
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
