package zan.wscard.sys;

public abstract class GameSystem {

	// MESSAGE ENUM

	public static final int MSG_NONE = 0;
	public static final int MSG_PING = 1;
	public static final int MSG_PONG = 2;
	public static final int MSG_READY = 3;
	public static final int MSG_STATE = 4;
	public static final int MSG_PHASE = 5;
	public static final int MSG_TURN = 6;
	public static final int MSG_REQUEST = 7;
	public static final int MSG_ANSWER = 8;
	public static final int MSG_ACTION = 9;
	public static final int MSG_INFO = 10;

	public static final int REQ_NONE = 0;
	public static final int REQ_DRAW = 1;
	public static final int REQ_DEALDAMAGE = 2;
	public static final int REQ_LEVELUP = 3;

	public static final int ANS_NONE = 0;
	public static final int ANS_DRAW = 1;
	public static final int ANS_DEALDAMAGE = 2;
	public static final int ANS_LEVELUP = 3;

	public static final int ACT_NONE = 0;
	public static final int ACT_ENDTURN = 1;
	public static final int ACT_STANDUP = 2;
	public static final int ACT_DRAWTOHAND = 3;
	public static final int ACT_DISCARDFROMHAND = 4;
	public static final int ACT_CLOCKFROMHAND = 5;
	public static final int ACT_PLACEFROMHAND = 6;
	public static final int ACT_SWAPONSTAGE = 7;
	public static final int ACT_ATTACK_DECLARATION = 8;
	public static final int ACT_ATTACK_TRIGGER = 9;
	public static final int ACT_ATTACK_DAMAGE = 10;
	public static final int ACT_ATTACK_BATTLE = 11;
	public static final int ACT_CLEANUP = 12;

	// GAME ENUM

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
	public static final int GP_LEVELUP = 7;

	public static final int PL_NONE = -1;
	public static final int PL_A = 0;
	public static final int PL_B = 1;
	public static final int PL_NUM = 2;

	public static final int SP_END = 0;
	public static final int SP_START = 1;

	public static final int SP_FIRSTDRAW_DISCARD = 2;

	public static final int SP_ATTACK_TRIGGER = 2;
	public static final int SP_ATTACK_DEALDAMAGE = 3;
	public static final int SP_ATTACK_BATTLE = 4;

	public static final int ATK_NONE = 0;
	public static final int ATK_DIRECT = 1;
	public static final int ATK_FRONTAL = 2;
	public static final int ATK_SIDE = 3;

	public static final int BTL_NONE = 0;
	public static final int BTL_ATTACKER = 1;
	public static final int BTL_DEFENDER = 2;
	public static final int BTL_TIE = 3;

	public static final int CARD_NONE = -1;
	public static final int CARD_CHARA = 0;
	public static final int CARD_CLIMAX = 1;
	public static final int CARD_EVENT = 2;

	public static final int STAGE_NONE = -1;

	protected int gameState = GS_INIT;
	protected int gamePhase = GP_WAIT;
	protected int playerTurn = PL_NONE;

	public boolean isState(int state) {return (gameState == state);}
	public boolean isPhase(int phase) {return (gamePhase == phase);}
	public boolean isTurn(int turn) {return (playerTurn == turn);}

	protected abstract String getInbox();

}
