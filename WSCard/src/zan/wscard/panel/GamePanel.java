package zan.wscard.panel;

import java.util.ArrayList;

import zan.lib.panel.BasePanel;
import zan.wscard.card.CardData;
import zan.wscard.card.CardReader;

public class GamePanel extends BasePanel {
	
	public ArrayList<CardData> lh;
	
	public GamePanel() {
		
	}
	
	@Override
	public void init() {
		CardReader cr = new CardReader();
		lh = cr.loadCardData("res/card/LH.wsci");
	}
	
	@Override
	public void destroy() {
		
	}
	
	@Override
	public void update(double time) {
		
	}
	
	@Override
	public void render(double ip) {
		
	}
	
}
