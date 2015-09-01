package zan.wscard.sys;

public class PlayerMove {
	
	public static final int MT_NONE = 0;
	public static final int MT_ENDTURN = 1;
	public static final int MT_DRAW = 2;
	public static final int MT_DISCARD = 3;
	public static final int MT_PLACE = 4;
	public static final int MT_MOVE = 5;
	
	private int type;
	private int[] args;
	
	public PlayerMove(int type, int... args) {
		this.type = type;
		this.args = args;
	}
	
	public int getType() {return type;}
	public int getArg(int arg) {return args[arg];}
	public int getNumArgs() {return args.length;}
	
}
