package zan.wscard.sys;

import java.util.ArrayList;

public abstract class Player {
	
	protected PlayerInfo info;
	
	protected ArrayList<Integer> deck;
	protected ArrayList<Integer> hand;
	protected int[] stage;
	
	public Player() {
		info = null;
		deck = new ArrayList<Integer>();
		hand = new ArrayList<Integer>();
		stage = new int[5];
		for (int i=0;i<5;i++) stage[i] = -1;
	}
	
	public void setInfo(PlayerInfo info) {
		this.info = info;
	}
	
}
