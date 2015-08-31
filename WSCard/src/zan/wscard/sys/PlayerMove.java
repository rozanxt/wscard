package zan.wscard.sys;

public class PlayerMove {
	
	public static final int MT_NONE = 0;
	public static final int MT_DRAW = 1;
	public static final int MT_PLACE = 2;
	public static final int MT_MOVE = 3;
	
	private int type;
	private int[] args;
	
	public PlayerMove(int type, int... args) {
		this.type = type;
		this.args = args;
	}
	
	public int getType() {return type;}
	public int getArg(int arg) {return args[arg];}
	public int getArg() {return getArg(0);}
	public int getStart() {return getArg(0);}
	public int getEnd() {return getArg(1);}
	public int getNumArgs() {return args.length;}
	
}
