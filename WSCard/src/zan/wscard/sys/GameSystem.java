package zan.wscard.sys;

public abstract class GameSystem {
	
	public static final int GS_INIT = 0;
	public static final int GS_FIRSTDRAW = 1;
	public static final int GS_GAME = 2;
	public static final int GS_END = 3;
	
	public static final int GP_WAIT = 0;
	public static final int GP_STANDUP = 1;
	public static final int GP_DRAW = 2;
	public static final int GP_CLOCK = 3;
	public static final int GP_MAIN = 4;
	public static final int GP_ATTACK = 5;
	public static final int GP_END = 6;
	
	public static final int PL_NONE = -1;
	public static final int PL_A = 0;
	public static final int PL_B = 1;
	
	protected int gameState;
	protected int gamePhase;
	protected int playerTurn;
	
	public GameSystem() {
		gameState = GS_INIT;
		gamePhase = GP_WAIT;
		playerTurn = PL_NONE;
	}
	
	public boolean isState(int state) {return (gameState == state);}
	public boolean isPhase(int phase) {return (gamePhase == phase);}
	
	protected void setState(int state) {gameState = state;}
	protected void setPhase(int phase) {gamePhase = phase;}
	
	protected abstract String getInbox();
	
}
