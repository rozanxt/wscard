package zan.wscard.sys;

public class PlayerAction {

	public final int type;
	public final int[] actions;

	public PlayerAction(int type, int... actions) {
		this.type = type;
		this.actions = actions;
	}

}
