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

	public static final int ANS_NONE = 0;
	public static final int ANS_DRAW = 1;
	public static final int ANS_DEALDAMAGE = 2;

	public static final int ACT_NONE = 0;
	public static final int ACT_ENDTURN = 1;
	public static final int ACT_STANDUP = 2;
	public static final int ACT_DRAWTOHAND = 3;
	public static final int ACT_DISCARDFROMHAND = 4;
	public static final int ACT_CLOCKFROMHAND = 5;
	public static final int ACT_PLACEFROMHAND = 6;
	public static final int ACT_SWAPONSTAGE = 7;
	public static final int ACT_MAIN_END = 8;
	public static final int ACT_ATTACK_DECLARATION = 9;
	public static final int ACT_ATTACK_TRIGGER = 10;
	public static final int ACT_ATTACK_DAMAGE = 11;
	public static final int ACT_ATTACK_BATTLE = 12;
	public static final int ACT_ENCORE = 13;
	public static final int ACT_CLEANUP = 14;
	public static final int ACT_LEVELUP = 15;
	public static final int ACT_RESHUFFLE = 16;

	public static final int ACS_NONE = 0;
	public static final int ACS_WAIT = 1;
	public static final int ACS_ENDTURN = 2;
	public static final int ACS_PHASE = 3;
	public static final int ACS_SUBPHASE = 4;
	public static final int ACS_STORESUBPHASE = 5;
	public static final int ACS_RESTORESUBPHASE = 6;

	public static final int ACS_PL_NONE = 100;
	public static final int ACS_PL_ENDTURN = 101;
	public static final int ACS_PL_STANDUP = 102;
	public static final int ACS_PL_DRAWTOHAND = 103;
	public static final int ACS_PL_DISCARDFROMHAND = 104;
	public static final int ACS_PL_CLOCKFROMHAND = 105;
	public static final int ACS_PL_PLACEFROMHAND = 106;
	public static final int ACS_PL_SWAPONSTAGE = 107;
	public static final int ACS_PL_MAIN_END = 108;
	public static final int ACS_PL_ATTACK_DECLARATION = 109;
	public static final int ACS_PL_ATTACK_TRIGGER = 110;
	public static final int ACS_PL_ATTACK_DAMAGE = 111;
	public static final int ACS_PL_ATTACK_BATTLE = 112;
	public static final int ACS_PL_ENCORE = 113;
	public static final int ACS_PL_CLEANUP = 114;
	public static final int ACS_PL_LEVELUP = 115;
	public static final int ACS_PL_RESHUFFLE = 116;
	public static final int ACS_PL_ATTACK_CANCEL = 151;
	public static final int ACS_PL_REVERSE = 152;
	public static final int ACS_PL_PAYSTOCK = 153;

	public static final int ACS_OP_NONE = 200;
	public static final int ACS_OP_ENDTURN = 201;
	public static final int ACS_OP_STANDUP = 202;
	public static final int ACS_OP_DRAWTOHAND = 203;
	public static final int ACS_OP_DISCARDFROMHAND = 204;
	public static final int ACS_OP_CLOCKFROMHAND = 205;
	public static final int ACS_OP_PLACEFROMHAND = 206;
	public static final int ACS_OP_SWAPONSTAGE = 207;
	public static final int ACS_OP_MAIN_END = 208;
	public static final int ACS_OP_ATTACK_DECLARATION = 209;
	public static final int ACS_OP_ATTACK_TRIGGER = 210;
	public static final int ACS_OP_ATTACK_DAMAGE = 211;
	public static final int ACS_OP_ATTACK_BATTLE = 212;
	public static final int ACS_OP_ENCORE = 213;
	public static final int ACS_OP_CLEANUP = 214;
	public static final int ACS_OP_LEVELUP = 215;
	public static final int ACS_OP_RESHUFFLE = 216;
	public static final int ACS_OP_ATTACK_CANCEL = 251;
	public static final int ACS_OP_REVERSE = 252;
	public static final int ACS_OP_PAYSTOCK = 253;

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
	public static final int GP_ENCORE = 6;
	public static final int GP_END = 7;

	public static final int PL_NONE = -1;
	public static final int PL_A = 0;
	public static final int PL_B = 1;
	public static final int PL_NUM = 2;

	public static final int SP_END = 0;
	public static final int SP_START = 1;

	public static final int SP_FIRSTDRAW_DRAW = 2;
	public static final int SP_FIRSTDRAW_DISCARD = 3;

	public static final int SP_ATTACK_TRIGGER = 2;
	public static final int SP_ATTACK_DAMAGE = 3;
	public static final int SP_ATTACK_BATTLE = 4;

	public static final int SP_ENCORE = 2;
	public static final int SP_CLEANUP = 3;

	public static final int SP_DISCARD = 2;

	public static final int SP_WAIT = 10;
	public static final int SP_LEVELUP = 11;

	public static final int ATK_NONE = 0;
	public static final int ATK_DIRECT = 1;
	public static final int ATK_FRONTAL = 2;
	public static final int ATK_SIDE = 3;

	public static final int BTL_NONE = 0;
	public static final int BTL_ATTACKER = 1;
	public static final int BTL_DEFENDER = 2;
	public static final int BTL_TIE = 3;

	public static final int CS_NONE = 0;
	public static final int CS_STAND = 1;
	public static final int CS_REST = 2;
	public static final int CS_REVERSE = 3;

	public static final int CARD_NONE = -1;
	public static final int CARD_CHARA = 0;
	public static final int CARD_CLIMAX = 1;
	public static final int CARD_EVENT = 2;

	public static final int STAGE_NONE = -1;

	public static final int NUM_DECKCARDS = 50;
	public static final int NUM_STAGES = 5;

	protected int gameState = GS_INIT;
	protected int gamePhase = GP_WAIT;
	protected int playerTurn = PL_NONE;

	protected void setState(int state) {gameState = state;}
	protected void setPhase(int phase) {gamePhase = phase;}
	protected void setTurn(int turn) {playerTurn = turn;}

	public boolean isState(int state) {return (gameState == state);}
	public boolean isPhase(int phase) {return (gamePhase == phase);}
	public boolean isTurn(int turn) {return (playerTurn == turn);}

	protected abstract String getInbox();

}
