package zan.wscard.panel;

import java.util.ArrayList;

import zan.lib.input.InputManager;
import zan.lib.panel.BasePanel;
import zan.wscard.card.CardData;
import zan.wscard.card.CardReader;
import zan.wscard.core.GameCore;
import zan.wscard.mechanics.LocalGameClient;
import zan.wscard.mechanics.LocalGameServer;

public class TestPanel extends BasePanel {
	
	private LocalGameServer server;
	private LocalGameClient clientA;
	private LocalGameClient clientB;
	
	public TestPanel(GameCore core) {
		
	}
	
	@Override
	public void init() {
		server = new LocalGameServer();
		clientA = new LocalGameClient(server, 0);
		clientB = new LocalGameClient(server, 1);
		server.addClient(clientA);
		server.addClient(clientB);
		
		CardReader cr = new CardReader();
		ArrayList<CardData> LHCards = cr.loadCardData("res/card/LH.wsci");
		ArrayList<CardData> deckCards = new ArrayList<CardData>();
		
		final int[] deckSetup = {4, 1, 2, 4, 2, 2, 2, 2, 2, 2, 4, 2, 4, 2, 1, 4, 2, 2, 4, 2};
		for (int i=0;i<deckSetup.length;i++) {
			for (int j=0;j<deckSetup[i];j++) {
				deckCards.add(LHCards.get(i));
			}
		}
		
		server.init(deckCards, deckCards);
		clientA.init(deckCards, deckCards);
		clientB.init(deckCards, deckCards);
	}
	
	@Override
	public void destroy() {
		
	}
	
	@Override
	public void update(double time) {
		if (InputManager.isKeyReleased(InputManager.IM_KEY_A) && clientA.isInTurn()) clientA.writeToServer("REQ_HAND");
		if (InputManager.isKeyReleased(InputManager.IM_KEY_B) && clientB.isInTurn()) clientB.writeToServer("REQ_HAND");
		if (InputManager.isKeyReleased(InputManager.IM_KEY_S)) clientA.writeToServer("ENDTURN");
		if (InputManager.isKeyReleased(InputManager.IM_KEY_N)) clientB.writeToServer("ENDTURN");
		
		server.update();
		clientA.update();
		clientB.update();
	}
	
	@Override
	public void render(double ip) {
		
	}
	
}
